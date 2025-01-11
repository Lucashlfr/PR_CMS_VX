package com.messdiener.cms.v3.app.entities.user;

import com.messdiener.cms.v3.shared.cache.Cache;
import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Permission {

    private final String name;
    private final String type;
    private final String description;
    private final int id;


    public boolean userHasPermission(UUID id) throws SQLException {
        return Cache.getPersonService().getPersonById(id).orElseThrow().hasPermission(name);
    }

    public static Permission of(String name, String type, String description, int id){
        return new Permission(name, type, description, id);
    }

    public static List<Permission> getDefaultPermissions(){
        List<Permission> permissions = new ArrayList<>();
        permissions.add(of("change_mandat", "home", "Benutzer kann das Mandat wechseln",1));
        permissions.add(of("mp_module_personal", "Personal","Benutzer hat Zugriff auf das Modul Personal",2));
        permissions.add(of("mg_module_gottesdienste", "Gottesdienste","Benutzer kann auf das Modul Gottesdienste zugreifen",3));
        permissions.add(of("mg_module_finanzen", "Finanzen","Benutzer kann auf das Modul Finanzen zugreifen",4));
        permissions.add(of("mg_module_events", "Termine","Benutzer kann auf das Modul Termine zugreifen",5));
        permissions.add(of("*", "Administration","Benutzer besitzt alle Rechte",16));
        return permissions;
    }

    public void save() throws SQLException {
        Cache.getPermissionService().createPermission(this);
    }
}
