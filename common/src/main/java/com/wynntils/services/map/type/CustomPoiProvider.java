/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.type;

import java.net.URI;
import java.util.Objects;

public final class CustomPoiProvider {
    private final String name;
    private final URI url;
    private boolean enabled = true;

    public CustomPoiProvider(String name, URI url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public URI getUrl() {
        return url;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomPoiProvider that = (CustomPoiProvider) o;
        return Objects.equals(name, that.name) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url);
    }
}
