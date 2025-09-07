// src/main/java/com/messdiener/cms/v3/api/web/dto/LoginResponse.java
package com.messdiener.cms.v3.api.auth.dto;

public record LoginResponse(boolean ok, String username) {
    public static LoginResponse success(String username) { return new LoginResponse(true, username); }
    public static LoginResponse fail() { return new LoginResponse(false, null); }
}
