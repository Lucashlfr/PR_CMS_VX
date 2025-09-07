package com.messdiener.cms.v3.app.entities.person.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PersonLoginDTO {

    private String username;
    private String password;

}
