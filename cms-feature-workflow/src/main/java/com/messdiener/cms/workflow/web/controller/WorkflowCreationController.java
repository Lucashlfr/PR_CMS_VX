package com.messdiener.cms.workflow.web.controller;

import com.messdiener.cms.domain.person.PersonSessionView;
import com.messdiener.cms.person.domain.dto.PersonOverviewDTO;
import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.person.persistence.service.PersonService;
import com.messdiener.cms.shared.enums.workflow.WorkflowType;
import com.messdiener.cms.web.common.security.SecurityHelper;
import com.messdiener.cms.workflow.persistence.service.WorkflowCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WorkflowCreationController {

    private final SecurityHelper securityHelper;
    private final PersonService personService;
    private final WorkflowCreationService workflowCreationService;

    @PostMapping("/workflow/create")
    public RedirectView create(@RequestParam("type") WorkflowType type,@RequestParam("person") String person) {

        PersonSessionView sessionUser = securityHelper.getPerson().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Person user = personService.getPersonById(sessionUser.id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Person not found"));

        List<UUID> targetIds = new ArrayList<>();
        switch (person) {
            case "all" -> targetIds.addAll(personService.getActivePersonsByPermissionDTO(user.getFRank(), user.getTenant()).stream().map(PersonOverviewDTO::getId).toList());
            case "tenant" -> targetIds.addAll(personService.getActiveMessdienerByTenantDTO(user.getTenant()).stream().map(PersonOverviewDTO::getId).toList());
            default -> targetIds.add(user.getId());
        }

        workflowCreationService.createWorkflow(targetIds, user.getId(), user.getId(), type);
        return new RedirectView("/workflow");
    }

}
