package com.messdiener.cms.v3.web.controller.personal;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.PersonOverviewDTO;
import com.messdiener.cms.v3.app.entities.person.data.connection.PersonConnection;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.services.audit.AuditService;
import com.messdiener.cms.v3.app.services.document.DocumentService;
import com.messdiener.cms.v3.app.services.person.PersonConnectionService;
import com.messdiener.cms.v3.app.services.person.PersonFileService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.privacy.PrivacyService;
import com.messdiener.cms.v3.app.services.user.UserService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.PersonAttributes;
import com.messdiener.cms.v3.shared.enums.StatusState;
import com.messdiener.cms.v3.utils.html.HTMLClasses;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    @PostConstruct
    public void init() {
        LOGGER.info("PersonManagementController initialized.");
    }


    @GetMapping("/personal")
    public String messdienerList(HttpSession httpSession, Model model, @RequestParam("q") Optional<String> q, @RequestParam("id") Optional<String> id, @RequestParam("s") Optional<String> s, @RequestParam("statusState") Optional<StatusState> statusState) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        securityHelper.addPersonToSession(httpSession);

        if (!personHelper.hasPermission(user, 1))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Du hast keinen Zugriff auf dieses Modul");

        UUID tenantId = personHelper.getTenant(user).orElseThrow().getId();

        model.addAttribute("user", user);
        String idS = id.orElse("null");
        String query = q.orElse("list");
        String step = s.orElse("null");

        model.addAttribute("systemId", Cache.SYSTEM_USER);

        model.addAttribute("statusState", statusState);
        model.addAttribute("query", query);

        model.addAttribute("personHelper", personHelper);

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

            return "person/personInterface";
        }
        step = s.orElse("1");
        model.addAttribute("step", step);
        model.addAttribute("htmlClasses", new HTMLClasses());

        List<PersonOverviewDTO> persons = new ArrayList<>();
        switch (step) {
            case "1":
                persons = personService.getActivePersonsByPermissionDTO(user.getFRank(), tenantId);
                break;
            case "2":
                persons = personService.getActiveMessdienerByTenantDTO(tenantId);
                break;
            default:
                break;
        }
        model.addAttribute("persons", persons);

        return "person/personOverview";

    }

    @PostMapping("/personal/update")
    public RedirectView updatePerson(@RequestParam("id") UUID id, @RequestParam("type") String type,
                                     @RequestParam("rank") String rank, @RequestParam("fRank") int fRank,
                                     @RequestParam("principal") UUID principal, @RequestParam("salutation") String salutation,
                                     @RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname,
                                     @RequestParam("gender") String gender, @RequestParam("birthdate") Optional<String> birthdateE,
                                     @RequestParam("mail") String mail, @RequestParam("phone") String phone,
                                     @RequestParam("mobile") String mobile, @RequestParam("accessionDate") Optional<String> accessionDateE,
                                     @RequestParam("exitDate") Optional<String> exitDateE) throws SQLException {

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

        personService.updatePerson(person);

        return new RedirectView("/personal?q=profil&id=" + person.getId() + "&statusState=" + StatusState.EDIT_OK);

    }


    @PostMapping("/personal/adress/update")
    public RedirectView updateAddress(@RequestParam("id") UUID id, @RequestParam("street") String street,
                                      @RequestParam("houseNumber") String houseNumber, @RequestParam("postalCode") String postalCode,
                                      @RequestParam("city") String city) throws SQLException {

        Person person = personService.getPersonById(id).orElseThrow();
        person.setStreet(street);
        person.setHouseNumber(houseNumber);
        person.setPostalCode(postalCode);
        person.setCity(city);
        personService.updatePerson(person);

        return new RedirectView("/personal?q=profil&id=" + person.getId() + "&statusState=" + StatusState.EDIT_OK);
    }

    @PostMapping("/personal/bankAccount/update")
    public RedirectView updateBankAccount(@RequestParam("id") UUID id, @RequestParam("iban") String iban,
                                          @RequestParam("bic") String bic, @RequestParam("bank") String bank,
                                          @RequestParam("accountHolder") String accountHolder) throws SQLException {

        Person person = personService.getPersonById(id).orElseThrow();
        person.setIban(iban);
        person.setBic(bic);
        person.setBank(bank);
        person.setAccountHolder(accountHolder);
        personService.updatePerson(person);

        return new RedirectView("/personal?q=profil&id=" + person.getId() + "&statusState=" + StatusState.EDIT_OK);
    }

    @PostMapping("/personal/connection/create")
    public RedirectView createConnection(@RequestParam("host") UUID host, @RequestParam("sub") UUID sub, @RequestParam("type") String type) throws SQLException {
        PersonConnection personConnection = new PersonConnection(UUID.randomUUID(), host, sub, PersonAttributes.Connection.valueOf(type));
        personConnectionService.createConnection(personConnection);
        return new RedirectView("/personal?q=profil&s=2&id=" + host);
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
        return "person/privacyOverwiew";
    }

    @GetMapping("/personal/download")
    public ResponseEntity<?> download(@RequestParam("q") String q, @RequestParam("id") String id) throws SQLException, FileNotFoundException {

        File file = getFile(q, id).orElseThrow();
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");


        return ResponseEntity.ok()
                .headers(header)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    public Optional<File> getFile(String q, String id) {
        return Optional.of(new File("./cms_vx/person/" + id + "/" + q));
    }

}
