package com.messdiener.cms.domain.person;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PersonLoginDTO {

    private String username;
    private String password;

}
