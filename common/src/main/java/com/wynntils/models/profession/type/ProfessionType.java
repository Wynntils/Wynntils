/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import com.wynntils.core.persisted.config.NullableConfig;
import com.wynntils.core.text.fonts.CommonFonts;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public enum ProfessionType implements NullableConfig {
    WOODCUTTING("Woodcutting", "\uE003"),
    MINING("Mining", "\uE002"),
    FISHING("Fishing", "\uE001"),
    FARMING("Farming", "\uE000"),

    // crafting
    ALCHEMISM("Alchemism", "\uE004"),
    ARMOURING("Armouring", "\uE005"),
    COOKING("Cooking", "\uE006"),
    JEWELING("Jeweling", "\uE007"),
    SCRIBING("Scribing", "\uE008"),
    TAILORING("Tailoring", "\uE009"),
    WEAPONSMITHING("Weaponsmithing", "\uE00A"),
    WOODWORKING("Woodworking", "\uE00B");

    private final String professionName;
    private final String professionIconChar;

    ProfessionType(String professionName, String professionIconChar) {
        this.professionName = professionName;
        this.professionIconChar = professionIconChar;
    }

    public String getDisplayName() {
        return professionName;
    }

    public Component getProfessionIcon() {
        return Component.literal(professionIconChar).withStyle(Style.EMPTY.withFont(CommonFonts.PROFESSION_FONT));
    }

    public static ProfessionType fromString(String type) {
        try {
            return valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static List<ProfessionType> craftingProfessionTypes() {
        return List.of(ALCHEMISM, ARMOURING, COOKING, JEWELING, SCRIBING, TAILORING, WEAPONSMITHING, WOODWORKING);
    }
}
