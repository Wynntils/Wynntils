/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements.type;

import com.wynntils.utils.StringUtils;
import java.util.Locale;
import net.minecraft.ChatFormatting;

public enum Skill {
    STRENGTH(Element.EARTH),
    DEXTERITY(Element.THUNDER),
    INTELLIGENCE(Element.WATER),
    DEFENCE(Element.FIRE), // Note! Must be spelled with "C" to match in-game
    AGILITY(Element.AIR);

    private final Element associatedElement;
    private final String apiName;
    private final String displayName;

    Skill(Element associatedElement) {
        this.associatedElement = associatedElement;
        this.apiName = this.name().toLowerCase(Locale.ROOT);
        this.displayName = StringUtils.capitalized(this.name());
    }

    public static Skill fromString(String str) {
        try {
            return Skill.valueOf(str.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Skill fromApiId(String apiId) {
        String str = apiId.toLowerCase(Locale.ROOT);
        for (Skill skill : values()) {
            if (skill.getApiName().equals(str)) {
                return skill;
            }
        }
        return null;
    }

    public static boolean isSkill(String idName) {
        for (Skill skill : values()) {
            if (idName.equals(skill.getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    public Element getAssociatedElement() {
        return associatedElement;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApiName() {
        return apiName;
    }

    public String getSymbol() {
        return associatedElement.getSymbol();
    }

    public ChatFormatting getColorCode() {
        return associatedElement.getColorCode();
    }
}
