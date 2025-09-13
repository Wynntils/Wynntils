/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements.type;

import com.mojang.serialization.Codec;
import com.wynntils.utils.StringUtils;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

public enum Skill implements StringRepresentable {
    STRENGTH(Element.EARTH),
    DEXTERITY(Element.THUNDER),
    INTELLIGENCE(Element.WATER),
    DEFENCE(Element.FIRE), // Note! Must be spelled with "C" to match in-game
    AGILITY(Element.AIR);

    public static final Codec<Skill> CODEC = StringRepresentable.fromEnum(Skill::values);

    private final Element associatedElement;
    private final String apiName;
    private final String displayName;

    Skill(Element associatedElement) {
        this.associatedElement = associatedElement;
        this.apiName = this.name().toLowerCase(Locale.ROOT);
        this.displayName = StringUtils.capitalized(this.name());
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
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

    public static Skill fromElement(Element element) {
        for (Skill skill : values()) {
            if (skill.getAssociatedElement() == element) {
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
