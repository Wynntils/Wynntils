/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.secrets.type;

import java.net.URI;

public enum WynntilsSecret {
    WYNNCRAFT_API_TOKEN(
            "service.wynntils.secrets.wynncraftApiToken",
            URI.create("https://wynncraft.com/account/dashboard?section=dev"));

    private final String descriptionKey;
    private final URI url;

    WynntilsSecret(String descriptionKey, URI url) {
        this.descriptionKey = descriptionKey;
        this.url = url;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public URI getUrl() {
        return url;
    }
}
