package com.messdiener.cms.v3.app.entities.tenant;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Tenant {

    private UUID id;
    private String name;
    private String color;
    private String initial;
}
