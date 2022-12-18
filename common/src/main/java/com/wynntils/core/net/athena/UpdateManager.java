/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.athena;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Manager;
import com.wynntils.core.managers.Managers;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlId;
import com.wynntils.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class UpdateManager extends Manager {
    private static final String WYNTILLS_UPDATE_FOLDER = "updates";
    private static final String WYNNTILS_UPDATE_FILE_NAME = "wynntils-update.jar";

    public UpdateManager(NetManager netManager) {
        super(List.of(netManager));
    }

    public CompletableFuture<String> getLatestBuild() {
        CompletableFuture<String> future = new CompletableFuture<>();

        ApiResponse apiResponse = Managers.Net.callApi(UrlId.API_ATHENA_UPDATE_CHECK);
        apiResponse.handleJsonObject(
                json -> {
                    String version = json.getAsJsonPrimitive("version").getAsString();
                    future.complete(version);
                },
                onError -> {
                    WynntilsMod.error("Exception while trying to fetch update.");
                    future.complete(null);
                });
        return future;
    }

    public CompletableFuture<UpdateResult> tryUpdate() {
        CompletableFuture<UpdateResult> future = new CompletableFuture<>();

        ApiResponse apiResponse = Managers.Net.callApi(UrlId.API_ATHENA_UPDATE_CHECK);
        apiResponse.handleJsonObject(
                json -> {
                    String latestMd5 = json.getAsJsonPrimitive("md5").getAsString();

                    if (latestMd5 == null) {
                        future.complete(UpdateResult.ERROR);
                        return;
                    }

                    String currentMd5 = FileUtils.getMd5(WynntilsMod.getModJar());
                    if (Objects.equals(currentMd5, latestMd5)) {
                        future.complete(UpdateResult.ALREADY_ON_LATEST);
                        return;
                    }

                    File localUpdateFile = getUpdateFile();
                    if (localUpdateFile.exists()) {
                        String localUpdateMd5 = FileUtils.getMd5(localUpdateFile);

                        // If the local update file is the same as the latest update, we can just use that.
                        if (Objects.equals(localUpdateMd5, latestMd5)) {
                            future.complete(UpdateResult.UPDATE_PENDING);
                            return;
                        } else {
                            // Otherwise, we need to delete the old file.
                            FileUtils.deleteFile(localUpdateFile);
                        }
                    }

                    String latestDownload = json.getAsJsonPrimitive("url").getAsString();

                    tryFetchNewUpdate(latestDownload, future);
                },
                onError -> {
                    WynntilsMod.error("Exception while trying to load new update.");
                    future.complete(UpdateResult.ERROR);
                });

        return future;
    }

    private File getUpdateFile() {
        File updatesDir =
                new File(WynntilsMod.getModStorageDir(WYNTILLS_UPDATE_FOLDER).toURI());
        FileUtils.mkdir(updatesDir);
        return new File(updatesDir, WYNNTILS_UPDATE_FILE_NAME);
    }

    private void tryFetchNewUpdate(String latestUrl, CompletableFuture<UpdateResult> future) {
        File oldJar = WynntilsMod.getModJar();
        File newJar = getUpdateFile();

        try {
            URL downloadUrl = new URL(latestUrl);
            InputStream in = downloadUrl.openStream();

            FileUtils.createNewFile(newJar);

            Files.copy(in, newJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

            future.complete(UpdateResult.SUCCESSFUL);

            WynntilsMod.info("Successfully downloaded Wynntils update!");

            addShutdownHook(oldJar, newJar);
        } catch (IOException exception) {
            newJar.delete();
            future.complete(UpdateResult.ERROR);
            WynntilsMod.error("Exception when trying to download update!", exception);
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

    public enum UpdateResult {
        SUCCESSFUL,
        ALREADY_ON_LATEST,
        UPDATE_PENDING,
        ERROR
    }
}
