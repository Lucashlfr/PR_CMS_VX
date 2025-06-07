package com.messdiener.cms.v3.app.utils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ImgUtils {

    public boolean isImageFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        String lowerCaseName = filename.toLowerCase();

        return lowerCaseName.endsWith(".jpg") ||
                lowerCaseName.endsWith(".jpeg") ||
                lowerCaseName.endsWith(".png") ||
                lowerCaseName.endsWith(".gif") ||
                lowerCaseName.endsWith(".bmp") ||
                lowerCaseName.endsWith(".webp") ||
                lowerCaseName.endsWith(".tiff") ||
                lowerCaseName.endsWith(".svg");
    }

}
