/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.athena;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Service;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.screens.compatibility.CompatibilityWarningScreen;
import com.wynntils.services.athena.actionbar.matchers.WynncraftVersionSegmentMatcher;
import com.wynntils.services.athena.actionbar.segments.WynncraftVersionSegment;
import com.wynntils.services.athena.type.CompatibilityTier;
import com.wynntils.services.athena.type.WynncraftVersion;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

public class CompatibilityService extends Service {
    private CompatibilityTier compatibilityTier = null;
    private WynncraftVersion wynncraftVersion = null;

    // Wynncraft version to ignore incompatibility checks for, Wynntils version used
    @Persisted
    private final Storage<Pair<String, String>> overrideIncompatibility = new Storage<>(Pair.of("", ""));

    public CompatibilityService() {
        super(List.of());

        Handlers.ActionBar.registerSegment(new WynncraftVersionSegmentMatcher());
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresent(WynncraftVersionSegment.class, this::handleVersion);
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.WORLD && event.isFirstJoinWorld()) {
            if (compatibilityTier.shouldChatPrompt()) {
                McUtils.sendMessageToClientWithPillHeader(Component.translatable(
                                compatibilityTier.getChatPromptKey(),
                                WynntilsMod.getVersion(),
                                wynncraftVersion.toString())
                        .withStyle(ChatFormatting.RED)
                        .withStyle(ChatFormatting.BOLD));
            }
        }
    }

    public boolean isCompatible() {
        // Not reached character selection screen yet
        if (wynncraftVersion == null) return true;
        if (compatibilityTier == CompatibilityTier.INCOMPATIBLE) return false;
        if (compatibilityTier == CompatibilityTier.UNKNOWN
                || compatibilityTier == CompatibilityTier.COMPATIBLE
                || compatibilityTier == CompatibilityTier.MINOR_ERRORS) return true;

        // CompatibilityTier.MAJOR_ERRORS
        return overrideIncompatibility.get().a().equals(wynncraftVersion.toString())
                && overrideIncompatibility.get().b().equals(WynntilsMod.getVersion());
    }

    public WynncraftVersion getWynncraftVersion() {
        return wynncraftVersion;
    }

    private void handleVersion(WynncraftVersionSegment segment) {
        wynncraftVersion = segment.getWynncraftVersion();

        // TODO: Replace with Athena call
        compatibilityTier = CompatibilityTier.COMPATIBLE;

        if (compatibilityTier == CompatibilityTier.INCOMPATIBLE) {
            // This has to be done on the main thread
            McUtils.mc().execute(() -> McUtils.mc().setScreen(CompatibilityWarningScreen.create()));
        }
    }
}
