package com.messdiener.cms.domain.document;

import com.messdiener.cms.shared.enums.ArticleState;
import com.messdiener.cms.shared.enums.ArticleType;
import com.messdiener.cms.utils.time.CMSDate;

import java.util.UUID;

public record ArticleView(
        UUID id,
        int tag,
        UUID creator,
        CMSDate lastUpdate,
        String target,
        ArticleState articleState,
        ArticleType articleType,
        String title,
        String description,
        String imgUrl,
        String form,
        String html
) {
    public static ArticleView empty() {
        return new ArticleView(
                new UUID(0L, 0L),
                0,
                null,
                CMSDate.current(),
                "",
                ArticleState.CREATED,
                ArticleType.BLOG,
                "",
                "",
                "",
                "",
                ""
        );
    }
}
