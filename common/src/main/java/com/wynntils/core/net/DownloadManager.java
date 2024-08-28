/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.components.CoreComponent;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Manages downloading files from the internet. This manager acts as a middle layer between the caller components
 * and {@link NetManager}. It's main purpose is to provide the components a well regulated and managed way
 * of downloading dependency files.
 * <p><b>This manager should not be used for spontaneous downloads. Use one {@link NetManager}'s download methods
 * if you want to download files during running, and not in a pre-planned fashion.</b></p>
 * <p>This component provides multiple tools to ease the development of other components:</p>
 * - The manager can handle dependency resoltion, allowing components to depend on other component's data files, without
 * having to natively code the dependency resolution. As the manager is responsible for every components' downloads,
 * it can also ensure that all dependencies are valid, meaning they are not circular or missing.
 * <br>
 * - The manager can directly handle re-download requests, allowing all data sources to be re-downloaded directly, if
 * the user or system requires it.
 * <br>
 * - The manager can keep track of failed downloads, and easily retry them, knowing that no race conditions will occur
 * on either successful or failed downloads. It can also provide the user with useful information about their cache state.
 * <br>
 * - The manager can handle parallel downloads, within regulated manners, ensuring a stable amount of downloads
 * are happening at any given time. This allows more stable downloads for less stable internet connections. The manager
 * can also provide a clear view of the download queue, and the download progress.
 */
public class DownloadManager extends Manager {
    private static final int MAX_PARALLEL_DOWNLOADS = 4;

    private final List<QueuedDownload> downloadQueue = new ArrayList<>();
    private boolean queueLock = false;

    public DownloadManager() {
        super(List.of());
    }

    public void initComponents(Map<Class<? extends CoreComponent>, List<CoreComponent>> componentMap) {
        componentMap.forEach((componentClass, coreComponents) -> {
            coreComponents.forEach(
                    coreComponent -> coreComponent.registerDownloads(new DownloadRegistry(this, coreComponent)));
        });
    }

    public CompletableFuture<Void> downloadAll() {
        // Lock the queue after the first download request
        queueLock = true;

        return CompletableFuture.runAsync(() -> {});
    }

    QueuedDownload queueDownload(UrlId urlId, CoreComponent callerComponent, Dependency dependency) {
        if (queueLock) {
            throw new IllegalStateException("Cannot queue downloads after the first download request.");
        }

        QueuedDownload queuedDownload =
                new QueuedDownload(Managers.Net.download(urlId), callerComponent, urlId, dependency);
        downloadQueue.add(queuedDownload);
        return queuedDownload;
    }

    private void createQueueTree() {
        // Reverse tree for dependency graph, traverse by breadth-first search
    }
}
