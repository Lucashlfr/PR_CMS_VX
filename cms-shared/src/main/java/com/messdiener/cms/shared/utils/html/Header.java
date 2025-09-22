package com.messdiener.cms.shared.utils.html;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Header {

    private boolean visible;
    private String text;
    private String color;

    public static Header empty(){
        return new Header(false, "", "d-none");
    }
}
