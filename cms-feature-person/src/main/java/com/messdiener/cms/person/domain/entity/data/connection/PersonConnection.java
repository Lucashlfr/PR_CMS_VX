package com.messdiener.cms.person.domain.entity.data.connection;

import com.messdiener.cms.shared.enums.PersonAttributes;
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
