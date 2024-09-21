/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.components.CoreComponent;

public class DownloadRegistry {
    private final DownloadManager downloadManager;
    private final CoreComponent callerComponent;

    DownloadRegistry(DownloadManager downloadManager, CoreComponent callerComponent) {
        this.downloadManager = downloadManager;
        this.callerComponent = callerComponent;
    }

    public QueuedDownload registerDownload(UrlId urlId) {
        return registerDownload(urlId, Dependency.empty());
    }

    public QueuedDownload registerDownload(UrlId urlId, Dependency dependency) {
        return downloadManager.queueDownload(urlId, callerComponent, dependency);
    }
}
