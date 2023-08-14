/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.athena;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class UpdateService extends Service {
    private static final String WYNTILLS_UPDATE_FOLDER = "updates";
    private static final String WYNNTILS_UPDATE_FILE_NAME = "wynntils-update.jar";
    private static final File UPDATES_FOLDER = WynntilsMod.getModStorageDir(WYNTILLS_UPDATE_FOLDER);

    public UpdateService() {
        super(List.of());
    }

    public CompletableFuture<String> getLatestBuild() {
        CompletableFuture<String> future = new CompletableFuture<>();

        String stream = getStream();
        WynntilsMod.info("Checking for update for stream " + stream + ".");

        ApiResponse apiResponse = Managers.Net.callApi(UrlId.API_ATHENA_UPDATE_CHECK, Map.of("stream", stream));
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

    private String getStream() {
        // TODO: Replace with config option for the user to select their preferred stream.
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

    public CompletableFuture<UpdateResult> tryUpdate() {
        CompletableFuture<UpdateResult> future = new CompletableFuture<>();

        String stream = getStream();
        WynntilsMod.info("Attempting to download update for stream " + stream + ".");

        ApiResponse apiResponse = Managers.Net.callApi(UrlId.API_ATHENA_UPDATE_CHECK, Map.of("stream", stream));
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

                    tryFetchNewUpdate(latestDownload, latestMd5, future);
                },
                onError -> {
                    WynntilsMod.error("Exception while trying to load new update.");
                    future.complete(UpdateResult.ERROR);
                });

        return future;
    }

    public File getUpdatesFolder() {
        return UPDATES_FOLDER;
    }

    private File getUpdateFile() {
        File updatesDir = new File(UPDATES_FOLDER.toURI());
        FileUtils.mkdir(updatesDir);
        return new File(updatesDir, WYNNTILS_UPDATE_FILE_NAME);
    }

    private void tryFetchNewUpdate(String latestUrl, String latestMd5, CompletableFuture<UpdateResult> future) {
        File oldJar = WynntilsMod.getModJar();
        File newJar = getUpdateFile();

        try {
            URL downloadUrl = new URL(latestUrl);
            InputStream in = downloadUrl.openStream();

            FileUtils.createNewFile(newJar);

            Files.copy(in, newJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String downloadedUpdateFileMd5 = FileUtils.getMd5(newJar);

            if (!Objects.equals(downloadedUpdateFileMd5, latestMd5)) {
                newJar.delete();
                future.complete(UpdateResult.ERROR);
                WynntilsMod.error("Downloaded update file is corrupted!");
                return;
            }

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
        SUCCESSFUL(Component.translatable("service.wynntils.updates.result.successful")
                .withStyle(ChatFormatting.DARK_GREEN)),
        ALREADY_ON_LATEST(
                Component.translatable("service.wynntils.updates.result.latest").withStyle(ChatFormatting.YELLOW)),
        UPDATE_PENDING(Component.translatable("service.wynntils.updates.result.pending")
                .withStyle(ChatFormatting.YELLOW)),
        ERROR(Component.translatable("service.wynntils.updates.result.error").withStyle(ChatFormatting.DARK_RED));

        private final MutableComponent message;

        UpdateResult(MutableComponent message) {
            this.message = message;
        }

        public MutableComponent getMessage() {
            return message;
        }
    }
}
