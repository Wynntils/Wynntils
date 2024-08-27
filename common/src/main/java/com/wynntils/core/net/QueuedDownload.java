/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.components.CoreComponent;

public record QueuedDownload(
        Download download, UrlId urlId, CoreComponent callerComponent, Dependency dependency, Status status) {
    public enum Status {
        QUEUED,
        DOWNLOADING,
        FINISHED,
        FAILED
    }
}
