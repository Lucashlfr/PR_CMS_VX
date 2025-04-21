package com.messdiener.cms.v3.web.controller.personal;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.web.request.PermissionsForm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PersonPermissionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonPermissionController.class);
    private final Cache cache;

    @PostConstruct
    public void init() {
        LOGGER.info("PersonPermissionController initialized.");
    }

    @PostMapping("/personal/permission")
    public RedirectView permissions(@ModelAttribute PermissionsForm permissionsForm) throws SQLException {
        Person person = cache.getPersonService().getPersonById(permissionsForm.getId()).orElseThrow();
        List<String> selectedPermissions = permissionsForm.getSelectedPermissions();

        cache.getUserPermissionMappingService().removeAllPermissionFromUser(person.getId());
        for(String permission : selectedPermissions) {
            cache.getUserPermissionMappingService().mapPermissions(person,permission, true);
        }

        return new RedirectView("/personal?q=permissions&id=" + permissionsForm.getId());
    }

    @GetMapping("/personal/permissions/remove")
    public RedirectView remove(@RequestParam("id") UUID id, @RequestParam("p") String name) throws SQLException {
        Person person = cache.getPersonService().getPersonById(id).orElseThrow();

        cache.getUserPermissionMappingService().removeAllPermissionFromUser(person.getId());
        return new RedirectView("/personal?q=permissions&id=" + id);
    }


}
