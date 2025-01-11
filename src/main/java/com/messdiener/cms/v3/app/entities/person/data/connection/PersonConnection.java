package com.messdiener.cms.v3.app.entities.person.data.connection;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.PersonAttributes;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.SQLException;
import java.util.UUID;

@AllArgsConstructor
@Data
public class PersonConnection {

	private UUID id;
	private UUID host;
	private UUID sub;
	private PersonAttributes.Connection connectionType;

	public void save() throws SQLException {
		Cache.getPersonService().createConnection(this);
	}

	public Person getSubPerson() throws SQLException {
		return Cache.getPersonService().getPersonById(sub).orElseThrow();
	}
}
