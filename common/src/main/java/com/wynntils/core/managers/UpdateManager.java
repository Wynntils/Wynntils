/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.webapi.request.Request;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.utils.FileUtils;
import com.wynntils.utils.Pair;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateManager extends CoreManager {
    private static final Pattern ARTIFACT_PATTERN =
            Pattern.compile("(.+)/build/libs/wynntils-(.+)\\+(\\d+).MC1.18.2-(.+).jar");

    private static final String LAST_BUILD_DOWNLOAD_PATH =
            "https://ci.wynntils.com/job/Artemis/lastSuccessfulBuild/artifact/";
    private static final String LAST_BUILD_RELATIVE_PATHS =
            "https://ci.wynntils.com/job/Artemis/lastSuccessfulBuild/api/json?tree=artifacts%5BrelativePath%5D";

    public static void init() {}

    public static CompletableFuture<UpdateResult> tryUpdate() {
        CompletableFuture<UpdateResult> future = new CompletableFuture<>();

        Request versionRequest = new RequestBuilder(LAST_BUILD_RELATIVE_PATHS, "update_paths")
                .handleJsonObject(jsonObject -> {
                    JsonArray artifacts = jsonObject.getAsJsonArray("artifacts");

                    Pair<String, Boolean> foundArtifact = findNewestArtifact(artifacts);
                    if (foundArtifact.b()) {
                        future.complete(UpdateResult.ALREADY_ON_LATEST);
                        return false;
                    }

                    if (foundArtifact.a() == null) {
                        future.complete(UpdateResult.ERROR);
                        return false;
                    }

                    tryFetchNewUpdate(foundArtifact, future);

                    return true;
                })
                .build();

        RequestHandler handler = new RequestHandler();

        handler.addAndDispatch(versionRequest, true);

        return future;
    }

    private static Pair<String, Boolean> findNewestArtifact(JsonArray artifacts) {
        final String expectedModLoader = WynntilsMod.getModLoader().toString().toLowerCase(Locale.ROOT);
        final int currentBuild = WynntilsMod.getBuildNumber();

        String foundArtifact = null;
        boolean alreadyOnLatest = false;

        for (JsonElement artifact : artifacts) {
            String artifactPath = artifact.getAsJsonObject()
                    .getAsJsonPrimitive("relativePath")
                    .getAsString();
            Matcher matcher = ARTIFACT_PATTERN.matcher(artifactPath);

            if (matcher.matches()) {
                if (!matcher.group(1).equals(expectedModLoader)) {
                    WynntilsMod.info(
                            "Found build for " + matcher.group(1) + ", but current loader is " + expectedModLoader);
                    continue;
                }

                int buildNumber = Integer.parseInt(matcher.group(3));

                if (buildNumber <= currentBuild) {
                    foundArtifact = artifactPath;
                    alreadyOnLatest = true;
                    WynntilsMod.info("Found build " + buildNumber + ", but current build is " + currentBuild);
                    continue;
                }

                foundArtifact = artifactPath;
                break;
            }
        }

        if (foundArtifact == null) {
            WynntilsMod.warn("Tried to update, but could not find target artifact.");
            return new Pair<>(null, false);
        }

        if (alreadyOnLatest) {
            WynntilsMod.warn("Tried to update, but mod is already up to date.");
            return new Pair<>(foundArtifact, true);
        }

        WynntilsMod.info("Found update artifact " + foundArtifact);
        return new Pair<>(foundArtifact, false);
    }

    private static void tryFetchNewUpdate(Pair<String, Boolean> foundArtifact, CompletableFuture<UpdateResult> future) {
        File oldJar = WynntilsMod.getModJar();

        try {
            URL downloadUrl = new URL(LAST_BUILD_DOWNLOAD_PATH + foundArtifact.a());
            InputStream in = downloadUrl.openStream();

            File newJar =
                    new File(new File(WynntilsMod.getModStorageDir("updates").toURI()), "");
            Files.copy(in, newJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

            future.complete(UpdateResult.SUCCESSFUL);

            WynntilsMod.info("Successfully downloaded Wynntils update!");

            addShutdownHook(oldJar, newJar);
        } catch (IOException exception) {
            future.complete(UpdateResult.ERROR);
            WynntilsMod.error("Exception when trying to download update!", exception);
        }
    }

    private static void addShutdownHook(File oldJar, File newJar) {
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
        ERROR
    }
}
