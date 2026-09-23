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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;

public enum GearTier {
    NORMAL(TextColor.WHITE, 0, 0.0f, CustomColor.fromInt(0xe0e0e0)),
    UNIQUE(TextColor.YELLOW, 3, 0.5f, CustomColor.fromInt(0xfff2b3)),
    RARE(TextColor.LIGHT_PURPLE, 8, 1.2f, CustomColor.fromInt(0xf2c2f2)),
    LEGENDARY(TextColor.AQUA, 12, 4.5f, CustomColor.fromInt(0xcff9f9)),
    FABLED(TextColor.RED, 16, 8.0f, CustomColor.fromInt(0xf2c2c2)),
    MYTHIC(TextColor.DARK_PURPLE, 90, 18.0f, CustomColor.fromInt(0xe0b3e6)),
    CRAFTED(TextColor.DARK_AQUA, 0, 0.0f, CustomColor.NONE);

    private final TextColor textColor;
    private final int baseCost;
    private final float costMultiplier;
    private final CustomColor secondaryColor;
    private final String apiName;

    GearTier(TextColor textColor, int baseCost, float costMultiplier, CustomColor secondaryColor) {
        this.textColor = textColor;
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
        Optional<TextColor> textColor = (TextColor.NAMED_COLORS.values().stream()
                .filter(c -> c.getValue()
                        == text.getFirstPart()
                                .getPartStyle()
                                .getStyle()
                                .getColor()
                                .getValue())
                .findFirst());

        if (textColor.isPresent()) {
            return fromTextColor(textColor.get());
        }

        return null;
    }

    public static GearTier fromComponent(Component component) {
        return fromStyledText(StyledText.fromComponent(component));
    }

    public static GearTier fromTextColor(TextColor textColor) {
        return Arrays.stream(GearTier.values())
                .filter(t -> t.getTextColor() == textColor)
                .findFirst()
                .orElse(null);
    }

    public TextColor getTextColor() {
        return textColor;
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

    public Identifier getTooltipStyle(boolean shiny) {
        return Identifier.withDefaultNamespace(apiName + (shiny ? "_shiny" : ""));
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
