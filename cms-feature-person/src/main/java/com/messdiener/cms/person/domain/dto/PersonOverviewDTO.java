package com.messdiener.cms.person.domain.dto;

import com.messdiener.cms.shared.enums.tenant.Tenant;
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
