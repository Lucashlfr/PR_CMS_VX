package com.messdiener.cms.v3.web.controller.workflow;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.enums.workflow.WorkflowType;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WorkflowCreateController {

    private final SecurityHelper securityHelper;
    private final PersonService personService;

    @GetMapping("/workflow/wizard")
    public String showWizardStep1(Model model, HttpSession session, @RequestParam("type") Optional<WorkflowType> typeOpt) throws SQLException {
        Person person = securityHelper.addPersonToSession(session).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        model.addAttribute("types", WorkflowType.values());
        model.addAttribute("persons", personService.getActivePersonsByPermissionDTO(person.getFRank(),person.getTenantId()));

        WorkflowType workflowType = typeOpt.orElse(WorkflowType.NULL);
        model.addAttribute("step", 1);
        model.addAttribute("workflowType", workflowType);
        return "workflow/interface/createWorkflow";
    }

    @GetMapping(value = "/workflow/wizard", params = "persons")
    public String processPersonsSelection(Model model,HttpSession session,@RequestParam("type") WorkflowType workflowType,@RequestParam("persons") List<UUID> personIds) throws SQLException {
        Person person = securityHelper.addPersonToSession(session).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        session.setAttribute("selectedPersonIds", personIds);

        model.addAttribute("step", 2);
        model.addAttribute("workflowType", workflowType);
        model.addAttribute("selectedPersons", personService.findAllByIds(personIds));

        return "workflow/interface/createWorkflow";
    }


}
