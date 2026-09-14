/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
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
import com.wynntils.services.mapdata.fog.DiscoveryRecord;
import com.wynntils.services.mapdata.fog.FogMasks;
import com.wynntils.services.mapdata.fog.FogOverlay;
import com.wynntils.services.mapdata.fog.FogStyle;
import com.wynntils.services.mapdata.fog.RevealShape;
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
import net.neoforged.bus.api.SubscribeEvent;

/** Sub-feature of {@link MainMapFeature}; the main map and the minimap both read it. */
@ConfigCategory(Category.MAP)
public class MapFogOfWarFeature extends Feature {
    private static final int SAMPLE_INTERVAL_TICKS = 20;
    private static final int MIN_REVEAL_RADIUS = 1;
    private static final int MAX_REVEAL_RADIUS = 8;
    private static final String NO_CHARACTER_ID = "-";
    private static final List<String> STATIC_CONTENT_CATEGORIES =
            List.of("wynntils:place", "wynntils:service", "wynntils:content", "wynntils:gathering");

    @Persisted
    public final Config<FogStyle> fogStyle = new Config<>(FogStyle.PARCHMENT);

    @Persisted
    public final Config<Boolean> fogMinimap = new Config<>(true);

    @Persisted
    private final Config<Float> fogOpacity = new Config<>(0.85f);

    @Persisted
    private final Config<Integer> revealRadius = new Config<>(3);

    @Persisted
    private final Config<RevealShape> revealShape = new Config<>(RevealShape.CIRCLE);

    @Persisted
    private final Config<Boolean> hideUndiscoveredContent = new Config<>(true);

    // Character id -> discovered chunk keys. Concrete map type so Gson deserialises into one that is safe to
    // serialise on the storage thread while the game thread reveals into it
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
        if (!record.get().reveal(x, z, radius, revealShape.get())) return;

        discoveredChunks.touched();
        fogMasks.invalidate();
    }

    /** What to draw over a tile, or empty when fog should not be drawn. */
    public Optional<FogOverlay> fogOverlay(MapTexture map) {
        return activeRecord()
                .map(record -> new FogOverlay(
                        fogStyle.get(), fogMasks.maskFor(map, record), FogMasks.PADDING_BLOCKS, fogColor()));
    }

    /** Drops static world content (places, services, combat, gathering) whose location is not discovered. */
    public Stream<MapFeature> withoutUndiscovered(Stream<MapFeature> features) {
        Optional<DiscoveryRecord> record = activeRecord();
        if (record.isEmpty() || !hideUndiscoveredContent.get()) return features;

        return features.filter(feature -> !(feature instanceof MapLocation location)
                || !isStaticWorldContent(location.getCategoryId())
                || record.get()
                        .isDiscovered(
                                location.getLocation().x(),
                                location.getLocation().z()));
    }

    /** @return whether there was a current character record to reset */
    public boolean resetCurrentCharacter() {
        Optional<DiscoveryRecord> record = activeRecord();
        if (record.isEmpty()) return false;

        record.get().clear();
        discoveredChunks.touched();
        fogMasks.invalidate();
        return true;
    }

    @Override
    public void onDisable() {
        fogMasks.release();
        currentCharacterId = null;
        currentRecord = null;
    }

    /** The current character's record while fog should apply; empty when disabled or no character is selected. */
    private Optional<DiscoveryRecord> activeRecord() {
        if (!isEnabled() || !Models.Character.hasCharacter()) return Optional.empty();

        // The id is still the placeholder between selecting a character and the character info scan
        String characterId = Models.Character.getId();
        if (characterId.equals(NO_CHARACTER_ID)) return Optional.empty();

        if (!characterId.equals(currentCharacterId)) {
            currentCharacterId = characterId;
            currentRecord = new DiscoveryRecord(discoveredChunks.get().getOrDefault(characterId, Set.of()));
            discoveredChunks.get().put(characterId, currentRecord.chunks());
            fogMasks.invalidate();
        }
        return Optional.of(currentRecord);
    }

    private CustomColor fogColor() {
        CustomColor base = fogStyle.get() == FogStyle.DARKEN ? CommonColors.BLACK : CommonColors.WHITE;
        return base.withAlpha(Math.round(MathUtils.clamp(fogOpacity.get(), 0f, 1f) * 255));
    }

    private static boolean isStaticWorldContent(String categoryId) {
        for (String category : STATIC_CONTENT_CATEGORIES) {
            if (categoryId.startsWith(category)) return true;
        }
        return false;
    }
}
