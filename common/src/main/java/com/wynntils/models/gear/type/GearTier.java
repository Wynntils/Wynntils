/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.StringUtils;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum GearTier {
    NORMAL(ChatFormatting.WHITE, 0, 0.0f, "common"),
    UNIQUE(ChatFormatting.YELLOW, 3, 0.5f),
    RARE(ChatFormatting.LIGHT_PURPLE, 8, 1.2f),
    SET(ChatFormatting.GREEN, 8, 1.2f),
    LEGENDARY(ChatFormatting.AQUA, 12, 4.5f),
    FABLED(ChatFormatting.RED, 16, 8.0f),
    MYTHIC(ChatFormatting.DARK_PURPLE, 90, 18.0f),
    CRAFTED(ChatFormatting.DARK_AQUA, 0, 0.0f);

    private final ChatFormatting chatFormatting;
    private final int baseCost;
    private final float costMultiplier;
    private final String apiName;

    GearTier(ChatFormatting chatFormatting, int baseCost, float costMultiplier) {
        this.chatFormatting = chatFormatting;
        this.baseCost = baseCost;
        this.costMultiplier = costMultiplier;
        this.apiName = name().toLowerCase(Locale.ROOT);
    }

    GearTier(ChatFormatting chatFormatting, int baseCost, float costMultiplier, String apiName) {
        this.chatFormatting = chatFormatting;
        this.baseCost = baseCost;
        this.costMultiplier = costMultiplier;
        this.apiName = apiName;
    }

    public static GearTier fromString(String typeStr) {
        for (GearTier type : GearTier.values()) {
            if (type.apiName.equals(typeStr.toLowerCase(Locale.ROOT))) {
                return type;
            }
        }

        return null;
    }

    public static GearTier fromStyledText(StyledText text) {
        Optional<ChatFormatting> chatFormatting = Arrays.stream(ChatFormatting.values())
                .filter(ChatFormatting::isColor)
                .filter(c -> c.getColor()
                        == text.getFirstPart()
                                .getPartStyle()
                                .getStyle()
                                .getColor()
                                .getValue())
                .findFirst();

        if (chatFormatting.isPresent()) {
            return fromChatFormatting(chatFormatting.get());
        }

        return null;
    }

    public static GearTier fromComponent(Component component) {
        return fromStyledText(StyledText.fromComponent(component));
    }

    public static GearTier fromChatFormatting(ChatFormatting formatting) {
        return Arrays.stream(GearTier.values())
                .filter(t -> t.getChatFormatting() == formatting)
                .findFirst()
                .orElse(null);
    }

    public static GearTier fromBoxDamage(int damage) {
        if (damage > 6) return NORMAL;
        return GearTier.values()[damage];
    }

    public ChatFormatting getChatFormatting() {
        return chatFormatting;
    }

    public int getGearIdentificationCost(int level) {
        return this.baseCost + (int) Math.ceil(level * this.costMultiplier);
    }

    public String getName() {
        return StringUtils.capitalizeFirst(name().toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return getName();
    }
}
