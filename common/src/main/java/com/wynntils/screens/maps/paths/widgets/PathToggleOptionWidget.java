/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.paths.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.screens.maps.managers.widgets.categorymanager.options.ToggleOptionWidget;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class PathToggleOptionWidget extends ToggleOptionWidget {
    public PathToggleOptionWidget(
            Component label, Component description, Function<MapAttributes, Optional<Boolean>> getter) {
        super(label, description, OptionCategory.PATH, getter);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return event.button() == InputConstants.MOUSE_BUTTON_LEFT && super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public List<Component> getTooltipLines(double mouseX, double mouseY) {
        if (!isMouseOverLabel(mouseX, mouseY)) return List.of();

        return List.of(
                getMessage().copy().withStyle(ChatFormatting.GOLD),
                description.copy().withStyle(ChatFormatting.GRAY));
    }
}
