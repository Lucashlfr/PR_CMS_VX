package com.messdiener.cms.liturgy.domain.entity;

import com.messdiener.cms.shared.enums.LiturgieState;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Data
public class LiturgieView {

    private UUID id;
    private String typeLabel;
    private CMSDate date;
    private Map<UUID, LiturgieState> personStates;

}
