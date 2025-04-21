package com.messdiener.cms.v3.web.controller;

import com.itextpdf.text.DocumentException;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.data.EmergencyContact;
import com.messdiener.cms.v3.app.entities.tasks.Task;
import com.messdiener.cms.v3.app.entities.tasks.message.TaskMessage;
import com.messdiener.cms.v3.app.export.FileCreator;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.StatusState;
import com.messdiener.cms.v3.shared.enums.onboarding.OnboardingType;
import com.messdiener.cms.v3.shared.enums.tasks.MessageInformationCascade;
import com.messdiener.cms.v3.shared.enums.tasks.MessageType;
import com.messdiener.cms.v3.shared.enums.tasks.TaskState;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class OnboardingController {

    private final Cache cache;
    private final SecurityHelper securityHelper;
    private final PersonHelper personHelper;

    @GetMapping("/onboarding")
    public String onboarding(HttpSession session, Model model, @RequestParam("q") Optional<String> q, @RequestParam("statusState") Optional<StatusState> statusState, Principal principal) throws SQLException {
        securityHelper.addPersonToSession(session);
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        model.addAttribute("user", user);

        String query = q.orElse("NULL");
        OnboardingType type = OnboardingType.valueOf(query);

        boolean state = statusState.isPresent();
        String status = statusState.orElse(StatusState.NONE).getLabel();
        model.addAttribute("status", status);
        model.addAttribute("state", state);
        model.addAttribute("tenantName", personHelper.getTenantName(user).orElse(""));

        String birthdate = "";
        if(user.getBirthdate().isPresent()) {
            birthdate = user.getBirthdate().get().getGermanDate();
        }
        model.addAttribute("birthdate", birthdate);

        return switch (type) {
            case PERSONAL_INFORMATION -> "onboarding/ob_data";
            case PRIVACY_POLICY -> "onboarding/ob_privacy_policy";
            case SAE -> {
                model.addAttribute("current", CMSDate.current());
                yield "onboarding/ob_self_declaration";
            }
            case EMERGENCY_CONTACT -> {
                model.addAttribute("contactList", personHelper.getEmergencyContacts(user));
                yield "onboarding/ob_emergency_contact";
            }
            default -> "onboarding/onboardingIndex";
        };

    }

    @PostMapping("/onboarding/check/submit")
    public RedirectView save(@RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname, @RequestParam("phone") Optional<String> phone,
                             @RequestParam("mobile") Optional<String> mobile, @RequestParam("mail") Optional<String> mail,
                             @RequestParam("street") Optional<String> street, @RequestParam("number") Optional<String> number, @RequestParam("plz") Optional<String> plz, @RequestParam("town") Optional<String> town,
                             @RequestParam("birthday") Optional<String> birthday, @RequestParam("data") Optional<String> data, @RequestParam("id") UUID workflowId) throws Exception {
        Person personOld = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person person = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        person.setFirstname(firstname);
        person.setLastname(lastname);
        person.setPhone(phone.orElse(""));
        person.setMobile(mobile.orElse(""));
        person.setEmail(mail.orElse(""));

        person.setStreet(street.orElse(""));
        person.setHouseNumber(number.orElse(""));
        person.setPostalCode(plz.orElse(""));
        person.setCity(town.orElse(""));

        CMSDate cmsDate = birthday.map(s -> CMSDate.convert(s, DateUtils.DateType.ENGLISH)).orElseGet(CMSDate::current);
        person.setBirthdate(Optional.of(cmsDate));
        person.setOb1(true);

        cache.getPersonService().updatePerson(person);

        new FileCreator()
                .setReceiver("Leitungsteam", personHelper.getTenantName(person).orElse(""), "Hintere Straße 1", "76756 Bellheim")
                .setGreeting("Leitungsteam " + personHelper.getTenantName(person).orElse(""))
                .setSender(person, personHelper.getTenantName(person).orElse(""))
                .setFolder("person", person.getId().toString())
                .setFileName("persoenliche_informationen.pdf")
                .addText("folgende Informationen wurden über das Onboarding angepasst:")
                .addAttribute("Vorname", firstname)
                .addAttribute("Nachname", lastname)
                .addAttribute("Telefon-Nr.", phone.orElse(""))
                .addAttribute("Handynummer", mobile.orElse(""))
                .addAttribute("Mail", mail.orElse(""))
                .addAttribute("Straße", street.orElse(""))
                .addAttribute("Hausnummer", number.orElse(""))
                .addAttribute("Plz", plz.orElse(""))
                .addAttribute("Ort", town.orElse(""))
                .addAttribute("Geburtstag", cmsDate.getGermanDate())
                .addAttribute("Sonstiges", data.orElse(""))
                .createPdf();

        Task task = cache.getTaskQueryService().getNormedTaskById(person.getId(), "OB1").orElseThrow();

        //TODO: FIX TaskUtils.checkPersons(task, personOld, person);

        TaskMessage taskMessage = new TaskMessage(UUID.randomUUID(), MessageType.ENDE, "Onboarding-Task wurde beendet.", "", MessageInformationCascade.C0, CMSDate.current(), Optional.ofNullable(person.getId()), false);
        cache.getTaskMessageService().saveMessage(task.getTaskId(), taskMessage);

        task.setTaskState(TaskState.COMPLETED);
        task.setEndDate(Optional.of(CMSDate.current()));
        task.setUpdateDate(CMSDate.current());
        cache.getTaskService().saveTask(task);

        return new RedirectView("/onboarding?statusState=" + StatusState.CHECK_OK);
    }

    @PostMapping("/onboarding/privacy/submit")
    public RedirectView save(@RequestParam("option1") Optional<String> option1, @RequestParam("option2") Optional<String> option2,
                             @RequestParam("option3") Optional<String> option3, @RequestParam("option4") Optional<String> option4,
                             @RequestParam("option5") Optional<String> option5, @RequestParam("option6") Optional<String> option6,
                             @RequestParam("option7") Optional<String> option7, @RequestParam("signature") String signature) throws Exception {

        Person person = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        String data = option1.isPresent() + ";" + option2.isPresent() + ";" + option3.isPresent() + ";" + option4.isPresent() + ";" + option5.isPresent() + ";" + option6.isPresent() + ";" + option7.isPresent();


        if (signature.isEmpty()) throw new IllegalStateException();

        person.setPrivacyPolicy(data);
        person.setSignature(signature);
        person.setOb3(true);

        cache.getPersonService().updatePerson(person);

        FileCreator fileCreator = new FileCreator()
                .setReceiver("Leitungsteam", personHelper.getTenantName(person).orElse(""), "Hintere Straße 1", "76756 Bellheim")
                .setGreeting("Leitungsteam " + personHelper.getTenantName(person).orElse(""))
                .setSender(person, personHelper.getTenantName(person).orElse(""))
                .setFolder("person", person.getId().toString())
                .setFileName("privacy.pdf");

        if (option1.isPresent()) {
            fileCreator.addText("Hiermit willigen wir / willige ich ein, dass Fotoaufnahmen, die die KiGem/Pfarrei oder eines ihrer Mitglieder bei Veranstaltungen erstellt und auf denen der Ministrant bzw. die Eltern selbst abgebildet sind, für Internet-Präsentationen der Pfarrei / Gruppe verwendet werden dürfen.");
        }
        if (option2.isPresent()) {
            fileCreator.addText("Hiermit willigen wir / willige ich ein, dass Fotoaufnahmen, die die KiGem/Pfarrei oder eines ihrer Mitglieder bei Veranstaltungen erstellt und auf denen der Ministrant bzw. die Eltern selbst abgebildet sind, für an andere Eltern - auch in der Form digitaler Speichermedien - weitergegeben werden dürfen.");
        }

        if (option3.isPresent()) {
            fileCreator.addText("Hiermit willigen wir / willige ich ein, dass Fotoaufnahmen, die die KiGem/Pfarrei oder eines ihrer Mitglieder bei Veranstaltungen erstellt und auf denen der Ministrant bzw. die Eltern selbst abgebildet sind, in Mitteilungen an die Mitglieder der katholischen Kirche wie z. B. dem Pfarrbrief wiedergegeben werden dürfen");
        }

        if (option4.isPresent()) {
            fileCreator.addText("Hiermit willigen wir / willige ich ein, dass Fotoaufnahmen, die die KiGem/Pfarrei oder eines ihrer Mitglieder bei Veranstaltungen erstellt und auf denen der Ministrant bzw. die Eltern selbst abgebildet sind, an öffentliche Publikationsorgane zum Zwecke der Veröffentlichung weitergegeben werden dürfen.");
        }

        if (option5.isPresent()) {
            fileCreator.addText("Hiermit willigen wir / willige ich ein, der Name, die Telefonnummer und die Emailadresse des Ministranten an die anderen Ministranten und deren Leiter für dienstliche Absprachen weitergegeben werden dürfen.");
        }

        if (option6.isPresent()) {
            fileCreator.addText("Hiermit willigen wir / willige ich ein, der Name des Ministranten in Veröffentlichungen der Einrichtung genannt werden darf.");
        }

        if (option7.isPresent()) {
            fileCreator.addText("Ich versichere, dass ich alleiniger Personensorgeberechtigter bin.");
        }

        fileCreator.createPdf();

        Task task = cache.getTaskQueryService().getNormedTaskById(person.getId(), "OB3").orElseThrow();
        TaskMessage taskMessage = new TaskMessage(UUID.randomUUID(), MessageType.ENDE, "Onboarding-Task Datenschutz wurde beendet.", "", MessageInformationCascade.C0, CMSDate.current(), Optional.ofNullable(person.getId()), false);
        cache.getTaskMessageService().saveMessage(task.getTaskId(), taskMessage);
        task.setTaskState(TaskState.COMPLETED);
        task.setEndDate(Optional.of(CMSDate.current()));
        task.setUpdateDate(CMSDate.current());
        cache.getTaskService().saveTask(task);


        return new RedirectView("/onboarding?statusState=" + StatusState.PRIVACY_POLICY_OK);
    }

    @PostMapping("/onboarding/sae/submit")
    public RedirectView saveSAE(@RequestParam("signature") String signature) throws SQLException, DocumentException, IOException {

        Person person = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        person.setOb4(true);
        cache.getPersonService().updatePerson(person);

        new FileCreator()
                .setReceiver("Leitungsteam", personHelper.getTenantName(person).orElse(""), "Hintere Straße 1", "76756 Bellheim")
                .setGreeting("Leitungsteam " + personHelper.getTenantName(person).orElse(""))
                .setSender(person, personHelper.getTenantName(person).orElse(""))
                .setFolder("person", person.getId().toString())
                .setFileName("sae.pdf")
                .addText("Gemäß §3 Absatz 1.2. der \"Rahmenordnung zur Prävention gegen sexualisierte Gewalt an Minderjährigen und schutz- oder hilfsbedürftigen Erwachsenen im Bistum Speyer\", versichere ich, " + person.getName() + ", hiermit, dass ich nicht wegen einer Straftat im Zusammenhang mit sexualisierter Gewalt (gem. §72a SGB VIII genannte Straftaten) rechtskräftig verurteilt worden bin und auch insoweit keine Ermittlungsverfahren gegen mich eingeleitet worden ist.")
                .addText("Für den Fall, dass diesbezüglich ein Ermittlungsverfahren gegen mich eingeleitet wird, verpflichte ich mich, dies meinem Dienstvorgesetzten bzw. der Person, die mich zu meiner Tätigkeit beauftragt hat, umgehend mitzuteilen.")
                .addAttribute("Name", person.getName())
                .addAttribute("Geburtsdatum", person.getBirthdate().get().getGermanDate())
                .addAttribute("Tätigkeit", person.getRank().getName())
                .addAttribute("Rechtsträger", "Pfarrei Hl. Hildegard von Bingen / " + personHelper.getTenantName(person).orElse(""))
                .setSignature(signature)
                .createPdf();

        Task task = cache.getTaskQueryService().getNormedTaskById(person.getId(), "OB4").orElseThrow();
        TaskMessage taskMessage = new TaskMessage(UUID.randomUUID(), MessageType.ENDE, "Onboarding-Task SAE wurde beendet.", "", MessageInformationCascade.C0, CMSDate.current(), Optional.ofNullable(person.getId()), false);
        cache.getTaskMessageService().saveMessage(task.getTaskId(), taskMessage);
        task.setTaskState(TaskState.COMPLETED);
        task.setEndDate(Optional.of(CMSDate.current()));
        task.setUpdateDate(CMSDate.current());
        cache.getTaskService().saveTask(task);

        return new RedirectView("/onboarding?statusState=" + StatusState.SAE_OK);
    }

    @PostMapping("/onboarding/emergency_contact/create")
    public RedirectView createEmergencyContact(@RequestParam("type") String type, @RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname,
                                               @RequestParam("phone") String phone, @RequestParam("email") Optional<String> email) throws SQLException {
        Person person = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        EmergencyContact emergencyContact = new EmergencyContact(UUID.randomUUID(), type, firstname, lastname, phone, email.orElse("Nicht angegeben"), true);
        cache.getEmergencyContactService().saveEmergencyContact(person.getId(), emergencyContact);

        return new RedirectView("/onboarding?q=EMERGENCY_CONTACT");

    }

    @GetMapping("/onboarding/emergency_contact/submit")
    public RedirectView submitContact() throws SQLException, DocumentException, IOException {
        Person person = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        FileCreator fileCreator = new FileCreator()
                .setReceiver("Leitungsteam", personHelper.getTenantName(person).orElse(""), "Hintere Straße 1", "76756 Bellheim")
                .setGreeting("Leitungsteam " + personHelper.getTenantName(person).orElse(""))
                .setSender(person, personHelper.getTenantName(person).orElse(""))
                .setFolder("person", person.getId().toString())
                .setFileName("contacts.pdf")
                .setSubject("Notfallkontakte")
                .addText("folgende Kontakte wurden hinterlegt:");

        for (EmergencyContact contact : cache.getEmergencyContactService().getEmergencyContactsByPerson(person.getId())) {
            fileCreator.addAttribute(contact.getFirstName() + " " + contact.getLastName() + " (" + contact.getType() + ")", contact.getMail() + " " + contact.getPhoneNumber());
        }
        fileCreator.createPdf();
        person.setOb2(true);
        cache.getPersonService().updatePerson(person);

        Task task = cache.getTaskQueryService().getNormedTaskById(person.getId(), "OB2").orElseThrow();
        TaskMessage taskMessage = new TaskMessage(UUID.randomUUID(), MessageType.ENDE, "Onboarding-Task Notfallkontakt wurde beendet.", "", MessageInformationCascade.C0, CMSDate.current(), Optional.ofNullable(person.getId()), false);
        cache.getTaskMessageService().saveMessage(task.getTaskId(), taskMessage);
        task.setTaskState(TaskState.COMPLETED);
        task.setEndDate(Optional.of(CMSDate.current()));
        task.setUpdateDate(CMSDate.current());
        cache.getTaskService().saveTask(task);

        return new RedirectView("/onboarding?statusState=" + StatusState.CONTACT_OK);
    }

    @GetMapping("/onboarding/contact/delete")
    public RedirectView deleteContact(@RequestParam("id") UUID id) throws SQLException, DocumentException, IOException {

        cache.getEmergencyContactService().deleteEmergencyContact(id);

        return new RedirectView("/onboarding?q=EMERGENCY_CONTACT");
    }

}
