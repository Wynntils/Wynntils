/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.components.CoreComponent;

public class QueuedDownload extends Download {
    private final CoreComponent callerComponent;
    private final UrlId urlId;
    private final Dependency dependency;

    public QueuedDownload(Download download, CoreComponent callerComponent, UrlId urlId, Dependency dependency) {
        super(download.desc, download.localFile, download.request, download.processedEvent);
        this.callerComponent = callerComponent;
        this.urlId = urlId;
        this.dependency = dependency;
    }

    public CoreComponent callerComponent() {
        return callerComponent;
    }

    public UrlId urlId() {
        return urlId;
    }

    public Dependency dependency() {
        return dependency;
    }
}
