/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public abstract class GuideItemStack extends ItemStack {
    protected GuideItemStack(ItemStack itemStack, ItemAnnotation annotation, String baseName) {
        super(itemStack.getItem(), 1);
        this.applyComponents(itemStack.getComponentsPatch());
        Handlers.Item.updateItem(this, annotation, StyledText.fromString(baseName));
    }

    protected void appendObtainInfo(List<Component> tooltipLines, List<ItemObtainInfo> itemObtainInfos) {
        tooltipLines.add(Component.empty());
        tooltipLines.add(Component.literal("Obtain from:").withStyle(ChatFormatting.GRAY));
        for (ItemObtainInfo obtainInfo : itemObtainInfos) {
            MutableComponent obtainSourceType =
                    Component.literal(obtainInfo.sourceType().getDisplayName());
            if (obtainInfo.name().isPresent()) {
                obtainSourceType.append(Component.literal(": ").withStyle(ChatFormatting.GRAY));
                obtainSourceType.append(
                        Component.literal(obtainInfo.name().get()).withStyle(ChatFormatting.YELLOW));
            }
            tooltipLines.add(obtainSourceType);
        }
    }

    protected void appendFavoriteInfo(List<Component> tooltipLines) {
        tooltipLines.add(Component.empty());
        if (Services.Favorites.isFavorite(this)) {
            MutableComponent keybind = Component.empty()
                    .append(WynnFont.asFont("key_shift", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(WynnFont.asFont("key_plus", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.unfavorite")
                            .withStyle(ChatFormatting.YELLOW));

            tooltipLines.add(keybind);
        } else {
            MutableComponent keybind = Component.empty()
                    .append(WynnFont.asFont("key_shift", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(WynnFont.asFont("key_plus", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.favorite")
                            .withStyle(ChatFormatting.GREEN));

            tooltipLines.add(keybind);
        }
    }

    protected void appendWebGuideInfo(List<Component> tooltipLines) {
        MutableComponent keybind = Component.empty()
                .append(WynnFont.asFont("key_shift", WynncraftKeybindsFont.class))
                .append(" ")
                .append(WynnFont.asFont("key_plus", WynncraftKeybindsFont.class))
                .append(" ")
                .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class))
                .append(" ")
                .append(Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.open")
                        .withStyle(ChatFormatting.RED));

        tooltipLines.add(keybind);
    }
}
