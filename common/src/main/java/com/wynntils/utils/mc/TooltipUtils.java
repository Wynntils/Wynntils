/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public final class TooltipUtils {
    public static int getToolTipWidth(List<ClientTooltipComponent> lines, Font font) {
        return lines.stream()
                .map(clientTooltipComponent -> clientTooltipComponent.getWidth(font))
                .max(Integer::compareTo)
                .orElse(0);
    }

    public static int getToolTipHeight(List<ClientTooltipComponent> lines) {
        return (lines.size() == 1 ? -2 : 0)
                + lines.stream()
                        .map(ClientTooltipComponent::getHeight)
                        .mapToInt(Integer::intValue)
                        .sum();
    }

    public static List<ClientTooltipComponent> componentToClientTooltipComponent(List<Component> components) {
        return components.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();
    }
}
