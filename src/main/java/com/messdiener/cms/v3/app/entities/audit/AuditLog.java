package com.messdiener.cms.v3.app.entities.audit;

import com.messdiener.cms.v3.shared.enums.ActionCategory;
import com.messdiener.cms.v3.shared.enums.MessageInformationCascade;
import com.messdiener.cms.v3.shared.enums.MessageType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class AuditLog {

    private UUID logId;
    private MessageType type;

    private ActionCategory category;
    private UUID connectedId;

    private UUID userId;
    private CMSDate timestamp;

    private String title;
    private String description;
    private MessageInformationCascade mic;

    private boolean file;

    public static AuditLog of(MessageType type, ActionCategory category, UUID connectedId, UUID userId, String title, String description) {
        return new AuditLog(UUID.randomUUID(), type, category, connectedId, userId, CMSDate.current(), title, description, MessageInformationCascade.C0, false);
    }

}
