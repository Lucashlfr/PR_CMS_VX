package com.messdiener.cms.events.web.controller;

import com.messdiener.cms.domain.documents.StorageQueryPort;
import com.messdiener.cms.events.app.service.EventMessageService;
import com.messdiener.cms.events.app.service.EventService;
import com.messdiener.cms.events.app.service.PreventionService;
import com.messdiener.cms.person.app.helper.PersonHelper;
import com.messdiener.cms.utils.other.ImgUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class EventFragmentController {

    private final EventService eventService;
    private final PreventionService preventionService;
    private final EventMessageService eventMessageService;
    private final PersonHelper personHelper;
    private final StorageQueryPort storageQueryPort;

    @GetMapping("/event/fragment/application")
    public String applicationFrag(@RequestParam UUID id, Model model) {
        var event = eventService.getEventById(id);
        model.addAttribute("event", event.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Event not found")));
        return "event/fragments/application :: application";
    }

    @GetMapping("/event/fragment/notes")
    public String notesFrag(@RequestParam UUID id, Model model) {
        var event = eventService.getEventById(id);
        model.addAttribute("event", event.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Event not found")));
        return "event/fragments/notes :: notes";
    }

    @GetMapping("/event/fragment/press")
    public String pressFrag(@RequestParam UUID id, Model model) {
        var event = eventService.getEventById(id);
        model.addAttribute("event", event.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Event not found")));
        return "event/fragments/press :: press";
    }

    @GetMapping("/event/fragment/risk")
    public String riskFrag(@RequestParam UUID id, Model model) {
        var event = eventService.getEventById(id);
        model.addAttribute("event", event.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Event not found")));
        model.addAttribute("preventionForm", preventionService.getPreventionForm(id));
        return "event/fragments/risk :: risk";
    }

    @GetMapping("/event/fragment/messages")
    public String messagesFrag(@RequestParam UUID id, Model model) {
        model.addAttribute("messages", eventMessageService.getItems(id));
        model.addAttribute("personHelper", personHelper);
        return "event/fragments/messages :: messages";
    }

    // optional:
    @GetMapping("/event/fragment/files")
    public String filesFrag(@RequestParam UUID id, Model model) {
        var event = eventService.getEventById(id);
        model.addAttribute("event", event.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Event not found")));
        model.addAttribute("files", storageQueryPort.getFiles(id));
        model.addAttribute("personHelper", personHelper);
        model.addAttribute("imgUtils", new ImgUtils());
        return "event/fragments/files :: files";
    }


}
