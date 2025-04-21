package com.messdiener.cms.v3.app.entities.tasks.message;

import com.messdiener.cms.v3.shared.enums.tasks.MessageInformationCascade;
import com.messdiener.cms.v3.shared.enums.tasks.MessageType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Data
public class TaskMessage {

    private UUID messageId;
    private MessageType messageType;
    private String messageTitle;
    private String messageDescription;
    private MessageInformationCascade messageInformationCascade;
    private CMSDate date;
    private Optional<UUID> creatorId;
    private boolean file;

}
