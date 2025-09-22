package com.messdiener.cms.domain.privacy;

import java.util.*;
import java.util.UUID;

public interface PrivacyQueryPort {
    Optional<PrivacyView> getById(UUID personId);

    List<PrivacyView> getAll();
}
