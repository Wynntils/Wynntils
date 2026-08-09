/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CustomColor;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum GearTier {
    NORMAL(ChatFormatting.WHITE, 0, 0.0f, CustomColor.fromInt(0xe0e0e0)),
    UNIQUE(ChatFormatting.YELLOW, 3, 0.5f, CustomColor.fromInt(0xfff2b3)),
    RARE(ChatFormatting.LIGHT_PURPLE, 8, 1.2f, CustomColor.fromInt(0xf2c2f2)),
    @Deprecated
    SET(ChatFormatting.GRAY, 8, 1.2f, CustomColor.NONE),
    LEGENDARY(ChatFormatting.AQUA, 12, 4.5f, CustomColor.fromInt(0xcff9f9)),
    FABLED(ChatFormatting.RED, 16, 8.0f, CustomColor.fromInt(0xf2c2c2)),
    MYTHIC(ChatFormatting.DARK_PURPLE, 90, 18.0f, CustomColor.fromInt(0xe0b3e6)),
    CRAFTED(ChatFormatting.DARK_AQUA, 0, 0.0f, CustomColor.NONE);

    private final ChatFormatting chatFormatting;
    private final int baseCost;
    private final float costMultiplier;
    private final CustomColor secondaryColor;
    private final String apiName;

    GearTier(ChatFormatting chatFormatting, int baseCost, float costMultiplier, CustomColor secondaryColor) {
        this.chatFormatting = chatFormatting;
        this.baseCost = baseCost;
        this.costMultiplier = costMultiplier;
        this.secondaryColor = secondaryColor;
        this.apiName = name().toLowerCase(Locale.ROOT);
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

    public ChatFormatting getChatFormatting() {
        return chatFormatting;
    }

    public CustomColor getSecondaryColor() {
        return secondaryColor;
    }

    public int getGearIdentificationCost(int level) {
        return this.baseCost + (int) Math.ceil(level * this.costMultiplier);
    }

    public String getName() {
        return StringUtils.capitalizeFirst(name().toLowerCase(Locale.ROOT));
    }

    public String getApiName() {
        return apiName;
    }

    // This should be used instead of values() in almost all places as to not include the SET tier
    public static GearTier[] validValues() {
        return new GearTier[] {NORMAL, UNIQUE, RARE, LEGENDARY, FABLED, MYTHIC, CRAFTED};
    }

    @Override
    public String toString() {
        return getName();
    }
}
