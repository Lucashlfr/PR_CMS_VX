package com.messdiener.cms.v3.app.entities.worship;

import com.messdiener.cms.v3.shared.enums.LiturgieState;
import com.messdiener.cms.v3.utils.time.CMSDate;
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
