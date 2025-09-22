package com.messdiener.cms.domain.person;

public interface PersonPasswordCommandPort {
    void setPasswordAndMarkCustom(String username, String encodedPassword);
}
