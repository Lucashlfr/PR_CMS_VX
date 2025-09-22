package com.messdiener.cms.person.domain.entity.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class EmergencyContact {

    private UUID contactId;
    private String type;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String mail;
    private boolean active;
}
