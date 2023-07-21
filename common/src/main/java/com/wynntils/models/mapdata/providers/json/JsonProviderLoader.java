/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata.providers.json;

public class JsonProviderLoader {
    private JsonProvider provider;

    public JsonProviderLoader(String filename) {
        provider = JsonProvider.loadLocalResource(filename);
    }

    public JsonProvider getProvider() {
        return provider;
    }
}
