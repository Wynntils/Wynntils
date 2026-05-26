/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.remoteoverlay.type;

import java.net.URI;
import java.util.Objects;

public final class RemoteOverlayProvider {
    private final String name;
    private final URI url;

    public RemoteOverlayProvider(String name, URI url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public URI getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        RemoteOverlayProvider converted = (RemoteOverlayProvider) obj;
        return Objects.equals(getName(), converted.getName()) && Objects.equals(getUrl(), converted.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url);
    }
}
