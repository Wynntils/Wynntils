/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.secrets.type;

public enum SecretKey {
    WYNNCRAFT_API_TOKEN("service.wynntils.secrets.wynncraftApiToken");

    private final String descriptionKey;

    SecretKey(String descriptionKey) {
        this.descriptionKey = descriptionKey;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }
}
