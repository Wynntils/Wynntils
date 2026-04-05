/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import java.util.Optional;

public enum DownloadSource {
    CDN(Optional.of("https://cdn.wynntils.com/static/")),
    GITHUB(Optional.of("https://raw.githubusercontent.com/Wynntils/Static-Storage/refs/heads/main/")),
    CUSTOM(Optional.empty());

    private final Optional<String> url;

    DownloadSource(Optional<String> url) {
        this.url = url;
    }

    public Optional<String> getUrl() {
        return url;
    }
}
