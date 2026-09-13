package com.wynntils.screens.maps.managers.type;

import com.wynntils.services.mapdata.providers.json.JsonProviderInfo.JsonProviderType;
import com.wynntils.utils.EnumUtils;

public enum ProviderType {
    BUILT_IN,
    JSON_BUNDLED(JsonProviderType.BUNDLED),
    JSON_LOCAL(JsonProviderType.LOCAL),
    JSON_REMOTE(JsonProviderType.REMOTE);

    private final JsonProviderType jsonType;

    ProviderType() {
        this.jsonType = null;
    }

    ProviderType(JsonProviderType jsonType) {
        this.jsonType = jsonType;
    }

    public boolean isJson() {
        return jsonType != null;
    }

    public JsonProviderType getJsonType() {
        return jsonType;
    }

    public static ProviderType fromJsonProviderType(JsonProviderType jsonType) {
        for (ProviderType type : values()) {
            if (type.getJsonType() == jsonType) {
                return type;
            }
        }

        return null;
    }

    public String getDisplayName() {
        return EnumUtils.toNiceString(name());
    }
}