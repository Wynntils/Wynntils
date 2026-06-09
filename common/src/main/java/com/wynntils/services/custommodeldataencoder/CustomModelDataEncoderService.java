/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.custommodeldataencoder;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.utils.FileUtils;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ResourcepackUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public class CustomModelDataEncoderService extends Service {
    private static final File SAVE_FOLDER = WynntilsMod.getModStorageDir("debug");
    private static final String OUTPUT_FILE = "item_display_model_data.json";

    private List<ModelEntry> modelEntries = List.of();

    /**
     * To find texture and fingerprint hashes, use the TextureRecorderFeature:
     *
     * 1. Enable the feature and bind a key in Options.
     *
     * 2. Press the keybind to start recording, then cast a spell or find the ingame model.
     * Keep in mind that it's handled via onEntitySetData, so if the entity already exists it won't be recorded.
     * Record before the entity spawns.
     *
     * 3. After the recording period (default 10s), results are saved to:
     *      - wynntils/debug/texture_dump.txt  — lists texturehash and fingerprint for each seen model
     *      - wynntils/debug/textures/         — contains the exported PNG files to find the model visually
     *
     *      - The recording period is configurable in the content book under Texture Recorder
     *
     * Adding a texturehash to texture_and_fingerprint_hashes.json will match all model IDs
     * that use that texture. If you need to target a specific model sharing a texture, you should
     * specify the model's fingerprint. The fingerprint is a hash of the "elements" block in the
     * model's JSON, making it unique. You can also add multiple texturehashes to the same group if a spell or model
     * uses more than one texture.
     *
     * See texture_and_fingerprint_hashes.json in static_storage for examples (e.g. meteor ball and meteor trail).
     */
    public CustomModelDataEncoderService() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_TEXTURE_AND_FINGERPRINT_HASHES)
                .handleJsonObject(this::handleJson);
    }

    private void handleJson(JsonObject jsonObject) {
        JsonObject modelsObject = jsonObject.getAsJsonObject("models");
        if (modelsObject == null) return;

        List<ModelEntry> entries = new ArrayList<>();
        for (String modelName : modelsObject.keySet()) {
            JsonObject entry = modelsObject.getAsJsonObject(modelName);

            Set<String> textureHashes = new LinkedHashSet<>();
            for (JsonElement element : JsonUtils.getNullableJsonArray(entry, "textures")) {
                textureHashes.add(element.getAsString());
            }

            Set<String> fingerprints = new LinkedHashSet<>();
            for (JsonElement element : JsonUtils.getNullableJsonArray(entry, "fingerprints")) {
                fingerprints.add(element.getAsString());
            }

            entries.add(new ModelEntry(modelName, textureHashes, fingerprints));
        }

        modelEntries = entries;
    }

    public int encodeCustomModelData(CommandContext<CommandSourceStack> context) {
        WynntilsMod.info("[Encoder] Starting encode...");

        ResourceManager resourceManager = McUtils.mc().getResourceManager();

        Map<String, String> texturePathToHash = ResourcepackUtils.buildTexturePathToTextureHashMap(resourceManager);

        JsonObject outputModels = new JsonObject();
        for (ModelEntry entry : modelEntries) {
            outputModels.add(entry.name(), new JsonArray());
        }

        boolean succeeded =
                ResourcepackUtils.forEachBoatModelOverride(resourceManager, (customModelData, modelPath) -> {
                    ResourcepackUtils.ModelData model = ResourcepackUtils.parseModelData(resourceManager, modelPath);
                    if (model == null) return;

                    for (ModelEntry entry : modelEntries) {
                        for (Identifier textureId : model.textureIds()) {
                            String cleanPath = textureId
                                    .getPath()
                                    .replaceFirst("^textures/", "")
                                    .replaceFirst("\\.png$", "");
                            String hash = texturePathToHash.get(cleanPath);

                            if (entry.matches(hash, model.fingerprint())) {
                                outputModels.getAsJsonArray(entry.name()).add(customModelData);
                                break;
                            }
                        }
                    }
                });

        if (!succeeded) {
            error("Could not find oak_boat.json override");
            return 1;
        }

        for (String name : outputModels.keySet()) {
            if (outputModels.getAsJsonArray(name).isEmpty()) {
                error("No matches found for model: " + name);
            }
        }

        JsonObject output = new JsonObject();
        output.add("models", outputModels);

        File outFile = new File(SAVE_FOLDER, OUTPUT_FILE);
        FileUtils.mkdir(outFile.getParentFile());

        try (PrintWriter pw =
                new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8))) {
            pw.print(new GsonBuilder().setPrettyPrinting().create().toJson(output));
        } catch (IOException e) {
            error("Failed to write output file: " + e.getMessage());
            return 1;
        }

        String summary = "Encoding complete. Output written to " + outFile.getAbsolutePath();
        WynntilsMod.info("[Encoder] " + summary);
        McUtils.sendWynntilsPrefixMessage(Component.literal(summary));
        return 1;
    }

    private static void error(String msg) {
        WynntilsMod.error("[Encoder] " + msg);
        McUtils.sendWynntilsPrefixMessage(Component.literal("[Encoder] ERROR: " + msg));
    }

    private record ModelEntry(String name, Set<String> textureHashes, Set<String> fingerprints) {
        public boolean matches(String textureHash, String fingerprint) {
            if (!textureHashes.contains(textureHash)) return false;
            if (fingerprints == null || fingerprints.isEmpty()) return true;
            return fingerprints.contains(fingerprint);
        }
    }
}
