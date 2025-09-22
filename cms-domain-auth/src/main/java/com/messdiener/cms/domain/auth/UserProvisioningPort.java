package com.messdiener.cms.domain.auth;

import com.messdiener.cms.domain.person.PersonLoginDTO;

import java.util.List;

public interface UserProvisioningPort {
    void createSingleUser(PersonLoginDTO login);
    void initializeUsersAndPermissions(List<PersonLoginDTO> persons);
}