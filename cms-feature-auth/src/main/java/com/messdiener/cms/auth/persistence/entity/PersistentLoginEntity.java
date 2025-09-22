// X:\workspace\PR_CMS\cms-feature-auth\src\main\java\com\messdiener\cms\auth\persistence\entity\PersistentLoginEntity.java
package com.messdiener.cms.auth.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "persistent_logins")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PersistentLoginEntity {

    @Id
    @Column(name = "series", length = 64, nullable = false)
    private String series;

    @Column(name = "username", length = 64, nullable = false)
    private String username;

    @Column(name = "token", length = 64, nullable = false)
    private String token;

    @Column(name = "last_used", nullable = false)
    private Instant lastUsed; // TIMESTAMP <-> Instant
}
