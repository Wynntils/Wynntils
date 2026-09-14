/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterCommand;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.services.map.MapTexture;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.features.type.MapLocation;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.BoundingBox;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.MAP)
public class MapFogOfWarFeature extends Feature {
    private static final int SAMPLE_INTERVAL_TICKS = 20;
    private static final int MIN_REVEAL_RADIUS = 1;
    private static final int MAX_REVEAL_RADIUS = 8;
    private static final List<String> STATIC_CONTENT_CATEGORIES =
            List.of("wynntils:place", "wynntils:service", "wynntils:content", "wynntils:gathering");

    @RegisterCommand
    private final LiteralCommandNode<CommandSourceStack> resetCommand =
            Commands.literal("reset").executes(this::resetCurrentCharacter).build();

    @Persisted
    public final Config<Boolean> fogMinimap = new Config<>(true);

    @Persisted
    private final Config<Float> fogOpacity = new Config<>(1f);

    @Persisted
    private final Config<Integer> revealRadius = new Config<>(3);

    @Persisted
    private final Config<Boolean> hideStaticContent = new Config<>(true);

    // Character id -> chunk keys. Concrete map type so Gson deserialises into one that is safe to serialise on the
    // storage thread while the game thread reveals into it
    @Persisted
    private final Storage<ConcurrentHashMap<String, Set<Long>>> discoveredChunks =
            new Storage<>(new ConcurrentHashMap<>());

    private final FogMasks fogMasks = new FogMasks();

    private String currentCharacterId;
    private DiscoveryRecord currentRecord;
    private int ticksUntilSample = 0;

    public MapFogOfWarFeature() {
        super(new ProfileDefault.Builder().enabledFor(ConfigProfile.NEW_PLAYER).build());
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (--ticksUntilSample > 0) return;
        ticksUntilSample = SAMPLE_INTERVAL_TICKS;

        Optional<DiscoveryRecord> record = activeRecord();
        if (record.isEmpty() || !Models.WorldState.onWorld() || Models.Housing.isOnHousing()) return;

        double x = McUtils.player().getX();
        double z = McUtils.player().getZ();
        BoundingBox playerBlock = new BoundingBox((float) x, (float) z, (float) x + 1, (float) z + 1);
        if (Services.Map.getMapsForBoundingBox(playerBlock).isEmpty()) return;

        int radius = MathUtils.clamp(revealRadius.get(), MIN_REVEAL_RADIUS, MAX_REVEAL_RADIUS);
        if (!record.get().reveal(x, z, radius)) return;

        discoveredChunks.touched();
        fogMasks.invalidate();
    }

    /** The current character's record while fog should apply; empty when disabled or no character is selected. */
    public Optional<DiscoveryRecord> activeRecord() {
        if (!isEnabled() || !Models.Character.hasCharacter()) return Optional.empty();

        String characterId = Models.Character.getId();
        if (!characterId.equals(currentCharacterId)) {
            currentCharacterId = characterId;
            currentRecord = new DiscoveryRecord(discoveredChunks.get().getOrDefault(characterId, Set.of()));
            discoveredChunks.get().put(characterId, currentRecord.chunks());
            fogMasks.invalidate();
        }
        return Optional.of(currentRecord);
    }

    /** What to draw over a tile, or empty when fog should not be drawn. */
    public Optional<FogOverlay> fogOverlay(MapTexture map) {
        return activeRecord()
                .map(record -> new FogOverlay(fogMasks.maskFor(map, record), FogMasks.PADDING_BLOCKS, fogColor()));
    }

    private CustomColor fogColor() {
        return CommonColors.BLACK.withAlpha(Math.round(MathUtils.clamp(fogOpacity.get(), 0f, 1f) * 255));
    }

    /** Drops static world content (places, services, combat, gathering) whose location is not discovered. */
    public Stream<MapFeature> withoutUndiscovered(Stream<MapFeature> features) {
        Optional<DiscoveryRecord> record = activeRecord();
        if (record.isEmpty() || !hideStaticContent.get()) return features;

        return features.filter(feature -> !(feature instanceof MapLocation location)
                || !isStaticWorldContent(location.getCategoryId())
                || record.get()
                        .isDiscovered(
                                location.getLocation().x(),
                                location.getLocation().z()));
    }

    private static boolean isStaticWorldContent(String categoryId) {
        return STATIC_CONTENT_CATEGORIES.stream().anyMatch(categoryId::startsWith);
    }

    private int resetCurrentCharacter(CommandContext<CommandSourceStack> context) {
        activeRecord().ifPresent(record -> {
            record.clear();
            discoveredChunks.touched();
            fogMasks.invalidate();
        });
        return 1;
    }
}
