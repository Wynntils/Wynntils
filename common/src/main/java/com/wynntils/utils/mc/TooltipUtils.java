/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.utils.render.FontRenderer;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
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
        if (itemStack instanceof FakeItemStack fakeItemStack) {
            return fakeItemStack.getWynnTooltip();
        }

        Optional<IdentifiableItemProperty> identifiableItemPropertyOpt =
                Models.Item.asWynnItemProperty(itemStack, IdentifiableItemProperty.class);
        if (identifiableItemPropertyOpt.isPresent()) {
            return getCachedTooltip(
                    wynnItem, () -> Handlers.Tooltip.buildFromItemStack(itemStack, identifiableItemPropertyOpt.get()));
        }

        Optional<CraftedGearItem> craftedGearItemOpt = Models.Item.asWynnItemProperty(itemStack, CraftedGearItem.class);
        if (craftedGearItemOpt.isPresent()) {
            return getCachedTooltip(
                    wynnItem, () -> Handlers.Tooltip.buildFromItemStack(itemStack, craftedGearItemOpt.get()));
        }

        Optional<CraftedConsumableItem> craftedConsumableItemOpt =
                Models.Item.asWynnItemProperty(itemStack, CraftedConsumableItem.class);
        if (craftedConsumableItemOpt.isPresent()) {
            return getCachedTooltip(
                    wynnItem, () -> Handlers.Tooltip.buildFromItemStack(itemStack, craftedConsumableItemOpt.get()));
        }

        Optional<MountItem> mountItemOpt = Models.Item.asWynnItem(itemStack, MountItem.class);
        if (mountItemOpt.isPresent()) {
            return getMountItemTooltip(itemStack, wynnItem);
        }

        return List.of();
    }

    private static List<Component> getCachedTooltip(WynnItem wynnItem, Supplier<TooltipBuilder> builderFactory) {
        TooltipBuilder builder = wynnItem.getData().getOrCalculate(WynnItemData.TOOLTIP_KEY, builderFactory);
        if (builder == null) return null;

        return builder.getTooltipLines(Models.Character.getClassType());
    }

    private static List<Component> getMountItemTooltip(ItemStack itemStack, WynnItem wynnItem) {
        // Mounts currently have no usage for building from an itemstack so if we don't have a cached tooltip there's
        // nothing to calculate, just use the itemstack tooltip
        TooltipBuilder builder = wynnItem.getData().get(WynnItemData.TOOLTIP_KEY);
        if (builder == null) return LoreUtils.getTooltipLines(itemStack);

        return builder.getTooltipLines(Models.Character.getClassType());
    }
}
