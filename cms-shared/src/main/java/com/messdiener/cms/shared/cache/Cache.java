package com.messdiener.cms.shared.cache;

import java.util.UUID;

public final class Cache {
    private Cache() {}

    public static final UUID SYSTEM_TENANT = UUID.fromString("d4c5b381-8022-43bf-a67e-5b44562bb94f");
    public static final UUID SYSTEM_USER   = UUID.fromString("93dacda6-b951-413a-96dc-9a37858abe3e");
    public static final UUID WEBSITE       = UUID.fromString("d4c5b381-8022-43bf-a67e-5b44562bb94f");
}
