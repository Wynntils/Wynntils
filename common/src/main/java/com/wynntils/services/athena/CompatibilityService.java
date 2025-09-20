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
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.screens.compatibility.CompatibilityWarningScreen;
import com.wynntils.services.athena.actionbar.matchers.WynncraftVersionSegmentMatcher;
import com.wynntils.services.athena.actionbar.segments.WynncraftVersionSegment;
import com.wynntils.services.athena.type.CompatibilityTier;
import com.wynntils.services.athena.type.WynncraftVersion;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import java.util.List;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class CompatibilityService extends Service {
    private static final long TOAST_DISPLAY_TIME = 10000L;
    private static final ResourceLocation WYNNTILS_KEYBIND_FONT =
            ResourceLocation.fromNamespaceAndPath("wynntils", "keybind");

    private CompatibilityTier compatibilityTier = null;
    private WynncraftVersion wynncraftVersion = null;

    private long toastExpire = 0L;
    private SystemToast warningToast = null;

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
        if (WynntilsMod.isDevelopmentEnvironment() || WynntilsMod.isDevelopmentBuild()) return;

        if (event.getNewState() == WorldState.WORLD && event.isFirstJoinWorld()) {
            if (compatibilityTier.shouldChatPrompt()) {
                MutableComponent toastMessage = Component.empty()
                        .append(Component.translatable("service.wynntils.compatibility.toastMessage1"))
                        .append(Component.literal("Y").withStyle(Style.EMPTY.withFont(WYNNTILS_KEYBIND_FONT)))
                        .append(Component.translatable("service.wynntils.compatibility.toastMessage2"));
                warningToast = new SystemToast(
                        new SystemToast.SystemToastId(TOAST_DISPLAY_TIME),
                        Component.translatable("service.wynntils.compatibility.toastTitle"),
                        toastMessage);
                McUtils.mc().getToastManager().addToast(warningToast);
                toastExpire = System.currentTimeMillis() + TOAST_DISPLAY_TIME;
            }
        } else if (event.getNewState() == WorldState.NOT_CONNECTED) {
            wynncraftVersion = null;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_Y) && System.currentTimeMillis() <= toastExpire) {
            warningToast.forceHide();
            toastExpire = 0L;
            warningToast = null;

            McUtils.mc().setScreen(CompatibilityWarningScreen.create(compatibilityTier));
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

        if (WynntilsMod.isDevelopmentEnvironment()
                || WynntilsMod.isDevelopmentBuild()
                || wynncraftVersion.equals(WynncraftVersion.DEV)) {
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
