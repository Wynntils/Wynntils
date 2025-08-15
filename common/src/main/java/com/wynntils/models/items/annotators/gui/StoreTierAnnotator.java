/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.StoreItem;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

public final class StoreTierAnnotator implements GuiItemAnnotator {
    private static final Pattern TIER_PATTERN = Pattern.compile("store_tier_(.+)");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (customModelData == null) return null;

        List<String> strings = customModelData.strings();
        if (strings.isEmpty()) return null;

        String tierName = strings.stream()
                .map(TIER_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group(1))
                .findFirst()
                .orElse(null);
        if (tierName == null) return null;

        StoreTier tier = StoreTier.parseTier(tierName);
        if (tier == null) {
            WynntilsMod.warn("Unknown store tier: " + tierName);
        }

        return new StoreItem(tier == null ? CommonColors.WHITE : tier.getHighlightColor());
    }

    private enum StoreTier {
        BLACK_MARKET(CustomColor.fromInt(0x640404)),
        GODLY(CustomColor.fromInt(0xeb2d2d)),
        EPIC(CustomColor.fromInt(0xffbb00)),
        RARE(CustomColor.fromInt(0xdd55ff)),
        COMMON(CustomColor.fromInt(0xfffddd));

        private final CustomColor highlightColor;

        StoreTier(CustomColor highlightColor) {
            this.highlightColor = highlightColor;
        }

        public static StoreTier parseTier(String tierName) {
            for (StoreTier tier : StoreTier.values()) {
                if (tier.name().equalsIgnoreCase(tierName)) {
                    return tier;
                }
            }

            return null;
        }

        public CustomColor getHighlightColor() {
            return highlightColor;
        }
    }
}
