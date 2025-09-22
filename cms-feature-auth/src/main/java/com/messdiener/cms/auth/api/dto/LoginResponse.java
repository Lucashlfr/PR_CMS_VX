package com.messdiener.cms.auth.api.dto;

public record LoginResponse(boolean ok, String username) {
    public static LoginResponse success(String username) { return new LoginResponse(true, username); }
    public static LoginResponse fail() { return new LoginResponse(false, null); }
}
