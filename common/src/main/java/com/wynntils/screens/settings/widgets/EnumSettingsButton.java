/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.persisted.config.Config;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class EnumSettingsButton<E extends Enum<E>> extends GeneralSettingsButton {
    private final Config<E> config;
    private final List<E> enumConstants;

    public EnumSettingsButton(int x, int y, Config<E> config, int maskTopY, int maskBottomY) {
        super(
                x,
                y,
                90,
                20,
                Component.literal(config.getValueString()),
                ComponentUtils.wrapTooltips(List.of(Component.literal(config.getDescription())), 150),
                maskTopY,
                maskBottomY);
        this.config = config;
        enumConstants = EnumSet.allOf((Class<E>) config.getType()).stream().toList();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;

        int addToIndex;

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            addToIndex = 1;
        } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            addToIndex = -1;
        } else {
            return false;
        }

        E value = config.get();
        int nextIndex = (enumConstants.indexOf(value) + addToIndex + enumConstants.size()) % enumConstants.size();
        E nextValue = enumConstants.get(nextIndex);
        config.setValue(nextValue);
        setMessage(Component.literal(config.getValueString()));

        playDownSound(McUtils.mc().getSoundManager());

        return true;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        // We use instead AbstractWidget#mouseClicked, because we also want to have an action on the right mouse button
    }
}
