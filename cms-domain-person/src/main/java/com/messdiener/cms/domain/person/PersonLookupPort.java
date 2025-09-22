package com.messdiener.cms.domain.person;

import java.util.Optional;

public interface PersonLookupPort {
    Optional<PersonSessionView> findByUsernameForSession(String username);
}
