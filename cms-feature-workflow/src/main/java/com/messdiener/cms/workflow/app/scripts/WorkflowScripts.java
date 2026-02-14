package com.messdiener.cms.workflow.app.scripts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.messdiener.cms.domain.person.PersonSessionView;
import com.messdiener.cms.person.app.helper.PersonHelper;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.person.domain.entity.data.EmergencyContact;
import com.messdiener.cms.person.persistence.service.EmergencyContactService;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.privacy.domain.entity.PrivacyPolicy;
import com.messdiener.cms.privacy.persistence.service.PrivacyService;
import com.messdiener.cms.shared.cache.Cache;
import com.messdiener.cms.shared.enums.document.FileType;
import com.messdiener.cms.shared.enums.workflow.CMSState;
import com.messdiener.cms.utils.other.JsonHelper;
import com.messdiener.cms.utils.time.CMSDate;
import com.messdiener.cms.utils.time.DateUtils;
import com.messdiener.cms.web.common.security.SecurityHelper;
import com.messdiener.cms.workflow.domain.entity.Workflow;
import com.messdiener.cms.workflow.domain.entity.WorkflowForm;
import com.messdiener.cms.workflow.domain.form.WFApproval;
import com.messdiener.cms.workflow.domain.form.WFSae;
import com.messdiener.cms.workflow.persistence.service.FormService;
import com.messdiener.cms.workflow.persistence.service.WorkflowService;
import com.messdiener.cms.files.app.export.FileCreator;
import com.messdiener.cms.files.domain.entity.StorageFile;
import com.messdiener.cms.files.persistence.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowScripts {

    private final SecurityHelper securityHelper;
    private final FormService formService;
    private final PersonService personService;
    private final EmergencyContactService emergencyContactService;
    private final PrivacyService privacyService;
    private final WorkflowService workflowService;
    private final PersonHelper personHelper;
    private final StorageService storageService;

    public void preApproval() { }

    public void postApproval(WFApproval form) {
        form.setState(CMSState.COMPLETED);
        formService.saveForm(form);
    }

    public void preData(WorkflowForm module) {
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        Map<String, String> params = new HashMap<>();
        params.put("firstname", user.getFirstName());
        params.put("lastname", user.getLastName());
        params.put("phone", user.getPhone());
        params.put("mobile", user.getMobile());
        params.put("street", user.getStreet());
        params.put("number", user.getHouseNumber());
        params.put("plz", user.getPostalCode());
        params.put("city", user.getCity());

        String d = user.getBirthdate().isPresent()
                ? user.getBirthdate().get().getEnglishDate()
                : CMSDate.of(-1).getEnglishDate();
        params.put("birthday", d);
        params.put("comment", "");
        params.put("mail", user.getEmail());

        String json = JsonHelper.buildNameValueJson(params);
        module.setResult(json);
        formService.updateWorkflowModule(module);
    }

    public void postData(WorkflowForm module) throws SQLException {
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        Map<String, String> params = JsonHelper.parseJsonToParams(module.getResults());

        user.setFirstName(params.getOrDefault("firstname", user.getFirstName()));
        user.setLastName(params.getOrDefault("lastname", user.getLastName()));
        user.setPhone(params.getOrDefault("phone", user.getPhone()));
        user.setMobile(params.getOrDefault("mobile", user.getMobile()));
        user.setStreet(params.getOrDefault("street", user.getStreet()));
        user.setHouseNumber(params.getOrDefault("number", user.getHouseNumber()));
        user.setPostalCode(params.getOrDefault("plz", user.getPostalCode()));
        user.setCity(params.getOrDefault("city", user.getCity()));
        user.setEmail(params.getOrDefault("mail", user.getEmail()));

        String birthdayString = params.get("birthday");
        if (birthdayString != null && !birthdayString.isEmpty()) {
            user.setBirthdate(Optional.of(CMSDate.convert(birthdayString, DateUtils.DateType.ENGLISH)));
        } else if (user.getBirthdate().isEmpty()) {
            user.setBirthdate(Optional.empty());
        }
        user.setNotes(params.getOrDefault("comment", ""));
        personService.updatePerson(user);

        createPDF(module);
    }

    public void preEmergency(WorkflowForm module) {
        List<EmergencyContact> contacts = emergencyContactService
                .getEmergencyContactsByPerson(module.getCurrentUser());

        List<Map<String, String>> contactMaps = contacts.stream()
                .map(c -> Map.of(
                        "Vorname", c.getFirstName(),
                        "Nachname", c.getLastName(),
                        "Telefon-Nr", c.getPhoneNumber(),
                        "E-Mail", c.getMail()
                ))
                .collect(Collectors.toList());

        ObjectMapper mapper = new ObjectMapper();
        String contactsArrayJson;
        try {
            contactsArrayJson = mapper.writeValueAsString(contactMaps);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Fehler beim Serialisieren der Notfallkontakte",
                    e
            );
        }

        Map<String, String> params = new HashMap<>();
        params.put("emergencyContacts", contactsArrayJson);
        String resultJson = JsonHelper.buildNameValueJson(params);

        module.setResult(resultJson);
        formService.updateWorkflowModule(module);
    }

    public void postEmergency(WorkflowForm module) throws SQLException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        emergencyContactService.deleteEmergencyContactsByUser(module.getCurrentUser());

        String contactsJson = JsonHelper.getValueFromJson(module.getResults(), "emergencyContacts");
        if (contactsJson == null || contactsJson.isEmpty()) {
            return;
        }

        JsonNode arrayNode = objectMapper.readTree(contactsJson);

        for (JsonNode c : arrayNode) {
            String firstName = c.path("Vorname").asText();
            String lastName = c.path("Nachname").asText();
            String phoneNumber = c.path("Telefon-Nr").asText();
            String email = c.path("E-Mail").asText();

            EmergencyContact contact = new EmergencyContact(
                    UUID.randomUUID(),
                    "parents",
                    firstName,
                    lastName,
                    phoneNumber,
                    email,
                    true
            );
            emergencyContactService.saveEmergencyContact(module.getCurrentUser(), contact);
        }

        // person updaten (falls gewünscht) – Domänen-Person laden
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));
        personService.updatePerson(user);

        createPDF(module);
    }

    public void prePrivacy() { }

    public void postPrivacy(WorkflowForm module) throws SQLException {
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        PrivacyPolicy privacyPolicy = new PrivacyPolicy(
                module.getCurrentUser(),
                CMSDate.current(),
                user.getFirstName(),
                user.getLastName(),
                user.getStreet(),
                user.getHouseNumber(),
                user.getPostalCode(),
                user.getCity(),
                JsonHelper.hasNameInJson(module.getResults(), "check1"),
                JsonHelper.hasNameInJson(module.getResults(), "check2"),
                JsonHelper.hasNameInJson(module.getResults(), "check3"),
                JsonHelper.hasNameInJson(module.getResults(), "check4"),
                JsonHelper.hasNameInJson(module.getResults(), "check5"),
                JsonHelper.hasNameInJson(module.getResults(), "check6"),
                JsonHelper.hasNameInJson(module.getResults(), "check7"),
                JsonHelper.getValueFromJson(module.getResults(), "signature")
        );
        privacyService.create(privacyPolicy);

        personService.updatePerson(user);
        createPDF(module);
    }

    public void preInformation(WorkflowForm module) throws SQLException {
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        Workflow workflow = workflowService.getWorkflowById(
                formService.getWorkflowId(module.getModuleId())
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));

        if (workflow.getCurrentNumber() != module.getNumber()) return;

        module.setState(CMSState.SKIPPED);
        formService.saveForm(module);

        workflow.setCurrentNumber(workflow.getCurrentNumber() + 1);
        workflowService.saveWorkflow(workflow);

        WorkflowForm nextModule = formService.getModuleByNumber(
                workflow.getWorkflowId(), workflow.getCurrentNumber()
        ).orElseThrow(() -> new IllegalArgumentException("WorkflowModule not found"));
        nextModule.setState(CMSState.OPEN);
        formService.saveForm(nextModule);
    }

    public void preSAE(WorkflowForm module) { }

    public void postSae(WFSae wfSae) throws SQLException {
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));
        personService.updatePerson(user);

        createPDF(wfSae);
    }

    private void createPDF(WorkflowForm module) throws SQLException {
        PersonSessionView sessionUser = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        FileCreator fileCreator = new FileCreator().setFolder("person", user.getId().toString())
                .setFileName("wf_" + module.getName() + ".pdf")
                .setSubject("[WF] " + module.getName())
                .setInfoline("-", module.getModuleId().toString(), personHelper.getName(user.getPrincipal()), "-")
                .setReceiver(user, user.getTenant())
                .addText("Dieses Dokument informiert Sie über die Änderungen, die Sie im Rahmen des aktuellen Workflows vorgenommen haben. Sie finden hier eine Zusammenfassung der von Ihnen aktualisierten oder hinzugefügten Daten sowie der abgegebenen Bestätigungen.");

        Map<String, String> map = JsonHelper.parseJsonToParams(module.getResults());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            fileCreator.addAttribute(entry.getKey(), entry.getValue());
        }

        fileCreator.setSender(personService.getPersonById(Cache.SYSTEM_USER).orElseThrow(), "technischer Benutzer");
        try {
            Optional<File> file = fileCreator.createPdf();
            storageService.store(new StorageFile(
                    UUID.randomUUID(), 0, user.getId(), user.getId(),
                    CMSDate.current(), fileCreator.getFileName(),
                    CMSDate.current(), 0, FileType.ONBOARDING,
                    file.orElseThrow().getPath()
            ));
        } catch (IOException | DocumentException e) {
            throw new RuntimeException(e);
        }
    }
}
