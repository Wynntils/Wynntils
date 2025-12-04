/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.StoreItem;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.LoreUtils;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class StoreTierAnnotator implements GuiItemAnnotator {
    private static final ResourceLocation RARITY_FONT = ResourceLocation.withDefaultNamespace("banner/box");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        List<StyledText> lore = LoreUtils.getLore(itemStack);
        if (lore.isEmpty()) return null;

        StyledText firstLine = lore.getFirst();
        StyledTextPart firstPart = firstLine.getFirstPart();

        if (firstPart == null) return null;
        if (!firstPart.getPartStyle().getFont().equals(RARITY_FONT)) return null;

        StoreTier tier = StoreTier.parseTier(firstLine.getStringWithoutFormatting());
        if (tier == null) return null;

        return new StoreItem(tier.getHighlightColor());
    }

    private enum StoreTier {
        BLACK_MARKET(
                "\uE060\uDAFF\uDFFF\uE031\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE03A\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE03C\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE03A\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE062\uDAFF\uDFB8\uE001\uE00B\uE000\uE002\uE00A \uE00C\uE000\uE011\uE00A\uE004\uE013\uDB00\uDC02",
                CustomColor.fromInt(0x640404)),
        GODLY(
                "\uE060\uDAFF\uDFFF\uE036\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE033\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE048\uDAFF\uDFFF\uE062\uDAFF\uDFE0\uE006\uE00E\uE003\uE00B\uE018\uDB00\uDC02",
                CustomColor.fromInt(0xeb2d2d)),
        EPIC(
                "\uE060\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE03F\uDAFF\uDFFF\uE038\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE062\uDAFF\uDFE8\uE004\uE00F\uE008\uE002\uDB00\uDC02",
                CustomColor.fromInt(0xffbb00)),
        RARE(
                "\uE060\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE062\uDAFF\uDFE6\uE011\uE000\uE011\uE004\uDB00\uDC02",
                CustomColor.fromInt(0xdd55ff)),
        COMMON(
                "\uE060\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE03C\uDAFF\uDFFF\uE03C\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE03D\uDAFF\uDFFF\uE062\uDAFF\uDFDA\uE002\uE00E\uE00C\uE00C\uE00E\uE00D\uDB00\uDC02",
                CustomColor.fromInt(0xfffddd));

        private final String rarityText;
        private final CustomColor highlightColor;

        StoreTier(String rarityText, CustomColor highlightColor) {
            this.rarityText = rarityText;
            this.highlightColor = highlightColor;
        }

        public static StoreTier parseTier(String rarityText) {
            for (StoreTier tier : StoreTier.values()) {
                if (rarityText.startsWith(tier.rarityText)) {
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
