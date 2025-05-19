package com.messdiener.cms.v3.app.entities.person;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PersonOverviewDTO {

    private UUID id;
    private String firstName;
    private String lastName;
    private String tenantName;
    private String rank;
    private String birthdate;
    private double[] activity;
    private String imgUrl;
}
