package com.messdiener.cms.workflow.domain.entity.data;

import com.messdiener.cms.shared.enums.workflow.CMSState;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Notification {

    private UUID notificationId;
    private UUID templateId;
    private CMSState state;

    private Map<String, Object> payload;
}

