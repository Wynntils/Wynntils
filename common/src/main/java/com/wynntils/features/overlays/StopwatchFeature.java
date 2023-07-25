/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.components.Services;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.overlays.Overlay;
import com.wynntils.core.consumers.features.overlays.OverlayPosition;
import com.wynntils.core.consumers.features.overlays.OverlaySize;
import com.wynntils.core.consumers.features.overlays.TextOverlay;
import com.wynntils.core.consumers.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.consumers.features.properties.RegisterCommand;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
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

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay stopwatchOverlay = new StopwatchOverlay();

    private void toggleStopwatch() {
        if (Services.Stopwatch.isRunning()) {
            Services.Stopwatch.pause();
        } else {
            Services.Stopwatch.start();
        }
    }

    public static class StopwatchOverlay extends TextOverlay {
        private static final String TEMPLATE =
                "{if_str(stopwatch_zero;\"\";concat(if_str(stopwatch_running;\"\";\"&e\");leading_zeros(stopwatch_hours;2);\":\";leading_zeros(stopwatch_minutes;2);\":\";leading_zeros(stopwatch_seconds;2);\".\";leading_zeros(stopwatch_milliseconds;3)))}";

        protected StopwatchOverlay() {
            super(
                    new OverlayPosition(
                            0,
                            0,
                            VerticalAlignment.BOTTOM,
                            HorizontalAlignment.LEFT,
                            OverlayPosition.AnchorSection.BOTTOM_LEFT),
                    new OverlaySize(100, 20),
                    HorizontalAlignment.CENTER,
                    VerticalAlignment.MIDDLE);
        }

        @Override
        public String getTemplate() {
            return TEMPLATE;
        }

        @Override
        public String getPreviewTemplate() {
            return "01:24:31.877";
        }
    }
}
