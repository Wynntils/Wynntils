/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ServerResourcePackEvent;
import com.wynntils.mc.event.SetEntityDataEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.utils.FileUtils;
import com.wynntils.utils.mc.McUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.DEBUG)
public class TextureRecorderFeature extends Feature {
    private static final File SAVE_FOLDER = WynntilsMod.getModStorageDir("debug");
    private static final File TEXTURES_FOLDER = new File(SAVE_FOLDER, "textures");

    private final Map<Integer, List<Identifier>> modelIdToTextureIds = new LinkedHashMap<>();
    private final Map<Integer, List<Identifier>> lastSeenModelToIds = new LinkedHashMap<>();
    private final Map<Integer, String> modelIdToFingerprint = new LinkedHashMap<>();

    private Integer recordingTicksRemaining = 0;

    @RegisterKeyBind
    private final KeyBind textureRecorderKeybind =
            KeyBindDefinition.TEXTURE_RECORDER.create(this::startRecording);

    @Persisted
    private final Config<Integer> recordingTicks = new Config<>(200);

    public TextureRecorderFeature() {
        super(ProfileDefault.DISABLED);
    }

    private void startRecording() {
        if (modelIdToTextureIds.isEmpty() && !buildModelMap()) {
            WynntilsMod.info("Failed to build model map — recording aborted.");
            return;
        }

        recordingTicksRemaining = recordingTicks.get();
        WynntilsMod.info("Recording started for " + recordingTicks.get() / 20 + "s (" + modelIdToTextureIds.size()
                + " model IDs mapped)");
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (recordingTicksRemaining <= 0) return;
        recordingTicksRemaining--;

        if (recordingTicksRemaining == 0) {
            saveResults();
        }
    }

    @SubscribeEvent
    public void onServerResourcePackChange(ServerResourcePackEvent.Change event) {
        modelIdToTextureIds.clear();
        modelIdToFingerprint.clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySetData(SetEntityDataEvent event) {
        if (recordingTicksRemaining <= 0) return;

        Entity entity = McUtils.mc().level.getEntity(event.getId());
        if (!(entity instanceof Display.ItemDisplay)) return;

        SynchedEntityData.DataValue<?> dataValue = event.getPackedItems().stream()
                .filter(data -> data.id() == Display.ItemDisplay.DATA_ITEM_STACK_ID.id())
                .findFirst()
                .orElse(null);
        if (dataValue == null) return;

        ItemStack itemStack = (ItemStack) dataValue.value();
        if (!itemStack.is(Items.OAK_BOAT)) return;

        CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (customModelData == null || customModelData.floats().isEmpty()) return;

        int modelId = customModelData.floats().getFirst().intValue();

        if (lastSeenModelToIds.containsKey(modelId)) return;

        List<Identifier> textureIds = modelIdToTextureIds.get(modelId);
        if (textureIds == null || textureIds.isEmpty()) {
            WynntilsMod.info("id: " + modelId + " (no textures resolved)");
            return;
        }

        lastSeenModelToIds.put(modelId, textureIds);

        String firstName = getFilename(textureIds.getFirst());
        String pixelHash = computePixelHash(textureIds.getFirst());
        String fingerprint = modelIdToFingerprint.getOrDefault(modelId, "none");
        WynntilsMod.info("id: " + modelId
                + " png: " + firstName
                + " pixel-hash: " + pixelHash
                + " fingerprint: " + fingerprint
                + (textureIds.size() > 1 ? " (+" + (textureIds.size() - 1) + " more)" : ""));
    }

    private boolean buildModelMap() {
        ResourceManager rm = McUtils.mc().getResourceManager();
        Identifier boatJson = Identifier.fromNamespaceAndPath("minecraft", "models/item/oak_boat.json");
        Optional<Resource> overrideRes = rm.getResource(boatJson);

        if (overrideRes.isEmpty()) {
            WynntilsMod.error("Could not find oak_boat.json override file!");
            return false;
        }

        try (InputStream stream = overrideRes.get().open();
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject baseJson = JsonParser.parseReader(reader).getAsJsonObject();
            if (!baseJson.has("overrides")) return false;

            JsonArray overrides = baseJson.getAsJsonArray("overrides");
            WynntilsMod.info("Processing " + overrides.size() + " overrides from oak_boat.json");

            for (int i = 0; i < overrides.size(); i++) {
                JsonObject override = overrides.get(i).getAsJsonObject();
                int cmd = override.getAsJsonObject("predicate")
                        .get("custom_model_data")
                        .getAsInt();
                String modelPath = override.get("model").getAsString();
                resolveModelTextures(rm, cmd, modelPath);
            }

        } catch (IOException e) {
            WynntilsMod.error("Failed to parse oak_boat.json: " + e.getMessage());
            return false;
        }

        return !modelIdToTextureIds.isEmpty();
    }

    private void resolveModelTextures(ResourceProvider rm, int cmd, String modelPath) {
        Identifier parsedModel = Identifier.parse(modelPath);
        Identifier modelId = parsedModel.withPath(p -> "models/" + p + ".json");

        rm.getResource(modelId).ifPresent(res -> {
            try (InputStream stream = res.open();
                    InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                if (!json.has("elements") || !json.has("textures")) return;

                JsonArray elements = json.getAsJsonArray("elements");
                String fingerprint = sha256(elements.toString());
                modelIdToFingerprint.put(cmd, fingerprint);

                Set<String> referencedSlots = new LinkedHashSet<>();
                for (var element : json.getAsJsonArray("elements")) {
                    JsonObject el = element.getAsJsonObject();
                    if (!el.has("faces")) continue;
                    for (String faceKey : el.getAsJsonObject("faces").keySet()) {
                        JsonObject face = el.getAsJsonObject("faces").getAsJsonObject(faceKey);
                        if (!face.has("texture")) continue;
                        String ref = face.get("texture").getAsString();
                        if (ref.startsWith("#")) referencedSlots.add(ref.substring(1));
                    }
                }

                JsonObject textures = json.getAsJsonObject("textures");
                List<Identifier> textureIds = new ArrayList<>();

                for (String slot : referencedSlots) {
                    if (!textures.has(slot)) continue;
                    String texPath = textures.get(slot).getAsString();
                    Identifier parsedTex = Identifier.parse(texPath);
                    textureIds.add(parsedTex.withPath(p -> "textures/" + p + ".png"));
                }

                if (!textureIds.isEmpty()) {
                    modelIdToTextureIds.put(cmd, textureIds);
                }

            } catch (IOException e) {
                WynntilsMod.error("Failed to parse model JSON: " + modelId);
            }
        });
    }

    private void saveResults() {
        saveToDisk();
        saveTexturePngs();
        resetState();
    }

    private void saveToDisk() {
        String fileName = "texture_dump.txt";
        File textFile = new File(SAVE_FOLDER, fileName);

        FileUtils.mkdir(textFile.getParentFile());
        Map<Identifier, String> textureHashCache = new HashMap<>();

        try (PrintWriter writer =
                new PrintWriter(new OutputStreamWriter(new FileOutputStream(textFile), StandardCharsets.UTF_8))) {
            writer.println("=== Texture Recorder Debug Results ===");
            writer.println("Seen " + lastSeenModelToIds.size() + " unique model IDs");
            writer.println();

            writer.println("--- Seen textures summary ---");
            for (Map.Entry<Integer, List<Identifier>> entry : lastSeenModelToIds.entrySet()) {
                int modelId = entry.getKey();
                String fingerprint = modelIdToFingerprint.getOrDefault(modelId, "none");
                String label = getLabel(entry.getValue());
                writer.println("  " + label + " (model ID: " + modelId + ", fingerprint: " + fingerprint + ")");
            }
            writer.println();

            for (Map.Entry<Integer, List<Identifier>> entry : lastSeenModelToIds.entrySet()) {
                int modelId = entry.getKey();
                List<Identifier> textureIds = entry.getValue();
                String label = getLabel(textureIds);
                writer.println("Texture: " + label);

                String fingerprint = modelIdToFingerprint.getOrDefault(modelId, "none");
                writer.println("  model ID: " + modelId + "   fingerprint: " + fingerprint);

                for (Identifier texId : textureIds) {
                    String pixelHash = textureHashCache.get(texId);
                    if (pixelHash == null) {
                        pixelHash = computePixelHash(texId);
                        textureHashCache.put(texId, pixelHash);
                    }
                    writer.println("  texture: " + texId + "   pixel-hash: " + pixelHash);
                }
                writer.println();
            }

        } catch (IOException e) {
            WynntilsMod.error("Failed to save texture mapping dump " + textFile, e);
        }

        McUtils.sendWynntilsPrefixMessage(
                Component.literal("Saved texture mapping dump to " + textFile.getAbsolutePath()));
    }

    private String computePixelHash(Identifier textureId) {
        ResourceManager rm = McUtils.mc().getResourceManager();
        Optional<Resource> res = rm.getResource(textureId);
        if (res.isEmpty()) return "not found";

        try (InputStream stream = res.get().open();
                NativeImage img = NativeImage.read(stream)) {
            int[] pixels = getPixels(img);
            return sha256(Arrays.toString(pixels));
        } catch (IOException e) {
            WynntilsMod.error("Failed to compute pixel hash for " + textureId + ": " + e.getMessage());
            return "error";
        }
    }

    private void saveTexturePngs() {
        ResourceManager rm = Minecraft.getInstance().getResourceManager();

        FileUtils.mkdir(TEXTURES_FOLDER);

        Set<Identifier> uniqueTextures = new LinkedHashSet<>();
        for (List<Identifier> ids : lastSeenModelToIds.values()) {
            uniqueTextures.addAll(ids);
        }

        int exportedCount = 0;
        for (Identifier textureId : uniqueTextures) {
            Optional<Resource> resource = rm.getResource(textureId);
            if (resource.isEmpty()) {
                WynntilsMod.error("Texture not found in resource pack: " + textureId);
                continue;
            }

            String filename = getFilename(textureId) + ".png";
            File outFile = new File(TEXTURES_FOLDER, filename);

            try (InputStream in = resource.get().open();
                 FileOutputStream out = new FileOutputStream(outFile)) {
                in.transferTo(out);
                exportedCount++;
            } catch (IOException e) {
                WynntilsMod.error("Failed to export texture " + filename + ": " + e.getMessage());
            }
        }

        McUtils.sendWynntilsPrefixMessage(
                Component.literal("Exported " + exportedCount + " textures to " + TEXTURES_FOLDER.getAbsolutePath()));
    }

    private void resetState() {
        lastSeenModelToIds.clear();
        WynntilsMod.info("Recording finished. Results saved.");
    }

    private static String getFilename(Identifier id) {
        String path = id.getPath();
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.endsWith(".png") ? name.substring(0, name.length() - 4) : name;
    }

    private static String getLabel(List<Identifier> textureIds) {
        if (textureIds == null || textureIds.isEmpty()) return "(unknown)";
        String firstName = getFilename(textureIds.getFirst());
        if (textureIds.size() == 1) return firstName;
        return firstName + " (+" + (textureIds.size() - 1) + " more)";
    }

    private static String sha256(String input) {
        try {
            byte[] fullHash = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(fullHash);
        } catch (NoSuchAlgorithmException e) {
            WynntilsMod.error("SHA-256 algorithm not available: " + e.getMessage());
            return "hash-error";
        }
    }

    private static int[] getPixels(NativeImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = img.getPixel(x, y);
            }
        }
        return pixels;
    }
}
