package com.messdiener.cms.person.web.controller;

import com.messdiener.cms.domain.audit.AuditQueryPort;
import com.messdiener.cms.domain.auth.UserAdminPort;
import com.messdiener.cms.domain.auth.UserCredential;
import com.messdiener.cms.domain.documents.DocumentQueryPort;
import com.messdiener.cms.domain.documents.StorageFileView;
import com.messdiener.cms.domain.documents.StorageQueryPort;
import com.messdiener.cms.domain.liturgy.LiturgyHelperPort;
import com.messdiener.cms.domain.liturgy.LiturgyQueryPort;
import com.messdiener.cms.domain.privacy.PrivacyQueryPort;
import com.messdiener.cms.domain.workflow.WorkflowQueryPort;
import com.messdiener.cms.person.app.helper.PersonHelper;
import com.messdiener.cms.person.persistence.service.PersonConnectionService;
import com.messdiener.cms.person.persistence.service.PersonFileService;
import com.messdiener.cms.person.persistence.service.PersonFlagService;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.person.domain.dto.PersonOverviewDTO;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.person.domain.entity.data.connection.PersonConnection;
import com.messdiener.cms.person.domain.entity.data.flags.PersonFlag;
import com.messdiener.cms.shared.cache.Cache;
import com.messdiener.cms.shared.enums.PersonAttributes;
import com.messdiener.cms.shared.enums.StatusState;
import com.messdiener.cms.shared.utils.html.HTMLClasses;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.utils.time.DateUtils;
import com.messdiener.cms.web.common.security.SecurityHelper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PersonManagementController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonManagementController.class);

    private final SecurityHelper securityHelper;
    private final PersonHelper personHelper;
    private final PersonFileService personFileService;
    private final PersonService personService;

    // ---- Domain-Ports statt Feature-Services
    private final DocumentQueryPort documentService;
    private final StorageQueryPort storageService;
    private final WorkflowQueryPort workflowQueryPort;
    private final AuditQueryPort auditService;
    private final PrivacyQueryPort privacyService;
    private final UserAdminPort userService;           // aus cms-domain-auth
    private final LiturgyHelperPort liturgieHelper;    // aus cms-domain-liturgy
    private final LiturgyQueryPort liturgieService;    // aus cms-domain-liturgy (aktuell ggf. ungenutzt)

    private final PersonConnectionService personConnectionService;
    private final PersonFlagService personFlagService;

    @PostConstruct
    public void init() {
        LOGGER.info("PersonManagementController initialized.");
    }

    @GetMapping("/personal")
    public String messdienerList(
            HttpSession httpSession, Model model,
            @RequestParam("q") Optional<String> q,
            @RequestParam("id") Optional<String> id,
            @RequestParam("s") Optional<String> s,
            @RequestParam("statusState") Optional<StatusState> statusState,
            @RequestParam("file") Optional<String> fileType,
            @RequestParam("startDate") Optional<String> startDateS,
            @RequestParam("endDate") Optional<String> endDateS) throws SQLException {

        UUID userId = securityHelper.getPerson().map(com.messdiener.cms.domain.person.PersonSessionView::id).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        securityHelper.addPersonToSession(httpSession);

        if (!personHelper.hasPermission(user, 1)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Du hast keinen Zugriff auf dieses Modul");
        }

        model.addAttribute("user", user);
        String idS = id.orElse("null");
        String query = q.orElse("list");
        String step = s.orElse("0");

        model.addAttribute("systemId", Cache.SYSTEM_USER);
        model.addAttribute("statusState", statusState);
        model.addAttribute("query", query);
        model.addAttribute("personHelper", personHelper);
        model.addAttribute("step", step);

        if (query.equals("profil")) {
            UUID personUUID = UUID.fromString(idS);
            Person person = personService.getPersonById(personUUID).orElseThrow();
            Optional<Person> principal = personService.getPersonById(person.getPrincipal());
            model.addAttribute("person", person);
            model.addAttribute("principalPerson", principal);
            model.addAttribute("types", PersonAttributes.Connection.values());
            model.addAttribute("persons", personService.getPersonsByTenant(person.getTenant()));
            model.addAttribute("contacts", personHelper.getEmergencyContacts(person));
            model.addAttribute("connections", personHelper.getConnections(person));
            model.addAttribute("workflows", workflowQueryPort.getWorkflowsByUserId(personUUID));
            model.addAttribute("files", personFileService.listFilesUsingJavaIO(person.getId()));

            // Documents via Domain-Port
            model.addAttribute("documents", documentService.getAllDocumentsByTarget(person.getId().toString()));
            model.addAttribute("managers", personService.getManagers());

            // Privacy & Audit via Domain-Port
            model.addAttribute("privacy", privacyService.getById(person.getId()));
            model.addAttribute("audit", auditService.getLogsByConnectId(person.getId()));
            model.addAttribute("flags", personFlagService.getAllFlagsByPerson(personUUID));

            switch (step) {
                case "overview" -> {
                    liturgieHelper.loadOverview(
                            model,
                            startDateS,
                            endDateS,
                            person.getTenant(),
                            Optional.of(person.getId())
                    );
                }
                case "contact" -> {
                    return "person/interface/personInterfaceContact";
                }
                case "documents" -> {
                    List<StorageFileView> files = storageService.getFiles(person.getId());
                    model.addAttribute("files", files);
                    return "person/interface/personInterfaceDocuments";
                }
                case "liturgy" -> {
                    liturgieHelper.loadOverview(
                            model,
                            startDateS,
                            endDateS,
                            person.getTenant(),
                            Optional.of(person.getId())
                    );
                    return "person/interface/personInterfaceLiturgy";
                }
                case "flags" -> {
                    return "person/interface/personInterfaceFlags";
                }
                case "settings" -> {
                    return "person/interface/personInterfaceSettings";
                }
                default -> throw new IllegalStateException("Unknown step: " + step);
            }
            return "person/interface/personInterfaceOverview";
        }

        // ---- Listen-Ansichten
        step = s.orElse("overview");
        model.addAttribute("step", step);
        model.addAttribute("htmlClasses", new HTMLClasses());

        List<PersonOverviewDTO> persons = new ArrayList<>();
        switch (step) {
            case "principal" -> persons = personService.getActivePersonsByPermissionDTO(user.getFRank(), user.getTenant());
            case "tenant"    -> persons = personService.getActiveMessdienerByTenantDTO(user.getTenant());
            case "create"    -> { return "person/list/createPerson"; }
            case "overview"  -> {
                persons = personService.getActivePersonsByPermissionDTO(user.getFRank(), user.getTenant());
                model.addAttribute("persons", persons);
                return "person/list/overview";
            }
            case "privacy"   -> {
                model.addAttribute("privacyPolicies", privacyService.getAll());
                return "person/list/privacyOverview";
            }
            default -> throw new IllegalStateException("Unknown step: " + step);
        }

        // Standardpfad (principal/tenant)
        model.addAttribute("persons", persons);
        return "person/list/personOverview";
    }

    @PostMapping("/personal/update")
    public RedirectView updatePerson(
            @RequestParam("id") UUID id,
            @RequestParam("type") String type,
            @RequestParam("rank") String rank,
            @RequestParam("fRank") int fRank,
            @RequestParam("principal") UUID principal,
            @RequestParam("salutation") String salutation,
            @RequestParam("firstname") String firstname,
            @RequestParam("lastname") String lastname,
            @RequestParam("gender") String gender,
            @RequestParam("birthdate") Optional<String> birthdateE,
            @RequestParam("mail") String mail,
            @RequestParam("phone") String phone,
            @RequestParam("mobile") String mobile,
            @RequestParam("accessionDate") Optional<String> accessionDateE,
            @RequestParam("exitDate") Optional<String> exitDateE,
            @RequestParam("street") String street,
            @RequestParam("houseNumber") String houseNumber,
            @RequestParam("postalCode") String postalCode,
            @RequestParam("city") String city,
            @RequestParam("iban") String iban,
            @RequestParam("bic") String bic,
            @RequestParam("bank") String bank,
            @RequestParam("accountHolder") String accountHolder
    ) throws SQLException {

        Person person = personService.getPersonById(id).orElseThrow();
        person.setType(PersonAttributes.Type.valueOf(type));
        person.setRank(PersonAttributes.Rank.valueOf(rank));
        person.setFRank(fRank);
        person.setPrincipal(principal);
        person.setSalutation(PersonAttributes.Salutation.valueOf(salutation));
        person.setFirstname(firstname);
        person.setLastname(lastname);
        person.setGender(PersonAttributes.Gender.valueOf(gender));

        person.setBirthdate(CMSDate.generateOptionalString(birthdateE, DateUtils.DateType.ENGLISH));
        person.setEmail(mail);
        person.setPhone(phone);
        person.setMobile(mobile);
        person.setAccessionDate(CMSDate.generateOptionalString(accessionDateE, DateUtils.DateType.ENGLISH));
        person.setExitDate(CMSDate.generateOptionalString(exitDateE, DateUtils.DateType.ENGLISH));

        person.setStreet(street);
        person.setHouseNumber(houseNumber);
        person.setPostalCode(postalCode);
        person.setCity(city);

        person.setIban(iban);
        person.setBic(bic);
        person.setBank(bank);
        person.setAccountHolder(accountHolder);

        personService.updatePerson(person);

        return new RedirectView("/personal?q=profil&id=" + person.getId() + "&statusState=" + StatusState.EDIT_OK + "&s=contact");
    }

    @PostMapping("/personal/connection/create")
    public RedirectView createConnection(
            @RequestParam("host") UUID host,
            @RequestParam("sub") UUID sub,
            @RequestParam("type") String type) throws SQLException {
        PersonConnection conn = new PersonConnection(UUID.randomUUID(), host, sub, PersonAttributes.Connection.valueOf(type));
        personConnectionService.createConnection(conn);
        return new RedirectView("/personal?q=profil&s=contact&id=" + host);
    }

    @PostMapping("/personal/user/update")
    public RedirectView updateLogin(
            @RequestParam("id") UUID id,
            @RequestParam("user") String user,
            @RequestParam("passwort") String passwort) throws SQLException {

        Person person = personService.getPersonById(id).orElseThrow();
        person.setUsername(user);
        person.setPassword(passwort);
        personService.updatePerson(person);

        // Map PersonLoginDTO -> UserCredential (Domain-Auth)
        var creds = personService.getPersonsByLogin().stream()
                .map(p -> new UserCredential(p.getUsername(), p.getPassword()))
                .collect(Collectors.toList());
        userService.initializeUsersAndPermissions(creds);

        return new RedirectView("/personal?s=6&q=profil&id=" + person.getId());
    }

    @GetMapping("/person/passwordReset")
    public RedirectView resetPW(@RequestParam("id") UUID id) throws SQLException {
        Person person = personService.getPersonById(id).orElseThrow();
        person.setPassword(person.getBirthdate().isPresent()
                ? person.getBirthdate().get().getGermanDate()
                : "PASSWORT");
        personService.updatePerson(person);

        var creds = personService.getPersonsByLogin().stream()
                .map(p -> new UserCredential(p.getUsername(), p.getPassword()))
                .collect(Collectors.toList());
        userService.initializeUsersAndPermissions(creds);

        return new RedirectView("/personal?s=6&q=profil&id=" + person.getId());
    }

    @GetMapping("/person/privacyPolicy")
    public String privacyPolicy(HttpSession httpSession, Model model) throws SQLException {
        var sessionView = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        UUID userId = sessionView.id();
        Person user = personService.getPersonById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        securityHelper.addPersonToSession(httpSession);

        if (!personHelper.hasPermission(user, 1)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Du hast keinen Zugriff auf dieses Modul");
        }

        model.addAttribute("privacyPolicies", privacyService.getAll());
        return "person/list/privacyOverview";
    }

    @GetMapping("/personal/connection/delete")
    public RedirectView deleteConnection(@RequestParam("id") UUID id, @RequestParam("p") UUID p) throws SQLException {
        personConnectionService.deleteConnection(id);
        return new RedirectView("/personal?q=profil&id=" + p + "&s=connection");
    }

    @PostMapping("/personal/flag")
    public RedirectView flag(
            @RequestParam("personId") UUID personId,
            @RequestParam("id") UUID id,
            @RequestParam("flagDetails") String flagDetails,
            @RequestParam("additionalInformation") String additionalInformation,
            @RequestParam("flagDate") String flagDateE) throws SQLException {

        PersonFlag personFlag = personFlagService.getFlag(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flag not found"));
        personFlag.setFlagDetails(flagDetails);
        personFlag.setAdditionalInformation(additionalInformation);
        personFlag.setFlagDate(CMSDate.convert(flagDateE, DateUtils.DateType.ENGLISH));
        personFlag.setComplained(true);
        personFlagService.saveFlag(personId, personFlag);

        return new RedirectView("/personal?q=profil&s=flags&id=" + personId);
    }

    @PostMapping("/personal/create")
    public RedirectView createPerson(
            @RequestParam("firstname") String firstname,
            @RequestParam("lastname") String lastname) throws SQLException {
        var sessionView = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        UUID userId = sessionView.id();
        Person user = personService.getPersonById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Person person = Person.empty(user.getTenant());
        person.setFirstname(firstname);
        person.setLastname(lastname);
        person.setCanLogin(true);
        person.setActive(true);
        person.setUsername(firstname.toLowerCase() + "." + lastname.toLowerCase());
        person.setPassword("changeMe");
        person.setRank(PersonAttributes.Rank.MESSDIENER);
        person.setFRank(0);
        person.setType(PersonAttributes.Type.MESSDIENER);
        personService.updatePerson(person);

        return new RedirectView("/personal?q=profil&s=overview&id=" + person.getId());
    }
}
