package com.messdiener.cms.domain.liturgy;

import com.messdiener.cms.shared.enums.LiturgieState;

import java.util.UUID;

/** Kompaktes DTO für die Personen-States zu einer Liturgie. */
public record PersonStateView(
        UUID personId,
        String firstName,
        String lastName,
        LiturgieState state
) {}
