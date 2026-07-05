/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.utils.render.FontRenderer;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public final class TooltipUtils {
    public static int getTooltipWidth(List<ClientTooltipComponent> lines, Font font) {
        return lines.stream()
                .map(clientTooltipComponent -> clientTooltipComponent.getWidth(font))
                .max(Integer::compareTo)
                .orElse(0);
    }

    public static int getTooltipHeight(List<ClientTooltipComponent> lines) {
        return (lines.size() == 1 ? -2 : 0)
                + lines.stream()
                        .mapToInt(clientTooltip -> clientTooltip.getHeight(
                                FontRenderer.getInstance().getFont()))
                        .sum();
    }

    public static List<ClientTooltipComponent> getClientTooltipComponent(List<Component> components) {
        return components.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();
    }

    public static List<Component> getWynnItemTooltip(ItemStack itemStack, WynnItem wynnItem) {
        TooltipBuilder tooltipBuilder = wynnItem.getData().get(WynnItemData.TOOLTIP_KEY);
        if (tooltipBuilder != null) {
            return tooltipBuilder.getTooltipLines(Models.Character.getClassType());
        }

        return LoreUtils.getTooltipLines(itemStack);
    }

    public static boolean containsFont(Component component, FontDescription font) {
        if (font.equals(component.getStyle().getFont())) {
            return true;
        }

        for (Component sibling : component.getSiblings()) {
            if (containsFont(sibling, font)) {
                return true;
            }
        }

        return false;
    }

    public static int findFirstLineWithFont(List<Component> tooltips, FontDescription font) {
        for (int i = 0; i < tooltips.size(); i++) {
            if (containsFont(tooltips.get(i), font)) {
                return i;
            }
        }

        return -1;
    }

    public static int findFirstNonBlankLine(List<Component> tooltips) {
        for (int i = 0; i < tooltips.size(); i++) {
            if (!tooltips.get(i).getString().isBlank()) {
                return i;
            }
        }

        return -1;
    }

    public static boolean replaceTrailingTitleComponent(
            MutableComponent line, String itemName, MutableComponent replacement) {
        List<Component> siblings = line.getSiblings();
        for (int i = siblings.size() - 1; i >= 0; i--) {
            String siblingText = siblings.get(i).getString().trim();
            if (!siblingText.equals(itemName) && !siblingText.endsWith(itemName)) {
                continue;
            }

            siblings.set(i, replacement);
            return true;
        }

        return false;
    }
}
