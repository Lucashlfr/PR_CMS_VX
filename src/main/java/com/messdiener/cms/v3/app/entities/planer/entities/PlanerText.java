package com.messdiener.cms.v3.app.entities.planer.entities;

import com.messdiener.cms.v3.shared.enums.planer.PlanerTag;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PlanerText {

    private UUID textId;
    private UUID userId;
    private CMSDate creationDate;
    private PlanerTag tag;
    private String identifier;
    private String text;

    public static PlanerText create(UUID userId, PlanerTag tag, String identifier, String text) {
        return new PlanerText(UUID.randomUUID(), userId, CMSDate.current(), tag, identifier, text);
    }

    public int getNumber() {
        int number = 0;
        try {
            number = Integer.parseInt(text);
        }catch (NumberFormatException ignored) {
        }
        return number;
    }
}
