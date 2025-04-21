package com.messdiener.cms.v3.app.entities.person.data;

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
