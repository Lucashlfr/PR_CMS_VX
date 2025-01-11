package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.data.connection.PersonConnection;
import com.messdiener.cms.v3.app.entities.user.Permission;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.PersonAttributes;
import com.messdiener.cms.v3.shared.enums.StatusState;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import com.messdiener.cms.v3.web.request.PermissionsForm;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class PersonalController {

    @GetMapping("/personal")
    public String messdienerList(HttpSession httpSession, Model model, @RequestParam("q") Optional<String> q, @RequestParam("id") Optional<String> id, @RequestParam("s") Optional<String> s, @RequestParam("statusState") Optional<StatusState> statusState) throws SQLException {

        Person user = SecurityHelper.getPerson();
        SecurityHelper.addPersonToSession(httpSession);

        if (!user.hasPermission("mp_module_personal"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Du hast keinen Zugriff auf dieses Modul");

        model.addAttribute("persons", Cache.getPersonService().getActiveMessdienerByTenant(user.getTenant().getId()));
        model.addAttribute("inactive", Cache.getPersonService().getInactiveMessdienerByTenant(user.getTenant().getId()));
        model.addAttribute("nullPerson", Cache.getPersonService().getPersonsByTenantAndType(user.getTenant().getId(), PersonAttributes.Type.NULL));
        model.addAttribute("statusState", statusState);


        model.addAttribute("user", user);
        String idS = id.orElse("null");
        String query = q.orElse("list");
        String step = s.orElse("null");

        model.addAttribute("query", query);

        switch (query) {
            case "profil": {

                UUID personUUID = UUID.fromString(idS);
                Person person = Cache.getPersonService().getPersonById(personUUID).orElseThrow();
                model.addAttribute("person", person);
                //model.addAttribute("duties", Cache.getWorshipService().getDutiesByPerson(person));
                //model.addAttribute("available", Cache.getWorshipService().getWorshipsByPerson(person.getId(), false, true));
                switch (step) {
                    case "2":
                        model.addAttribute("types", PersonAttributes.Connection.values());
                        model.addAttribute("persons", Cache.getPersonService().getPersonsByTenant(user.getTenant().getId()));
                        return "person/pages/person_connections";
                    case "3":
                        return "person/pages/person_events";
                    case "4":
                        return "person/pages/person_history";
                    case "5":
                        return "person/pages/person_files";
                    case "6":
                        return "person/pages/person_user";
                    case "7":
                        return "person/pages/person_workflows";
                    default:
                        return "person/pages/person_basisdata";
                }
            }
            case "permissions": {

                UUID personUUID = UUID.fromString(idS);
                Person person = Cache.getPersonService().getPersonById(personUUID).orElseThrow();
                model.addAttribute("person", person);

                List<Permission> permissions = Cache.getPermissionService().getUncheckPermissionsByUser(person);
                model.addAttribute("permissions", permissions);
                model.addAttribute("mappedPermissions", Cache.getPermissionService().getPermissionsByUser(person));
                model.addAttribute("permissionsForm", new PermissionsForm());

                return "person/permissions";
            }
            case "edit": {

                UUID personUUID = UUID.fromString(idS);
                Person person = Cache.getPersonService().getPersonById(personUUID).orElseThrow();
                model.addAttribute("person", person);
                return "person/personUpdate";
            }
            case "connection": {

                UUID personUUID = UUID.fromString(idS);
                Person person = Cache.getPersonService().getPersonById(personUUID).orElseThrow();
                model.addAttribute("persons", Cache.getPersonService().getPersonsByTenant(user.getTenant().getId()));
                model.addAttribute("types", PersonAttributes.Connection.values());
                model.addAttribute("person", person);
                return "person/personConnection";
            }
            default:
                return "person/personOverview";
        }

    }



    @GetMapping("/personal/query")
    public String query(HttpSession httpSession, Model model) throws SQLException {

        Person user = SecurityHelper.getPerson();
        SecurityHelper.addPersonToSession(httpSession);

        if (!user.hasPermission("mp_module_personal"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Du hast keinen Zugriff auf dieses Modul");

        model.addAttribute("months", DateUtils.MonthNumberName.values());

        boolean active = Cache.getConfigurationService().isPresent("workScheduler");
        String startMonth = "";

        if (active) {
            startMonth = Cache.getConfigurationService().get("workSchedulerStartMonth");
        }

        model.addAttribute("active", active);
        model.addAttribute("startMonth", startMonth);

        model.addAttribute("persons", Cache.getPersonService().getActiveMessdienerByTenant(user.getTenantId()));

        return "person/personQuery";
    }


    @PostMapping("/personal/create")
    public RedirectView createPerson(@RequestParam("tenantId") UUID tenantId, @RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname) throws SQLException {
        Person person = Cache.getPersonService().createPerson(tenantId);
        person.setFirstname(firstname);
        person.setLastname(lastname);
        person.update();
        return new RedirectView("/personal?q=profil&id=" + person.getId() + "&statusState=" + StatusState.CREATE_OK);
    }

    @PostMapping("/personal/update")
    public RedirectView updatePerson(@RequestParam("id") UUID id, @RequestParam("type") PersonAttributes.Type type,
                                     @RequestParam("rank") PersonAttributes.Rank rank, @RequestParam("salutation") PersonAttributes.Salutation salutation,
                                     @RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname,
                                     @RequestParam("gender") PersonAttributes.Gender gender, @RequestParam("birthdate") Optional<String> birthdateE,
                                     @RequestParam("mail") String mail, @RequestParam("phone") String phone,
                                     @RequestParam("mobile") String mobile, @RequestParam("accessionDate") Optional<String> accessionDateE,
                                     @RequestParam("exitDate") Optional<String> exitDateE) throws SQLException {

        Person person = Cache.getPersonService().getPersonById(id).orElseThrow();
        person.setType(type);
        person.setRank(rank);
        person.setSalutation(salutation);
        person.setFirstname(firstname);
        person.setLastname(lastname);
        person.setGender(gender);
        person.setBirthdate(CMSDate.generateOptionalString(birthdateE, DateUtils.DateType.ENGLISH));
        person.setEmail(mail);
        person.setPhone(phone);
        person.setMobile(mobile);
        person.setAccessionDate(CMSDate.generateOptionalString(accessionDateE, DateUtils.DateType.ENGLISH));
        person.setExitDate(CMSDate.generateOptionalString(exitDateE, DateUtils.DateType.ENGLISH));
        person.update();
        return new RedirectView("/personal?q=profil&id=" + person.getId() + "&statusState=" + StatusState.EDIT_OK);
    }

    @PostMapping("/personal/adress/update")
    public RedirectView updateAddress(@RequestParam("id") UUID id, @RequestParam("street") String street,
                                      @RequestParam("houseNumber") String houseNumber, @RequestParam("postalCode") String postalCode,
                                      @RequestParam("city") String city) throws SQLException {

        Person person = Cache.getPersonService().getPersonById(id).orElseThrow();
        person.setStreet(street);
        person.setHouseNumber(houseNumber);
        person.setPostalCode(postalCode);
        person.setCity(city);

        return new RedirectView("/personal?q=profil&id=" + person.getId() + "&statusState=" + StatusState.EDIT_OK);
    }

    @PostMapping("/personal/bankAccount/update")
    public RedirectView updateBankAccount(@RequestParam("id") UUID id, @RequestParam("iban") String iban,
                                          @RequestParam("bic") String bic, @RequestParam("bank") String bank,
                                          @RequestParam("accountHolder") String accountHolder) throws SQLException {

        Person person = Cache.getPersonService().getPersonById(id).orElseThrow();
        person.setIban(iban);
        person.setBic(bic);
        person.setBank(bank);
        person.setAccountHolder(accountHolder);
        return new RedirectView("/personal?q=profil&id=" + person.getId() + "&statusState=" + StatusState.EDIT_OK);
    }


    @PostMapping("/personal/deregister")
    public RedirectView deregister(@RequestParam("id") UUID id, @RequestParam("reason") String reason) throws SQLException {
        Person user = SecurityHelper.getPerson();
        Person person = Cache.getPersonService().getPersonById(id).orElseThrow();

        person.setActive(false);

        person.update();
        return new RedirectView("/personal?q=profil&id=" + person.getId());
    }

    @PostMapping("/personal/connection/create")
    public RedirectView createConnection(@RequestParam("host") UUID host, @RequestParam("sub") UUID sub, @RequestParam("type") String type) throws SQLException {
        PersonConnection personConnection = new PersonConnection(UUID.randomUUID(), host, sub, PersonAttributes.Connection.valueOf(type));
        personConnection.save();
        return new RedirectView("/personal?q=profil&s=2&id=" + host);
    }

    @PostMapping("/personal/permission")
    public RedirectView permissions(@ModelAttribute PermissionsForm permissionsForm) throws SQLException {
        Person person = Cache.getPersonService().getPersonById(permissionsForm.getId()).orElseThrow();
        List<String> selectedPermissions = permissionsForm.getSelectedPermissions();

        person.addPermissions(selectedPermissions);

        return new RedirectView("/personal?q=permissions&id=" + permissionsForm.getId());
    }

    @GetMapping("/personal/permissions/remove")
    public RedirectView remove(@RequestParam("id") UUID id, @RequestParam("p") String name) throws SQLException {
        Person person = Cache.getPersonService().getPersonById(id).orElseThrow();

        person.removePermission(name);

        return new RedirectView("/personal?q=permissions&id=" + id);
    }



    @PostMapping("/personal/user/update")
    public RedirectView updateLogin(@RequestParam("id") UUID id, @RequestParam("user") String user, @RequestParam("passwort") String passwort) throws SQLException {

        Person person = Cache.getPersonService().getPersonById(id).orElseThrow();
        person.setUsername(user);
        person.setPassword(passwort);
        person.update();

        Cache.getUserService().createUserInSecurity();

        return new RedirectView("/personal?q=profil&id=" + person.getId());
    }

    @GetMapping("/person/passwordReset")
    public RedirectView resetPW(@RequestParam("id") UUID id) throws SQLException {
        Person person = Cache.getPersonService().getPersonById(id).orElseThrow();
        person.update();

        Cache.getUserService().createUserInSecurity();

        return new RedirectView("/personal?q=profil&id=" + person.getId());
    }

}
