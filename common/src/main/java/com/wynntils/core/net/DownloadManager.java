/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.CoreComponent;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.utils.StringUtils;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
    // -Dwynntils.downloads.dump.graph=true
    private static final String DOWNLOADS_DUMP_GRAPH = "wynntils.downloads.dump.graph";
    // -Dwynntils.downloads.log.debug=true
    private static final String DOWNLOADS_LOG_DEBUG = "wynntils.downloads.log.debug";
    // -Dwynntils.downloads.max.parallel=8
    private static final String DOWNLOADS_MAX_PARALLEL = "wynntils.downloads.max.parallel";

    private static final boolean DUMP_GRAPH = System.getProperty(DOWNLOADS_DUMP_GRAPH) != null;
    private static final boolean DEBUG_LOGS = System.getProperty(DOWNLOADS_LOG_DEBUG) != null;
    private static final int MAX_PARALLEL_DOWNLOADS = Integer.getInteger(DOWNLOADS_MAX_PARALLEL, 4);

    private final List<QueuedDownload> registeredDownloads = new ArrayList<>();
    private boolean registrationLock = false;

    private DownloadDependencyGraph graph = null;

    private Object downloadsLock = new Object();
    private Map<QueuedDownload, Download> currentDownloads;

    public DownloadManager() {
        super(List.of());
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
        if (DUMP_GRAPH) {
            graph.logGraph();
        }
    }

    public void download() {
        // Reset the state of the manager, as the downloads are being started
        graph.resetState();
        currentDownloads = new LinkedHashMap<>();

        // Start the downloads by filling the parallel download slots
        // After that, the manager will regulate the downloads by itself
        synchronized (downloadsLock) {
            for (int i = 0; i < MAX_PARALLEL_DOWNLOADS; i++) {
                QueuedDownload queuedDownload = graph.nextDownload();

                if (queuedDownload == null) {
                    // This may not be an issue, but it can be a sign of a bug, or a bad configuration
                    WynntilsMod.warn(
                            "Max parallel downloads was not reached, but there are no more downloads to start.");
                    return;
                }

                currentDownloads.put(queuedDownload, getDownload(queuedDownload));
            }
        }
    }

    QueuedDownload queueDownload(UrlId urlId, CoreComponent callerComponent, Dependency dependency) {
        if (registrationLock) {
            throw new IllegalStateException("Cannot queue downloads after the download graph is already built.");
        }

        QueuedDownload queuedDownload = new QueuedDownload(callerComponent, urlId, dependency);
        registeredDownloads.add(queuedDownload);
        return queuedDownload;
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
        QueuedDownload nextDownload = graph.nextDownload();

        synchronized (downloadsLock) {
            for (Map.Entry<QueuedDownload, Download> entry : currentDownloads.entrySet()) {
                if (!entry.getKey().equals(finishedDownload)) continue;

                if (DEBUG_LOGS) {
                    WynntilsMod.info(entry.getKey() + " -> " + nextDownload);
                }

                // Remove the finished download from the current downloads
                currentDownloads.remove(entry.getKey());

                // Queue the next download, if there is one
                if (nextDownload != null) {
                    currentDownloads.put(nextDownload, getDownload(nextDownload));
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
            if (DEBUG_LOGS) {
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
            if (DEBUG_LOGS) {
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
        WynntilsMod.info("[DownloadManager] All downloads finished.");

        if (graph.hasError()) {
            WynntilsMod.warn("[DownloadManager] Some downloads failed. See the statistics for more information.");
        }

        if (graph.hasError() || DEBUG_LOGS) {
            WynntilsMod.info("[DownloadManager] Download statistics:");
            WynntilsMod.info("  - Total downloads: %d".formatted(graph.totalDownloads()));
            WynntilsMod.info("  - Successful downloads: %d".formatted(graph.successfulDownloads()));
            WynntilsMod.info("  - Failed downloads: %d".formatted(graph.failedDownloads()));
            WynntilsMod.info("  - Error Rate: %.0f%%".formatted(graph.errorRate() * 100f));
        }
    }
}
