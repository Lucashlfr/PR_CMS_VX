package com.messdiener.cms.v3.app.scripts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.messdiener.cms.v3.app.entities.document.StorageFile;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.data.EmergencyContact;
import com.messdiener.cms.v3.app.entities.privacy.PrivacyPolicy;
import com.messdiener.cms.v3.app.entities.workflow.Workflow;
import com.messdiener.cms.v3.app.entities.workflow.WorkflowModule;
import com.messdiener.cms.v3.app.entities.workflow.repository.form.WFPrivacyPolicy;
import com.messdiener.cms.v3.app.entities.workflow.repository.form.WFSae;
import com.messdiener.cms.v3.app.entities.workflow.repository.scripts.WFInformation;
import com.messdiener.cms.v3.app.export.FileCreator;
import com.messdiener.cms.v3.app.helper.person.PersonHelper;
import com.messdiener.cms.v3.app.helper.workflow.WorkflowHelper;
import com.messdiener.cms.v3.app.services.document.StorageService;
import com.messdiener.cms.v3.app.services.person.EmergencyContactService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.privacy.PrivacyService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowModuleService;
import com.messdiener.cms.v3.app.services.workflow.WorkflowService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.document.FileType;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowModuleStatus;
import com.messdiener.cms.v3.utils.other.JsonHelper;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import lombok.Getter;
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
    private final WorkflowModuleService workflowModuleService;
    private final PersonService personService;
    private final EmergencyContactService emergencyContactService;
    private final PrivacyService privacyService;
    private final WorkflowHelper workflowHelper;
    private final WorkflowService workflowService;
    private final PersonHelper personHelper;
    private final StorageService storageService;

    public void preApproval() {

    }


    public void postApproval() {

    }

    public void preData(WorkflowModule module) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Map<String, String> params = new HashMap<>();
        params.put("firstname", user.getFirstname());
        params.put("lastname", user.getLastname());
        params.put("phone", user.getPhone());
        params.put("mobile", user.getMobile());
        params.put("street", user.getStreet());
        params.put("number", user.getHouseNumber());
        params.put("plz", user.getPostalCode());
        params.put("city", user.getCity());

        String d = user.getBirthdate().isPresent() ? user.getBirthdate().get().getEnglishDate() : CMSDate.of(-1).getEnglishDate();
        params.put("birthday", d);
        params.put("comment", "");
        params.put("mail", user.getEmail());

        String json = JsonHelper.buildNameValueJson(params);
        module.setResult(json);
        workflowModuleService.updateWorkflowModule(module);
    }


    public void postData(WorkflowModule module) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Map<String, String> params = JsonHelper.parseJsonToParams(module.getResults());

        user.setFirstname(params.getOrDefault("firstname", user.getFirstname()));
        user.setLastname(params.getOrDefault("lastname", user.getLastname()));
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
        user.setOb1(true);
        personService.updatePerson(user);

        createPDF(module);
    }


    public void preEmergency(WorkflowModule module) throws SQLException {

        List<EmergencyContact> contacts = emergencyContactService
                .getEmergencyContactsByPerson(module.getOwner());

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

        workflowModuleService.updateWorkflowModule(module);
    }


    public void postEmergency(WorkflowModule module) throws SQLException, JsonProcessingException {
        System.out.println("post emergency");

        ObjectMapper objectMapper = new ObjectMapper();

        emergencyContactService.deleteEmergencyContactsByUser(module.getOwner());

        // Extract the raw JSON string for emergencyContacts
        String contactsJson = JsonHelper.getValueFromJson(module.getResults(), "emergencyContacts");
        if (contactsJson == null || contactsJson.isEmpty()) {
            return; // Keine Kontakte vorhanden
        }

        // Parse the contacts array
        JsonNode arrayNode = objectMapper.readTree(contactsJson);

        for (JsonNode c : arrayNode) {
            String firstName = c.path("Vorname").asText();
            String lastName = c.path("Nachname").asText();
            String phoneNumber = c.path("Telefon-Nr").asText();
            String email = c.path("E-Mail").asText();

            EmergencyContact contact = new EmergencyContact(
                    UUID.randomUUID(),      // neue UUID generieren
                    "parents",           // Typ festlegen
                    firstName,
                    lastName,
                    phoneNumber,
                    email,
                    true                    // aktiv
            );
            emergencyContactService.saveEmergencyContact(module.getOwner(), contact);

            Person user = securityHelper.getPerson()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
            user.setOb3(true);
            personService.updatePerson(user);
        }

        createPDF(module);

    }

    public void prePrivacy() {
    }


    public void postPrivacy(WorkflowModule module) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        PrivacyPolicy privacyPolicy = new PrivacyPolicy(module.getOwner(), CMSDate.current(), user.getFirstname(), user.getLastname(), user.getStreet(), user.getHouseNumber(), user.getPostalCode(), user.getCity(), JsonHelper.hasNameInJson(module.getResults(), "check1"),JsonHelper.hasNameInJson(module.getResults(), "check2"), JsonHelper.hasNameInJson(module.getResults(), "check3"), JsonHelper.hasNameInJson(module.getResults(), "check4"), JsonHelper.hasNameInJson(module.getResults(), "check5"), JsonHelper.hasNameInJson(module.getResults(), "check6"), JsonHelper.hasNameInJson(module.getResults(), "check7"), JsonHelper.getValueFromJson(module.getResults(), "signature"));
        privacyService.create(privacyPolicy);

        user.setOb2(true);
        personService.updatePerson(user);

        createPDF(module);
    }

    public void preInformation(WorkflowModule module) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Workflow workflow = workflowService.getWorkflowById(workflowModuleService.getWorkflowId(module.getModuleId())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));

        if(workflow.getCurrentNumber() != module.getNumber())return;

        module.setStatus(WorkflowModuleStatus.SKIPPED);
        module.setResult(workflowHelper.createUrl(user, module, workflow));
        workflowModuleService.saveWorkflowModule(workflow.getWorkflowId(), module);

        workflow.setCurrentNumber(workflow.getCurrentNumber() + 1);
        workflowService.saveWorkflow(workflow);

        WorkflowModule nextModule = workflowModuleService.getModuleByNumber(workflow.getWorkflowId(), workflow.getCurrentNumber()).orElseThrow(() -> new IllegalArgumentException("WorkflowModule not found"));
        nextModule.setStatus(WorkflowModuleStatus.OPEN);
        workflowModuleService.saveWorkflowModule(workflow.getWorkflowId(),nextModule);


    }

    public void preSAE(WorkflowModule module) {
    }

    public void postSae(WFSae wfSae) throws SQLException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        user.setOb4(true);
        personService.updatePerson(user);

        createPDF(wfSae);
    }

    private void createPDF(WorkflowModule module) throws SQLException {

        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        FileCreator fileCreator = new FileCreator().setFolder("person", user.getId().toString())
                .setFileName("wf_" + module.getName() + ".pdf")
                .setSubject("[WF] " + module.getName())
                .setInfoline("-", module.getModuleId().toString(), personHelper.getName(user.getPrincipal()), "-")
                .setReceiver(user, personHelper.getTenantName(user).orElse(""))
                .addText("Dieses Dokument informiert Sie über die Änderungen, die Sie im Rahmen des aktuellen Workflows vorgenommen haben. Sie finden hier eine Zusammenfassung der von Ihnen aktualisierten oder hinzugefügten Daten sowie der abgegebenen Bestätigungen.");

        Map<String, String> map = JsonHelper.parseJsonToParams(module.getResults());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            fileCreator.addAttribute(entry.getKey(), entry.getValue());
        }

        fileCreator.setSender(personService.getPersonById(Cache.SYSTEM_USER).orElseThrow(), "technischer Benutzer");
        try {
            Optional<File> file = fileCreator.createPdf();
            storageService.store(new StorageFile(UUID.randomUUID(), 0, user.getId(), user.getId(), CMSDate.current(), fileCreator.getFileName(), CMSDate.current(), 0, FileType.ONBOARDING, file.orElseThrow().getPath()));
        } catch (IOException | DocumentException e) {
            throw new RuntimeException(e);
        }

    }

}
