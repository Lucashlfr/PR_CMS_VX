package com.messdiener.cms.events.app.utils;

public class SlugUtil {

    private SlugUtil() {}

    public static String toSlug(String title) {
        return title
                .trim()
                .replace(" ", "-");
    }
}
