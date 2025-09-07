package com.messdiener.cms.v3.app.entities.person.dto;

import com.messdiener.cms.v3.shared.enums.tenant.Tenant;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PersonOverviewDTO {

    private UUID id;
    private String firstName;
    private String lastName;
    private Tenant tenant;
    private String rank;
    private String birthdate;
    private double[] activity;
    private String imgUrl;
    private String username;
    private String password;
}
