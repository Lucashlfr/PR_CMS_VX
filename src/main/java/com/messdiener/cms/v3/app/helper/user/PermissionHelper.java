package com.messdiener.cms.v3.app.helper.user;

import com.messdiener.cms.v3.app.entities.user.Permission;
import com.messdiener.cms.v3.app.services.user.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class PermissionHelper {

    private final PermissionService permissionService;

    public boolean save(Permission permission) {
        try {
            permissionService.createPermission(permission);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
