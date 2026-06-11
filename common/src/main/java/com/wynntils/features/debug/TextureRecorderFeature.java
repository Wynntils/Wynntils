/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

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
import com.wynntils.utils.wynn.ResourcepackUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
    private static final String OUTPUT_FILE = "texture_dump.txt";
    private static final File TEXTURES_FOLDER = new File(SAVE_FOLDER, "textures");

    private final Map<Float, List<Identifier>> modelIdToTextureIds = new LinkedHashMap<>();
    private final Map<Float, List<Identifier>> lastSeenModelToIds = new LinkedHashMap<>();
    private final Map<Float, String> modelIdToFingerprint = new LinkedHashMap<>();

    private Integer recordingTicksRemaining = 0;

    @RegisterKeyBind
    private final KeyBind textureRecorderKeybind = KeyBindDefinition.TEXTURE_RECORDER.create(this::startRecording);

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

        float modelId = customModelData.floats().getFirst();
        if (lastSeenModelToIds.containsKey(modelId)) return;

        List<Identifier> textureIds = modelIdToTextureIds.get(modelId);
        if (textureIds == null || textureIds.isEmpty()) {
            WynntilsMod.info("id: " + modelId + " (no textures resolved)");
            return;
        }

        lastSeenModelToIds.put(modelId, textureIds);

        ResourceManager resourceManager = McUtils.mc().getResourceManager();
        String firstName = getFilename(textureIds.getFirst());
        String textureHash = ResourcepackUtils.computeTextureHash(resourceManager, textureIds.getFirst());
        String fingerprint = modelIdToFingerprint.getOrDefault(modelId, "none");
        WynntilsMod.info("id: " + modelId
                + " png: " + firstName
                + " texturehash: " + textureHash
                + " fingerprint: " + fingerprint
                + (textureIds.size() > 1 ? " (+" + (textureIds.size() - 1) + " more)" : ""));
    }

    private boolean buildModelMap() {
        ResourceManager resourceManager = McUtils.mc().getResourceManager();

        boolean succeeded = ResourcepackUtils.forEachBoatModelOverride(resourceManager, (cmd, modelPath) -> {
            ResourcepackUtils.ModelData model = ResourcepackUtils.parseModelData(resourceManager, modelPath);
            if (model == null) return;
            modelIdToFingerprint.put(cmd, model.fingerprint());
            if (!model.textureIds().isEmpty()) {
                modelIdToTextureIds.put(cmd, model.textureIds());
            }
        });

        if (!succeeded) {
            WynntilsMod.error("Could not find oak_boat.json override file!");
        }
        return succeeded && !modelIdToTextureIds.isEmpty();
    }

    private void saveResults() {
        saveToDisk();
        saveTexturePngs();
        lastSeenModelToIds.clear();
        WynntilsMod.info("Recording finished. Results saved.");
    }

    private void saveToDisk() {
        File textFile = new File(SAVE_FOLDER, OUTPUT_FILE);
        FileUtils.mkdir(textFile.getParentFile());

        ResourceManager resourceManager = McUtils.mc().getResourceManager();
        Map<Identifier, String> hashCache = new HashMap<>();

        try (PrintWriter writer =
                new PrintWriter(new OutputStreamWriter(new FileOutputStream(textFile), StandardCharsets.UTF_8))) {
            writer.println("=== Texture Recorder Debug Results ===");
            writer.println("Seen " + lastSeenModelToIds.size() + " unique model IDs");
            writer.println();

            writer.println("--- Seen textures summary ---");
            for (Map.Entry<Float, List<Identifier>> entry : lastSeenModelToIds.entrySet()) {
                float modelId = entry.getKey();
                List<Identifier> textureIds = entry.getValue();
                String textureHash = hashCache.computeIfAbsent(
                        textureIds.getFirst(), id -> ResourcepackUtils.computeTextureHash(resourceManager, id));
                writer.println("  " + getLabel(textureIds)
                        + " (model ID: " + modelId
                        + ", texturehash: " + textureHash
                        + ", fingerprint: " + modelIdToFingerprint.getOrDefault(modelId, "none") + ")");
            }
            writer.println();

            for (Map.Entry<Float, List<Identifier>> entry : lastSeenModelToIds.entrySet()) {
                float modelId = entry.getKey();
                List<Identifier> textureIds = entry.getValue();
                writer.println("Texture: " + getLabel(textureIds));
                writer.println("  model ID: " + modelId + "   fingerprint: "
                        + modelIdToFingerprint.getOrDefault(modelId, "none"));
                for (Identifier texId : textureIds) {
                    String textureHash =
                            hashCache.computeIfAbsent(texId, id -> ResourcepackUtils.computeTextureHash(resourceManager, id));
                    writer.println("  texture: " + texId + "   texturehash: " + textureHash);
                }
                writer.println();
            }
        } catch (IOException e) {
            WynntilsMod.error("Failed to save texture mapping dump " + textFile, e);
        }

        McUtils.sendWynntilsPrefixMessage(
                Component.literal("Saved texture mapping dump to " + textFile.getAbsolutePath()));
    }

    private void saveTexturePngs() {
        ResourceManager resourceManager = McUtils.mc().getResourceManager();
        FileUtils.mkdir(TEXTURES_FOLDER);

        Set<Identifier> uniqueTextures = new LinkedHashSet<>();
        for (List<Identifier> ids : lastSeenModelToIds.values()) {
            uniqueTextures.addAll(ids);
        }

        int exportedCount = 0;
        for (Identifier textureId : uniqueTextures) {
            Optional<Resource> resource = resourceManager.getResource(textureId);
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

    private static String getFilename(Identifier id) {
        String path = id.getPath();
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.endsWith(".png") ? name.substring(0, name.length() - 4) : name;
    }

    private static String getLabel(List<Identifier> textureIds) {
        if (textureIds == null || textureIds.isEmpty()) return "(unknown)";
        String firstName = getFilename(textureIds.getFirst());
        return textureIds.size() == 1 ? firstName : firstName + " (+" + (textureIds.size() - 1) + " more)";
    }
}
