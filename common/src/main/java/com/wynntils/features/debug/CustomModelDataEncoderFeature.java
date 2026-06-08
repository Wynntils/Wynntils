/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterCommand;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

@ConfigCategory(Category.DEBUG)
public class CustomModelDataEncoderFeature extends Feature {
    private static final File SAVE_FOLDER = WynntilsMod.getModStorageDir("debug");
    private static final String OUTPUT_FILE = "item_display_model_data.json";
    private static final String INPUT_JSON = """
            {
                "models": {
                    "Mantle": {
                        "textures": [
                            "e1642ce27ab672f742af2395275c55f8433a466d6fc6c28c9f201d83659f9891"
                        ],
                        "fingerprints": []
                    },
                    "Guardian Angels": {
                        "textures": [
                            "dc24efe75918338d3f2f9bac61a48c632ed270f96c0e0a36f4f3c740520d57a5"
                        ],
                        "fingerprints": []
                    },
                    "Angelic Ascension Guardian Angels": {
                        "textures": [
                            "03ef681add633b538ebc1b0d89ba9775840a9b823246c99583b4d4d763a0d6b0"
                        ],
                        "fingerprints": []
                    },
                    "Arrow Shield": {
                        "textures": [
                            "ac92d13b690ddfbc013bba1134b2e1b56e04c2a865b2532a873c55be15b4a15e"
                        ],
                        "fingerprints": []
                    },
                    "Judrajim": {
                        "textures": [
                            "0a347b9ece55a7c131d926bb1c27caa63cb32c1d17d9fa2f057d373c1634cb5c"
                        ],
                        "fingerprints": []
                    },
                    "Meteor Trail": {
                        "textures": [
                            "662b0544d2eaa5f74a31c492a382cb5767cc441c832dfdde74b5de39661a0086"
                        ],
                        "fingerprints": [
                          "4aa761cc79b8e1a17d786253ef423aca6bb92cd65f5afba467a6c38d36662702",
                          "5559c862acd173f8f333eb409634cd6f5b7582e53a15013b98cb19348ea3c123",
                          "fecc509fe5d212d14d9cf3bf93b339c80076f33cd7cc8254b0a93b8beeef0927"
                        ]
                    },
                    "Meteor Ball": {
                        "textures": [
                            "662b0544d2eaa5f74a31c492a382cb5767cc441c832dfdde74b5de39661a0086"
                        ],
                        "fingerprints": [
                            "4918908e2c891617ee71ca135251d6e17c610e40e7aa47670fd449b9a8dd3518"
                        ]
                    }
                }
            }
            """;

    @RegisterCommand
    private final LiteralCommandNode<CommandSourceStack> commandNode =
            Commands.literal("encode").executes(this::encodeCustomModelData).build();

    public CustomModelDataEncoderFeature() {
        super(ProfileDefault.DISABLED);
    }

    private record ModelEntry(String name, Set<String> textureHashes, Set<String> fingerprints) {
        public boolean matches(String textureHash, String fingerprint) {
            if (!textureHashes.contains(textureHash)) return false;
            if (fingerprints == null || fingerprints.isEmpty()) return true;
            return fingerprints.contains(fingerprint);
        }
    }

    private int encodeCustomModelData(CommandContext<CommandSourceStack> context) {
        WynntilsMod.info("[Encoder] Starting encode...");

        JsonObject inputModels;
        try {
            inputModels = JsonParser.parseString(INPUT_JSON).getAsJsonObject().getAsJsonObject("models");
        } catch (Exception e) {
            error("Failed to parse INPUT_JSON: " + e.getMessage());
            return 1;
        }

        // Build one ModelEntry per model, collecting all its texture hashes and fingerprints
        List<ModelEntry> modelEntries = new ArrayList<>();
        for (String modelName : inputModels.keySet()) {
            JsonObject entry = inputModels.getAsJsonObject(modelName);

            Set<String> textureHashes = new LinkedHashSet<>();
            for (var element : JsonUtils.getNullableJsonArray(entry, "textures"))
                textureHashes.add(element.getAsString());

            Set<String> fingerprints = new LinkedHashSet<>();
            for (var element : JsonUtils.getNullableJsonArray(entry, "fingerprints"))
                fingerprints.add(element.getAsString());

            modelEntries.add(new ModelEntry(modelName, textureHashes, fingerprints));
        }

        ResourceManager resourceManager = McUtils.mc().getResourceManager();

        Map<String, String> texturePathToHash = ResourcepackUtils.buildTexturePathToPixelHashMap(resourceManager);

        Map<String, List<Float>> result = new LinkedHashMap<>();
        for (String name : inputModels.keySet()) result.put(name, new ArrayList<>());

        boolean succeeded =
                ResourcepackUtils.forEachBoatModelOverride(resourceManager, (customModelData, modelPath) -> {
                    ResourcepackUtils.ModelData model = ResourcepackUtils.parseModelData(resourceManager, modelPath);
                    if (model == null) return;

                    Set<String> matchedNames = new LinkedHashSet<>();

                    for (ModelEntry entry : modelEntries) {
                        // Try each texture ID the override model references
                        for (Identifier textureId : model.textureIds()) {
                            String cleanPath = textureId
                                    .getPath()
                                    .replaceFirst("^textures/", "")
                                    .replaceFirst("\\.png$", "");
                            String hash = texturePathToHash.get(cleanPath);

                            if (entry.matches(hash, model.fingerprint())) {
                                matchedNames.add(entry.name());
                                break;
                            }
                        }
                    }

                    if (!matchedNames.isEmpty()) {
                        for (String name : matchedNames) result.get(name).add(customModelData);
                    }
                });

        if (!succeeded) {
            error("Could not find oak_boat.json override");
            return 1;
        }

        // Warn about models that didn't match any custom model data
        for (Map.Entry<String, List<Float>> entry : result.entrySet()) {
            if (entry.getValue().isEmpty()) {
                error("No matches found for model: " + entry.getKey());
            }
        }

        JsonObject outputModels = new JsonObject();
        for (Map.Entry<String, List<Float>> entry : result.entrySet()) {
            JsonArray ids = new JsonArray();
            for (float id : entry.getValue()) ids.add(id);
            outputModels.add(entry.getKey(), ids);
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
}
