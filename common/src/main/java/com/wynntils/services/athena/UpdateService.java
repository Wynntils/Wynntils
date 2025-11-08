/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.athena;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.net.event.DownloadEvent;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.services.athena.type.ChangelogMap;
import com.wynntils.services.athena.type.ModUpdateInfo;
import com.wynntils.services.athena.type.UpdateResult;
import com.wynntils.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import net.minecraft.SharedConstants;
import net.neoforged.bus.api.SubscribeEvent;

public final class UpdateService extends Service {
    private static final String WYNNTILS_UPDATE_FOLDER = "updates";
    private static final String WYNNTILS_UPDATE_FILE_NAME = "wynntils-update.jar";
    private static final File UPDATES_FOLDER = WynntilsMod.getModStorageDir(WYNNTILS_UPDATE_FOLDER);

    // If we don't know the last version, assume we just downloaded the mod, so don't show the changelog
    @Persisted
    public final Storage<String> lastShownChangelogVersion = new Storage<>(WynntilsMod.getVersion());

    @Persisted
    public final Storage<String> ignoredUpdate = new Storage<>("");

    private boolean promptedUpdate = false;
    private float updateProgress = -1f;
    private ModUpdateInfo modUpdateInfo;

    public UpdateService() {
        super(List.of());
    }

    @SubscribeEvent
    public void onDownloadsFinished(DownloadEvent.Completed event) {
        getLatestBuild().thenAccept(updateInfo -> modUpdateInfo = updateInfo);
    }

    public CompletableFuture<ModUpdateInfo> getLatestBuild() {
        if (WynntilsMod.isDevelopmentEnvironment()) return CompletableFuture.completedFuture(null);

        CompletableFuture<ModUpdateInfo> future = new CompletableFuture<>();

        String stream = getStream();
        WynntilsMod.info("Checking for update for stream " + stream + ".");

        ApiResponse apiResponse =
                Services.WynntilsAccount.callApi(UrlId.API_ATHENA_UPDATE_CHECK, Map.of("stream", stream));
        apiResponse.handleJsonObject(
                json -> {
                    ModUpdateInfo version = Managers.Json.GSON.fromJson(json, ModUpdateInfo.class);

                    if (checkUpdateIsValid(version)) {
                        modUpdateInfo = version;
                        future.complete(version);
                    } else {
                        future.complete(null);
                    }
                },
                onError -> {
                    WynntilsMod.error("Exception while trying to fetch update.");
                    future.complete(null);
                });
        return future;
    }

    public CompletableFuture<UpdateResult> tryUpdate() {
        CompletableFuture<UpdateResult> future = new CompletableFuture<>();

        if (modUpdateInfo == null) {
            String stream = getStream();
            WynntilsMod.info("Attempting to download update for stream " + stream + ".");

            ApiResponse apiResponse =
                    Services.WynntilsAccount.callApi(UrlId.API_ATHENA_UPDATE_CHECK, Map.of("stream", stream));
            apiResponse.handleJsonObject(
                    json -> {
                        ModUpdateInfo updateInfo = Managers.Json.GSON.fromJson(json, ModUpdateInfo.class);

                        if (updateInfo.md5() == null) {
                            future.complete(UpdateResult.ERROR);
                            return;
                        }

                        String currentMd5 = FileUtils.getMd5(WynntilsMod.getModJar());
                        if (Objects.equals(currentMd5, updateInfo.md5())) {
                            future.complete(UpdateResult.ALREADY_ON_LATEST);
                            return;
                        }

                        if (!Objects.equals(
                                updateInfo.supportedMcVersion(),
                                SharedConstants.getCurrentVersion().getName())) {
                            future.complete(UpdateResult.INCORRECT_VERSION_RECEIVED);
                            return;
                        }

                        File localUpdateFile = getUpdateFile();
                        if (localUpdateFile.exists()) {
                            String localUpdateMd5 = FileUtils.getMd5(localUpdateFile);

                            // If the local update file is the same as the latest update, we can just use that.
                            if (Objects.equals(localUpdateMd5, updateInfo.md5())) {
                                future.complete(UpdateResult.UPDATE_PENDING);
                                return;
                            } else {
                                // Otherwise, we need to delete the old file.
                                FileUtils.deleteFile(localUpdateFile);
                            }
                        }

                        tryFetchNewUpdate(updateInfo, future);
                    },
                    onError -> {
                        WynntilsMod.error("Exception while trying to load new update.");
                        future.complete(UpdateResult.ERROR);
                    });
        } else {
            File localUpdateFile = getUpdateFile();
            if (localUpdateFile.exists()) {
                String localUpdateMd5 = FileUtils.getMd5(localUpdateFile);

                // If the local update file is the same as the latest update, we can just use that.
                if (Objects.equals(localUpdateMd5, modUpdateInfo.md5())) {
                    future.complete(UpdateResult.UPDATE_PENDING);
                    return future;
                } else {
                    // Otherwise, we need to delete the old file.
                    FileUtils.deleteFile(localUpdateFile);
                }
            }

            Executors.newSingleThreadExecutor().submit(() -> tryFetchNewUpdate(modUpdateInfo, future));
        }

        return future;
    }

    public CompletableFuture<ChangelogMap> getChangelog(boolean saveLastShown) {
        return getChangelog(lastShownChangelogVersion.get(), WynntilsMod.getVersion(), saveLastShown);
    }

    public CompletableFuture<ChangelogMap> getChangelog(String oldVersion, String newVersion, boolean saveLastShown) {
        CompletableFuture<ChangelogMap> future = new CompletableFuture<>();

        ApiResponse response = Services.WynntilsAccount.callApi(
                UrlId.API_ATHENA_UPDATE_CHANGELOG_V2, Map.of("old_version", oldVersion, "new_version", newVersion));

        response.handleJsonObject(
                jsonObject -> {
                    if (!jsonObject.has("changelogs")) return;

                    if (saveLastShown) {
                        lastShownChangelogVersion.store(WynntilsMod.getVersion());
                    }

                    JsonObject changelogs = jsonObject.getAsJsonObject("changelogs");
                    Map<String, String> changelogMap = new LinkedHashMap<>();

                    List<Map.Entry<String, JsonElement>> entries = new ArrayList<>(changelogs.entrySet());
                    // Iterated backwards to get latest changelog first
                    for (int i = entries.size() - 1; i >= 0; i--) {
                        Map.Entry<String, JsonElement> versionEntry = entries.get(i);
                        changelogMap.put(
                                versionEntry.getKey(), versionEntry.getValue().getAsString());
                    }

                    future.complete(new ChangelogMap(changelogMap));
                },
                throwable -> WynntilsMod.warn("Could not get update changelog: ", throwable));

        return future;
    }

    public boolean shouldPromptUpdate() {
        return !promptedUpdate
                && modUpdateInfo != null
                && !modUpdateInfo.version().equals(ignoredUpdate.get());
    }

    public void setHasPromptedUpdate(boolean promptedUpdate) {
        this.promptedUpdate = promptedUpdate;
    }

    public float getUpdateProgress() {
        return updateProgress;
    }

    public ModUpdateInfo getModUpdateInfo() {
        return modUpdateInfo;
    }

    public File getUpdatesFolder() {
        return UPDATES_FOLDER;
    }

    private File getUpdateFile() {
        File updatesDir = new File(UPDATES_FOLDER.toURI());
        FileUtils.mkdir(updatesDir);
        return new File(updatesDir, WYNNTILS_UPDATE_FILE_NAME);
    }

    private String getStream() {
        String version = WynntilsMod.getVersion();
        // Format: v0.0.3-pre-alpha.103+MC-1.19.4 -> pre-alpha
        // Format: v0.0.3-alpha.103+MC-1.19.4 -> alpha
        // Format: v0.0.3+MC-1.19.4 -> release
        // Regex to get the stream:  v\d+\.\d+\.\d+(-(?<stream>[a-z\-]+)\.\d+)?(\+MC-\d\.\d+\.\d+)?

        if (WynntilsMod.isDevelopmentBuild()) {
            return "alpha";
        }

        String stream = version.replaceAll(
                "v\\d+\\.\\d+\\.\\d+(-(?<stream>[a-z\\-]+)\\.\\d+)?(\\+MC-\\d\\.\\d+\\.\\d+)?", "${stream}");

        if (stream.isEmpty()) {
            return "release";
        }

        return stream;
    }

    private boolean checkUpdateIsValid(ModUpdateInfo updateInfo) {
        if (updateInfo.version() == null) {
            WynntilsMod.info("Couldn't fetch latest version, not attempting update reminder or auto-update.");
            return false;
        }

        if (Objects.equals(updateInfo.version(), WynntilsMod.getVersion())) {
            WynntilsMod.info("Mod is on latest version, not attempting update reminder or auto-update.");
            return false;
        }

        if (!Objects.equals(
                updateInfo.supportedMcVersion(),
                SharedConstants.getCurrentVersion().getName())) {
            WynntilsMod.info(
                    "Athena sent an update for a different MC version, not attempting update reminder or auto-update.");
            return false;
        }

        return true;
    }

    private void tryFetchNewUpdate(ModUpdateInfo updateInfo, CompletableFuture<UpdateResult> future) {
        File oldJar = WynntilsMod.getModJar();
        File newJar = getUpdateFile();

        try {
            URL downloadUrl = URI.create(updateInfo.url()).toURL();
            URLConnection connection = downloadUrl.openConnection();

            FileUtils.downloadFileWithProgress(connection, newJar, progress -> updateProgress = progress);

            updateProgress = -1f;
            String downloadedUpdateFileMd5 = FileUtils.getMd5(newJar);

            if (!Objects.equals(downloadedUpdateFileMd5, updateInfo.md5())) {
                newJar.delete();
                future.complete(UpdateResult.ERROR);
                WynntilsMod.error("Downloaded update file is corrupted!");
                return;
            }

            future.complete(UpdateResult.SUCCESSFUL);

            WynntilsMod.info("Successfully downloaded Wynntils update!");

            addShutdownHook(oldJar, newJar);
        } catch (IOException exception) {
            updateProgress = -1f;
            future.complete(UpdateResult.ERROR);
        }
    }

    private void addShutdownHook(File oldJar, File newJar) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (oldJar == null || !oldJar.exists() || oldJar.isDirectory()) {
                    WynntilsMod.warn("Mod jar file not found or incorrect.");
                    return;
                }

                FileUtils.copyFile(newJar, oldJar);
                newJar.delete();

                WynntilsMod.info("Successfully applied update!");
            } catch (IOException e) {
                WynntilsMod.error("Cannot apply update!", e);
            }
        }));
    }
}
