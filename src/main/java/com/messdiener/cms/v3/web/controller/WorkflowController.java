package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.workflows.Workflow;
import com.messdiener.cms.v3.app.entities.workflows.WorkflowLog;
import com.messdiener.cms.v3.app.entities.workflows.request.WorkflowRequest;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.OrganisationType;
import com.messdiener.cms.v3.shared.enums.StatusState;
import com.messdiener.cms.v3.shared.enums.WorkflowAttributes;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
public class WorkflowController {

    @GetMapping("/workflows")
    public String workflows(HttpSession session, Model model, @RequestParam("q")Optional<String> q, @RequestParam("id")Optional<String> id) throws SQLException {
        Person user = SecurityHelper.addPersonToSession(session);
        model.addAttribute("user", user);
        model.addAttribute("workflows", Cache.getWorkflowService().getWorkflowsByUser(user.getId(), WorkflowAttributes.WorkflowState.PENDING));

        String query = q.orElse("null");
        switch (query){
            case "edit":
                if(id.isPresent()){
                    UUID workflowId = UUID.fromString(id.get());
                    Workflow workflow = Cache.getWorkflowService().getWorkflow(workflowId, user.getId()).orElseThrow();
                    model.addAttribute("workflow", workflow);
                    switch (workflow.getWorkflowType()){
                        case SCHEDULER:
                            model.addAttribute("events", Cache.getOrganisationService().getNextEvents(user.getTenantId(), OrganisationType.WORSHIP));
                            return "workflows/pages/workflow_scheduler";
                        case DATA:
                            return "workflows/pages/workflow_data";
                        case PRIVACY_POLICY:
                            return "workflows/pages/workflow_privacy_policy";
                    }
                }
                break;

            case "createScheduler":
                model.addAttribute("persons", Cache.getPersonService().getActiveMessdienerByTenant(user.getTenantId()));
                model.addAttribute("workflowType", WorkflowAttributes.WorkflowType.SCHEDULER.toString());
                return "workflows/interface/create_workflow";

            case "createData":
                model.addAttribute("persons", Cache.getPersonService().getActiveMessdienerByTenant(user.getTenantId()));
                model.addAttribute("workflowType", WorkflowAttributes.WorkflowType.DATA.toString());
                return "workflows/interface/create_workflow";

            case "createPrivacy_policy":
                model.addAttribute("persons", Cache.getPersonService().getActiveMessdienerByTenant(user.getTenantId()));
                model.addAttribute("workflowType", WorkflowAttributes.WorkflowType.PRIVACY_POLICY.toString());
                return "workflows/interface/create_workflow";
        }

        return "workflows/workflowIndex";
    }

    @PostMapping("/workflow/create")
    public ResponseEntity<String> createWorkflow(@RequestBody WorkflowRequest request) throws SQLException {

        Person user = SecurityHelper.getPerson();

        // Validierung der Eingabedaten
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return ResponseEntity.badRequest().body("Start- und Enddatum müssen angegeben werden.");
        }

        if (request.getUuids() == null || request.getUuids().isEmpty()) {
            return ResponseEntity.badRequest().body("Mindestens eine UUID muss ausgewählt sein.");
        }

        System.out.println(request.getType());

        // Verarbeitung der Daten
        UUID workflowId = UUID.randomUUID();
        UUID tenantId = user.getTenantId();
        WorkflowAttributes.WorkflowState workflowState = WorkflowAttributes.WorkflowState.PENDING;
        WorkflowAttributes.WorkflowType workflowType = WorkflowAttributes.WorkflowType.valueOf(request.getType());
        CMSDate creationDate = CMSDate.current();
        CMSDate startDate = CMSDate.convert(request.getStartDate(), DateUtils.DateType.ENGLISH);
        CMSDate endDate = CMSDate.convert(request.getEndDate(), DateUtils.DateType.ENGLISH);
        List<String> uuids = request.getUuids();

        WorkflowLog workflowLog = new WorkflowLog(UUID.randomUUID(), workflowId, creationDate, user.getId(), "Workflow erstellt", user.getName() + " hat den workflow erstellt");
        List<WorkflowLog> logs = List.of(workflowLog);

        for(String u : uuids){
            UUID personId = UUID.fromString(u);
            Workflow workflow = new Workflow(workflowId, tenantId, personId, workflowType, workflowState, creationDate, startDate, endDate, creationDate, user.getId(), logs);
            workflow.create();
        }

        return ResponseEntity.ok("Workflow wurde erfolgreich erstellt mit " + uuids.size() + " ausgewählten Personen.");
    }

    @PostMapping("/workflow/scheduler/submit")
    public RedirectView handleEventSubmission(@RequestParam("id")UUID workflowId, @RequestParam Map<String, String> eventOptions) throws SQLException {
        Person user = SecurityHelper.getPerson();
        Workflow workflow = Cache.getWorkflowService().getWorkflow(workflowId, user.getId()).orElseThrow();

        for (Map.Entry<String, String> entry : eventOptions.entrySet()) {

            if(entry.getValue().contains(workflow.getWorkflowId().toString()))continue;

            String eventIndex = entry.getKey().replace("event_",  "");  // z.B. "event_0"
            int selectedOption = Integer.parseInt(entry.getValue());  // z.B. "option1"

            Cache.getOrganisationService().setMapState(UUID.fromString(eventIndex), user.getId(), selectedOption, 0,0);
        }
        workflow.complete();
        return new RedirectView("/workflows?statusState=" + StatusState.SCHEDULER_OK);

    }

    @PostMapping("/workflow/check/submit")
    public RedirectView save(@RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname, @RequestParam("phone") Optional<String> phone,
                             @RequestParam("mobile") Optional<String> mobile, @RequestParam("mail") Optional<String> mail,
                             @RequestParam("street") Optional<String> street, @RequestParam("number") Optional<String> number, @RequestParam("plz") Optional<String> plz, @RequestParam("town") Optional<String> town,
                             @RequestParam("birthday") Optional<String> birthday, @RequestParam("data") Optional<String> data, @RequestParam("id")UUID workflowId) throws Exception {
        Person person = SecurityHelper.getPerson();
        Workflow workflow = Cache.getWorkflowService().getWorkflow(workflowId, person.getId()).orElseThrow();
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

        person.update();

        workflow.complete();
        return new RedirectView("/workflows?statusState=" + StatusState.CHECK_OK);
    }

    @PostMapping("/workflow/privacy/submit")
    public RedirectView save(@RequestParam("option1") Optional<String> option1, @RequestParam("option2") Optional<String> option2,
                             @RequestParam("option3") Optional<String> option3, @RequestParam("option4") Optional<String> option4,
                             @RequestParam("option5") Optional<String> option5, @RequestParam("option6") Optional<String> option6,
                             @RequestParam("option7") Optional<String> option7, @RequestParam("signature") String signature,
                             @RequestParam("id")String id) throws Exception {

        Person person = SecurityHelper.getPerson();
        UUID workflowId = UUID.fromString(id);

        String data = option1.isPresent() + ";" + option2.isPresent() + ";" + option3.isPresent() + ";" + option4.isPresent() + ";" + option5.isPresent() + ";" + option6.isPresent() + ";" + option7.isPresent();


        Workflow workflow = Cache.getWorkflowService().getWorkflow(workflowId, person.getId()).orElseThrow();

        if (signature.isEmpty()) throw new IllegalStateException();

        person.setPrivacy_policy(data);
        person.setSignature(signature);

        person.update();
        workflow.complete();

        return new RedirectView("/workflows?statusState=" + StatusState.PRIVACY_POLICY_OK);
    }

}
