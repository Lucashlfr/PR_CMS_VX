// cms-domain-person/.../PersonDirectoryPort.java
package com.messdiener.cms.domain.person;

import java.util.List;
import java.util.UUID;

public interface PersonDirectoryPort {
    List<PersonOverviewLite> getPersonsByTenant(UUID tenantIdOrNull);
}
