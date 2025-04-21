package com.messdiener.cms.v3.app.entities.user;

import com.messdiener.cms.v3.shared.cache.Cache;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Data
@AllArgsConstructor(staticName = "of")
public class Permission {

    private final String name;
    private final String type;
    private final String description;
    private final int id;

    public static List<Permission> getDefaultPermissions() {
        return List.of(
                of("ORG", "Group", "Personen mit Rechte auf Pfarreiebene", 1),
                of("TEAM", "Group", "Personen mit Rechte auf Ortsebene", 2),
                of("*", "Administration", "Benutzer besitzt alle Rechte", 3)
        );
    }
}
