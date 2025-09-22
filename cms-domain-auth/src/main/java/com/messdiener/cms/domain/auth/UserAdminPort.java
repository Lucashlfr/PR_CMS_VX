package com.messdiener.cms.domain.auth;

import java.util.List;

public interface UserAdminPort {
    void initializeUsersAndPermissions(List<UserCredential> users);
}