/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

import com.wynntils.core.WynntilsMod;
import com.wynntils.features.user.inventory.ItemHighlightFeature;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.utils.StringUtils;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Callable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum ItemTier {
    NORMAL(
            ChatFormatting.WHITE,
            () -> ItemHighlightFeature.INSTANCE.normalHighlightColor,
            () -> ItemHighlightFeature.INSTANCE.normalHighlightEnabled,
            -1,
            0),
    UNIQUE(
            ChatFormatting.YELLOW,
            () -> ItemHighlightFeature.INSTANCE.uniqueHighlightColor,
            () -> ItemHighlightFeature.INSTANCE.uniqueHighlightEnabled,
            3,
            0.5f),
    RARE(
            ChatFormatting.LIGHT_PURPLE,
            () -> ItemHighlightFeature.INSTANCE.rareHighlightColor,
            () -> ItemHighlightFeature.INSTANCE.rareHighlightEnabled,
            8,
            1.2f),
    SET(
            ChatFormatting.GREEN,
            () -> ItemHighlightFeature.INSTANCE.setHighlightColor,
            () -> ItemHighlightFeature.INSTANCE.setHighlightEnabled,
            8,
            1.2f),
    FABLED(
            ChatFormatting.RED,
            () -> ItemHighlightFeature.INSTANCE.fabledHighlightColor,
            () -> ItemHighlightFeature.INSTANCE.fabledHighlightEnabled,
            12,
            4.5f),
    LEGENDARY(
            ChatFormatting.AQUA,
            () -> ItemHighlightFeature.INSTANCE.legendaryHighlightColor,
            () -> ItemHighlightFeature.INSTANCE.legendaryHighlightEnabled,
            16,
            8.0f),
    MYTHIC(
            ChatFormatting.DARK_PURPLE,
            () -> ItemHighlightFeature.INSTANCE.mythicHighlightColor,
            () -> ItemHighlightFeature.INSTANCE.mythicHighlightEnabled,
            90,
            18.0f),
    CRAFTED(
            ChatFormatting.DARK_AQUA,
            () -> ItemHighlightFeature.INSTANCE.craftedHighlightColor,
            () -> ItemHighlightFeature.INSTANCE.craftedHighlightEnabled,
            -1,
            0);

    private final ChatFormatting chatFormatting;
    private final Callable<CustomColor> highlightColor;
    private final Callable<Boolean> highlightEnabled;
    private final int baseCost;
    private final float costMultiplier;

    ItemTier(
            ChatFormatting chatFormatting,
            Callable<CustomColor> highlightColor,
            Callable<Boolean> highlightEnabled,
            int baseCost,
            float costMultiplier) {
        this.chatFormatting = chatFormatting;
        this.highlightColor = highlightColor;
        this.highlightEnabled = highlightEnabled;
        this.baseCost = baseCost;
        this.costMultiplier = costMultiplier;
    }

    public ChatFormatting getChatFormatting() {
        return chatFormatting;
    }

    public boolean isHighlightEnabled() {
        try {
            return highlightEnabled.call();
        } catch (Exception e) {
            WynntilsMod.error(e.getMessage());
            return false;
        }
    }

    public CustomColor getHighlightColor() {
        try {
            return highlightColor.call();
        } catch (Exception e) {
            WynntilsMod.error(e.getMessage());
            return CustomColor.NONE;
        }
    }

    public static ItemTier fromString(String name) {
        if (name.charAt(0) == '§') {
            return fromChatFormatting(ChatFormatting.getByCode(name.charAt(1)));
        }

        return null;
    }

    public static ItemTier fromComponent(Component component) {
        String name = component.getString();

        if (name.charAt(0) == '§') {
            return fromChatFormatting(ChatFormatting.getByCode(name.charAt(1)));
        }

        return null;
    }

    public static ItemTier fromChatFormatting(ChatFormatting formatting) {
        return Arrays.stream(ItemTier.values())
                .filter(t -> t.getChatFormatting() == formatting)
                .findFirst()
                .orElse(null);
    }

    public static ItemTier fromBoxDamage(int damage) {
        if (damage > 6) return NORMAL;
        return ItemTier.values()[damage];
    }

    public int getItemIdentificationCost(int level) {
        return this.baseCost + (int) Math.ceil(level * this.costMultiplier);
    }

    public Component asLore() {
        return Component.literal(this + " Item").withStyle(chatFormatting);
    }

    @Override
    public String toString() {
        return StringUtils.capitalizeFirst(name().toLowerCase(Locale.ROOT));
    }
}
