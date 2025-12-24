/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterCommand;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.RenderState;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.overlays.stopwatch.StopwatchOverlay;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.OVERLAYS)
public class StopwatchFeature extends Feature {
    @RegisterKeyBind
    private final KeyBind toggleStopwatchKeybind =
            new KeyBind("Toggle Stopwatch", GLFW.GLFW_KEY_KP_0, true, this::toggleStopwatch);

    @RegisterKeyBind
    private final KeyBind resetStopwatchKeybind =
            new KeyBind("Reset Stopwatch", GLFW.GLFW_KEY_KP_DECIMAL, true, Services.Stopwatch::reset);

    @RegisterCommand
    private final LiteralCommandNode<CommandSourceStack> startCommand = Commands.literal("start")
            .executes(ctx -> {
                Services.Stopwatch.start();
                return 0;
            })
            .build();

    @RegisterCommand
    private final LiteralCommandNode<CommandSourceStack> pauseCommand = Commands.literal("pause")
            .executes(ctx -> {
                if (Services.Stopwatch.isRunning()) {
                    Services.Stopwatch.pause();
                }
                return 0;
            })
            .build();

    @RegisterCommand
    private final LiteralCommandNode<CommandSourceStack> resetCommand = Commands.literal("reset")
            .executes(ctx -> {
                Services.Stopwatch.reset();
                return 0;
            })
            .build();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.PRE)
    private final Overlay stopwatchOverlay = new StopwatchOverlay();

    public StopwatchFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(
                        ConfigProfile.NEW_PLAYER, ConfigProfile.LITE, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    private void toggleStopwatch() {
        if (Services.Stopwatch.isRunning()) {
            Services.Stopwatch.pause();
        } else {
            Services.Stopwatch.start();
        }
    }
}
