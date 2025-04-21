package com.messdiener.cms.v3.app.entities.person.data.connection;

import com.messdiener.cms.v3.shared.enums.PersonAttributes;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PersonConnection {

	private UUID id;
	private UUID host;
	private UUID sub;
	private PersonAttributes.Connection connectionType;
}
