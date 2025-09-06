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
import net.neoforged.bus.api.EventPriority;
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

    // Lowest so it will be the most recent message for all of the on join messages we send
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWorldStateChange(WorldStateEvent event) {
        if (WynntilsMod.isDevelopmentEnvironment() || WynntilsMod.isDevelopmentBuild()) return;

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

    public void setOverrideIncompatibility() {
        overrideIncompatibility.store(Pair.of(wynncraftVersion.toString(), WynntilsMod.getVersion()));
        overrideIncompatibility.touched();
    }

    public WynncraftVersion getWynncraftVersion() {
        return wynncraftVersion;
    }

    private void handleVersion(WynncraftVersionSegment segment) {
        if (segment.getWynncraftVersion().equals(wynncraftVersion)) return;

        wynncraftVersion = segment.getWynncraftVersion();

        if (WynntilsMod.isDevelopmentEnvironment() || WynntilsMod.isDevelopmentBuild()) {
            compatibilityTier = CompatibilityTier.UNKNOWN;
            return;
        }

        // TODO: Replace with Athena call
        compatibilityTier = CompatibilityTier.COMPATIBLE;

        if (compatibilityTier.shouldScreenPrompt() && !isCompatible()) {
            // This has to be done on the main thread
            McUtils.mc().execute(() -> McUtils.mc().setScreen(CompatibilityWarningScreen.create(compatibilityTier)));
        }
    }
}
