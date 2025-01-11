package com.messdiener.cms.v3.web.request;

import com.messdiener.cms.v3.shared.cache.Cache;
import lombok.Data;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Data
public class PermissionsForm {

	private UUID id;
	private List <String> selectedPermissions;

	public boolean userHasPermission(UUID id, String name) throws SQLException {
		return Cache.getPersonService().getPersonById(id).orElseThrow().hasPermission(name);
	}

}
