package com.messdiener.cms.v3.web.controller.personal;

import com.messdiener.cms.v3.app.entities.document.StorageFile;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.PersonOverviewDTO;
import com.messdiener.cms.v3.app.entities.person.data.connection.PersonConnection;
import com.messdiener.cms.v3.app.entities.person.data.flags.PersonFlag;
import com.messdiener.cms.v3.app.helper.liturgie.LiturgieHelper;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.audit.AuditService;
import com.messdiener.cms.v3.app.services.document.DocumentService;
import com.messdiener.cms.v3.app.services.document.StorageService;
import com.messdiener.cms.v3.app.services.liturgie.LiturgieService;
import com.messdiener.cms.v3.app.services.person.PersonConnectionService;
import com.messdiener.cms.v3.app.services.person.PersonFileService;
import com.messdiener.cms.v3.app.services.person.PersonFlagService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.privacy.PrivacyService;
import com.messdiener.cms.v3.app.services.user.UserService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.PersonAttributes;
import com.messdiener.cms.v3.shared.enums.StatusState;
import com.messdiener.cms.v3.shared.enums.document.FileType;
import com.messdiener.cms.v3.utils.html.HTMLClasses;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
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

@Controller
@RequiredArgsConstructor
public class PersonManagementController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonManagementController.class);
    private final SecurityHelper securityHelper;
    private final PersonHelper personHelper;
    private final PersonFileService personFileService;
    private final PersonService personService;
    private final DocumentService documentService;
    private final WorkflowService workflowService;
    private final AuditService auditService;
    private final PrivacyService privacyService;
    private final PersonConnectionService personConnectionService;
    private final UserService userService;
    private final StorageService storageService;
    private final LiturgieHelper liturgieHelper;
    private final LiturgieService liturgieService;
    private final PersonFlagService personFlagService;

    @PostConstruct
    public void init() {
        LOGGER.info("PersonManagementController initialized.");
    }


    @GetMapping("/personal")
    public String messdienerList(HttpSession httpSession, Model model, @RequestParam("q") Optional<String> q, @RequestParam("id") Optional<String> id,
                                 @RequestParam("s") Optional<String> s, @RequestParam("statusState") Optional<StatusState> statusState,
                                 @RequestParam("file") Optional<String> fileType,
                                 @RequestParam("startDate") Optional<String> startDateS, @RequestParam("endDate") Optional<String> endDateS) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(httpSession);

        if (!personHelper.hasPermission(user, 1))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Du hast keinen Zugriff auf dieses Modul");

        UUID tenantId = personHelper.getTenant(user).orElseThrow().getId();

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
            model.addAttribute("person", person);
            model.addAttribute("types", PersonAttributes.Connection.values());
            model.addAttribute("persons", personService.getPersonsByTenant(tenantId));
            model.addAttribute("contacts", personHelper.getEmergencyContacts(person));
            model.addAttribute("connections", personHelper.getConnections(person));
            model.addAttribute("workflows", workflowService.getWorkflowsByUserId(personUUID));
            model.addAttribute("files", personFileService.listFilesUsingJavaIO(person.getId()));
            model.addAttribute("documents", documentService.getAllDocumentsByTarget(person.getId().toString()));
            model.addAttribute("managers", personService.getManagers());

            model.addAttribute("privacy", privacyService.getById(person.getId()));

            model.addAttribute("audit", auditService.getLogsByConnectId(person.getId()));
            model.addAttribute("flags", personFlagService.getAllFlagsByPerson(personUUID));

            switch (step) {
                case "overview" ->
                        liturgieHelper.extractedLoadMethod(model, startDateS, endDateS, person, Optional.of(person));
                case "contact" -> {
                    return "person/interface/personInterfaceContact";
                }
                case "documents" -> {
                    List<StorageFile> files = storageService.getFiles(person.getId());
                    model.addAttribute("files", files);
                    return "person/interface/personInterfaceDocuments";
                }
                case "liturgy" -> {
                    liturgieHelper.extractedLoadMethod(model, startDateS, endDateS, person, Optional.of(person));
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
        step = s.orElse("overview");
        model.addAttribute("step", step);
        model.addAttribute("htmlClasses", new HTMLClasses());

        List<PersonOverviewDTO> persons = new ArrayList<>();
        switch (step) {
            case "principal" -> persons = personService.getActivePersonsByPermissionDTO(user.getFRank(), tenantId);
            case "tenant" -> persons = personService.getActiveMessdienerByTenantDTO(tenantId);
            case "create" -> {

            }

            case "overview" -> {
                persons = personService.getActivePersonsByPermissionDTO(user.getFRank(), tenantId);
                model.addAttribute("persons", (List<PersonOverviewDTO>) persons);
                return "person/list/overview";
            }

            case "privacy" -> {
                model.addAttribute("privacyPolicies", privacyService.getAll());
                return "person/list/privacyOverview";
            }

            default -> throw new IllegalStateException("Unknown step: " + step);
        }
        model.addAttribute("persons", persons);

        return "person/list/personOverview";

    }

    @PostMapping("/personal/update")
    public RedirectView updatePerson(@RequestParam("id") UUID id, @RequestParam("type") String type,
                                     @RequestParam("rank") String rank, @RequestParam("fRank") int fRank,
                                     @RequestParam("principal") UUID principal, @RequestParam("salutation") String salutation,
                                     @RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname,
                                     @RequestParam("gender") String gender, @RequestParam("birthdate") Optional<String> birthdateE,
                                     @RequestParam("mail") String mail, @RequestParam("phone") String phone,
                                     @RequestParam("mobile") String mobile, @RequestParam("accessionDate") Optional<String> accessionDateE,
                                     @RequestParam("exitDate") Optional<String> exitDateE, @RequestParam("street") String street,
                                     @RequestParam("houseNumber") String houseNumber, @RequestParam("postalCode") String postalCode,
                                     @RequestParam("city") String city, @RequestParam("iban") String iban,
                                     @RequestParam("bic") String bic, @RequestParam("bank") String bank,
                                     @RequestParam("accountHolder") String accountHolder) throws SQLException {

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
    public RedirectView createConnection(@RequestParam("host") UUID host, @RequestParam("sub") UUID sub, @RequestParam("type") String type) throws SQLException {
        PersonConnection personConnection = new PersonConnection(UUID.randomUUID(), host, sub, PersonAttributes.Connection.valueOf(type));
        personConnectionService.createConnection(personConnection);
        return new RedirectView("/personal?q=profil&s=contact&id=" + host);
    }

    @PostMapping("/personal/user/update")
    public RedirectView updateLogin(@RequestParam("id") UUID id, @RequestParam("user") String user, @RequestParam("passwort") String passwort) throws SQLException {

        Person person = personService.getPersonById(id).orElseThrow();
        person.setUsername(user);
        person.setPassword(passwort);
        personService.updatePerson(person);

        userService.initializeUsersAndPermissions();

        return new RedirectView("/personal?s=6&q=profil&id=" + person.getId());
    }

    @GetMapping("/person/passwordReset")
    public RedirectView resetPW(@RequestParam("id") UUID id) throws SQLException {
        Person person = personService.getPersonById(id).orElseThrow();
        person.setPassword(person.getBirthdate().isPresent() ? person.getBirthdate().get().getGermanDate() : "PASSWORT");
        personService.updatePerson(person);

        userService.initializeUsersAndPermissions();

        return new RedirectView("/personal?s=6&q=profil&id=" + person.getId());
    }

    @GetMapping("/person/privacyPolicy")
    public String privacyPolicy(HttpSession httpSession, Model model) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(httpSession);

        if (!personHelper.hasPermission(user, 1))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Du hast keinen Zugriff auf dieses Modul");


        model.addAttribute("privacyPolicies", privacyService.getAll());
        return "person/list/privacyOverview";
    }


    @GetMapping("/personal/connection/delete")
    public RedirectView deleteConnection(@RequestParam("id") UUID id, @RequestParam("p") UUID p) throws SQLException {
        personConnectionService.deleteConnection(id);
        return new RedirectView("/personal?q=profil&id=" + p + "&s=connection");
    }

    @PostMapping("/personal/flag")
    public RedirectView flag(@RequestParam("personId") UUID personId, @RequestParam("id") UUID id, @RequestParam("flagDetails") String flagDetails, @RequestParam("additionalInformation") String additionalInformation, @RequestParam("flagDate") String flagDateE) throws SQLException {

        PersonFlag personFlag = personFlagService.getFlag(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flag not found"));
        personFlag.setFlagDetails(flagDetails);
        personFlag.setAdditionalInformation(additionalInformation);
        personFlag.setFlagDate(CMSDate.convert(flagDateE, DateUtils.DateType.ENGLISH));
        personFlag.setComplained(true);
        personFlagService.saveFlag(personId, personFlag);

        return new RedirectView("/personal?q=profil&s=flags&id=" + personId);
    }

}
