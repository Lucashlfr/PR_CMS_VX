package com.messdiener.cms.domain.person;

import java.util.Optional;
import java.util.UUID;

public interface PersonQueryPort {
    Optional<PersonRef> findRefById(UUID personId);
}
