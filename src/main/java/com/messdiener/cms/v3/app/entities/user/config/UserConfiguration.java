package com.messdiener.cms.v3.app.entities.user.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserConfiguration {

    public final String name;
    public final String value;

    public static UserConfiguration empty() {
        return new UserConfiguration("", "");
    }
}
