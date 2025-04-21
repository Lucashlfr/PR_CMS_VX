package com.messdiener.cms.v3.app.entities.user.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserConfigurations {

    private List<UserConfiguration> configurations;

    private UserConfigurations(List<UserConfiguration> configurations) {
        this.configurations = configurations;
    }

    public static UserConfigurations of(List<UserConfiguration> configurations) {
        return new UserConfigurations(configurations);
    }

    public static UserConfigurations empty() {
        return UserConfigurations.of(new ArrayList<>());
    }

    public String getValue(String name) {
        return configurations.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(UserConfiguration.empty())
                .getValue();
    }
}
