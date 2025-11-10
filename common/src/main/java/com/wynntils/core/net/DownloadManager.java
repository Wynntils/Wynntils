/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.CoreComponent;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.event.DownloadEvent;
import com.wynntils.core.net.event.UrlProcessingFinishedEvent;
import com.wynntils.core.properties.Property;
import com.wynntils.utils.StringUtils;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Manages downloading files from the internet. This manager acts as a middle layer between the caller components
 * and {@link NetManager}. It's main purpose is to provide the components a well regulated and managed way
 * of downloading dependency files.
 * <p><b>This manager should not be used for spontaneous downloads. Use one {@link NetManager}'s download methods
 * if you want to download files during running, and not in a pre-planned fashion.</b></p>
 * <p>This component provides multiple tools to ease the development of other components:</p>
 * - The manager can handle dependency resolution, allowing components to depend on other component's data files, without
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
    private final Property<Boolean> dumpGraph = createProperty(Boolean.class, "dump.graph", false);
    private final Property<Boolean> debugLogs = createProperty(Boolean.class, "log.debug", false);
    private final Property<Integer> maxParallelDownloads = createProperty(Integer.class, "max.parallel", 4);

    private final List<QueuedDownload> registeredDownloads = new ArrayList<>();
    private boolean registrationLock = false;

    private DownloadDependencyGraph graph = null;

    private Set<QueuedDownload> currentDownloads;

    public DownloadManager() {
        super(List.of());
    }

    @SubscribeEvent
    public void onUrlProcessingFinished(UrlProcessingFinishedEvent event) {
        download(false);
    }

    public void initComponents(Map<Class<? extends CoreComponent>, List<CoreComponent>> componentMap) {
        componentMap.forEach((componentClass, coreComponents) -> {
            coreComponents.forEach(
                    coreComponent -> coreComponent.registerDownloads(new DownloadRegistry(this, coreComponent)));
        });

        // Lock the queue, as the graph is being built,
        // no modifications should be done during the lifetime of the game instance
        registrationLock = true;

        graph = DownloadDependencyGraph.build(registeredDownloads);

        // Dump the graph if the system property is set
        if (dumpGraph.get()) {
            graph.logGraph();
        }
    }

    public void retryDownload(QueuedDownload download) {
        if (!registrationLock) {
            throw new IllegalStateException("Cannot retry downloads while the download graph is still being built.");
        }

        if (!graph.state().finished()) {
            throw new IllegalStateException("Cannot retry downloads while a download is already happening.");
        }

        // Mark the download as a retry, with it's dependencies and dependents
        graph.markDownloadRetry(download);

        download(true);
    }

    public List<QueuedDownload> registeredDownloads() {
        return Collections.unmodifiableList(registeredDownloads);
    }

    public DownloadDependencyGraph.DownloadDependencyGraphState graphState() {
        return graph.state();
    }

    public DownloadDependencyGraph.NodeState getDownloadState(QueuedDownload download) {
        return graph.getDownloadState(download);
    }

    QueuedDownload queueDownload(UrlId urlId, CoreComponent callerComponent, Dependency dependency) {
        if (registrationLock) {
            throw new IllegalStateException("Cannot queue downloads after the download graph is already built.");
        }

        QueuedDownload queuedDownload = new QueuedDownload(callerComponent, urlId, dependency);
        registeredDownloads.add(queuedDownload);
        return queuedDownload;
    }

    private void download(boolean partialRedownload) {
        if (!partialRedownload) {
            // Reset the state of the manager, as a full redownload is happening
            graph.resetState();
            currentDownloads = new LinkedHashSet<>();
        }

        WynntilsMod.postEventOnMainThread(new DownloadEvent.Started(partialRedownload));

        // Start the downloads by filling the parallel download slots
        // After that, the manager will regulate the downloads by itself
        synchronized (currentDownloads) {
            for (int i = 0; i < maxParallelDownloads.get(); i++) {
                QueuedDownload queuedDownload = graph.nextDownload();

                if (queuedDownload == null) {
                    if (!partialRedownload) {
                        // This may not be an issue, but it can be a sign of a bug, or a bad configuration
                        WynntilsMod.warn(
                                "Max parallel downloads was not reached, but there are no more downloads to start.");
                    }

                    return;
                }

                currentDownloads.add(queuedDownload);
                getDownload(queuedDownload);
            }

            if (debugLogs.get()) {
                WynntilsMod.info("[DownloadManager] Started downloads:");
                currentDownloads.forEach(queuedDownload -> {
                    WynntilsMod.info("  - %s -> %s"
                            .formatted(
                                    StringUtils.capitalizeFirst(
                                            queuedDownload.callerComponent().getJsonName()),
                                    queuedDownload.urlId()));
                });
            }
        }
    }

    private Download getDownload(QueuedDownload queuedDownload) {
        Download download = Managers.Net.download(queuedDownload.urlId());

        Consumer<Reader> readerHandler = queuedDownload.onCompletionReader();
        if (readerHandler != null) {
            download.handleReader(
                    wrapDownloadHandler(readerHandler, queuedDownload), wrapDownloadFailure(queuedDownload));
            return download;
        }

        Consumer<JsonObject> jsonObjectHandler = queuedDownload.onCompletionJsonObject();
        if (jsonObjectHandler != null) {
            download.handleJsonObject(
                    wrapDownloadHandler(jsonObjectHandler, queuedDownload), wrapDownloadFailure(queuedDownload));
            return download;
        }

        Consumer<JsonArray> jsonArrayHandler = queuedDownload.onCompletionJsonArray();
        if (jsonArrayHandler != null) {
            download.handleJsonArray(
                    wrapDownloadHandler(jsonArrayHandler, queuedDownload), wrapDownloadFailure(queuedDownload));
            return download;
        }

        throw new IllegalStateException("Queued download has no handler set: " + queuedDownload);
    }

    private void queueNextDownload(QueuedDownload finishedDownload) {
        synchronized (currentDownloads) {
            QueuedDownload nextDownload = graph.nextDownload();

            for (QueuedDownload queuedDownload : currentDownloads) {
                if (!queuedDownload.equals(finishedDownload)) continue;

                if (debugLogs.get()) {
                    WynntilsMod.info(queuedDownload + " -> " + nextDownload);
                }

                // Remove the finished download from the current downloads
                currentDownloads.remove(queuedDownload);

                // Queue the next download, if there is one
                if (nextDownload != null) {
                    currentDownloads.add(nextDownload);
                    getDownload(nextDownload);
                }

                return;
            }

            WynntilsMod.error(
                    "Finished, but not yet replaced download not found in the current downloads: " + finishedDownload);
        }
    }

    private <T> Consumer<T> wrapDownloadHandler(Consumer<T> handler, QueuedDownload download) {
        return (T result) -> {
            // Firstly, run the handler
            handler.accept(result);

            // The handling succeeded, mark the download as completed
            // (if the handling failed, download itself handles the error)

            // Log the progress if the system property is set
            if (debugLogs.get()) {
                WynntilsMod.info("Download finished: "
                        + StringUtils.capitalizeFirst(download.callerComponent().getJsonName()) + " -> "
                        + download.urlId());
            }

            // Mark the download as completed
            graph.markDownloadCompleted(download);
            queueNextDownload(download);
            checkDownloadsFinished();
        };
    }

    private Consumer<Throwable> wrapDownloadFailure(QueuedDownload download) {
        return (throwable) -> {
            // Log the progress if the system property is set
            if (debugLogs.get()) {
                WynntilsMod.warn("Download failed: "
                        + StringUtils.capitalizeFirst(download.callerComponent().getJsonName()) + " -> "
                        + download.urlId());
            }

            // Mark the download as failed
            graph.markDownloadError(download);
            queueNextDownload(download);
            checkDownloadsFinished();
        };
    }

    private void checkDownloadsFinished() {
        if (!graph.isFinished()) return;
        if (!currentDownloads.isEmpty()) return;

        // All downloads are finished, and there are no more downloads to start
        // Display statistics from the graph
        WynntilsMod.info("[DownloadManager] Downloads finished.");

        if (graph.hasError()) {
            WynntilsMod.postEventOnMainThread(new DownloadEvent.Failed());
            WynntilsMod.warn("[DownloadManager] Some downloads failed. See the statistics for more information.");
        } else {
            WynntilsMod.postEventOnMainThread(new DownloadEvent.Completed());
            WynntilsMod.info("[DownloadManager] Downloads succeeded.");
        }

        if (graph.hasError() || debugLogs.get()) {
            WynntilsMod.info("[DownloadManager] Download statistics:");
            WynntilsMod.info("  - Total downloads: %d".formatted(graph.totalDownloads()));
            WynntilsMod.info("  - Successful downloads: %d".formatted(graph.successfulDownloads()));
            WynntilsMod.info("  - Failed downloads: %d".formatted(graph.failedDownloads()));
            WynntilsMod.info("  - Error Rate: %.0f%%".formatted(graph.errorRate() * 100f));
        }
    }
}
