package com.messdiener.cms.v3.web.request;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class PermissionsForm {

	private UUID id;
	private List<String> selectedPermissions;

	// Keine Logik / Serviceaufrufe mehr!
}
