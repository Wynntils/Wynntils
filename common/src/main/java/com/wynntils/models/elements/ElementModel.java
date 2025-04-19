/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements;

import com.wynntils.core.components.Model;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderTierInfo;
import com.wynntils.models.elements.type.Skill;
import java.util.List;

public final class ElementModel extends Model {
    private final List<PowderTierInfo> allPowderTierInfo;

    public ElementModel() {
        super(List.of());

        allPowderTierInfo = buildPowderTierInfo();
    }

    public List<PowderTierInfo> getAllPowderTierInfo() {
        return allPowderTierInfo;
    }

    public PowderTierInfo getPowderTierInfo(Powder element, int tier) {
        return allPowderTierInfo.stream()
                .filter(p -> p.element() == element && p.tier() == tier)
                .findFirst()
                .orElse(null);
    }

    public List<Skill> getGearSkillOrder() {
        return List.of(Skill.STRENGTH, Skill.DEXTERITY, Skill.INTELLIGENCE, Skill.AGILITY, Skill.DEFENCE);
    }

    public List<Element> getGearElementOrder() {
        return List.of(Element.FIRE, Element.WATER, Element.AIR, Element.THUNDER, Element.EARTH);
    }

    public static Element getOpposingElement(Element element) {
        return switch (element) {
            case EARTH -> Element.AIR;
            case THUNDER -> Element.EARTH;
            case WATER -> Element.THUNDER;
            case FIRE -> Element.WATER;
            case AIR -> Element.FIRE;
        };
    }

    public Powder getOpposingElement(Powder powder) {
        return Powder.fromElement(getOpposingElement(powder.getElement()));
    }

    private List<PowderTierInfo> buildPowderTierInfo() {
        return List.of(
                new PowderTierInfo(Powder.WATER, 1, 3, 4, 13, 3, 1),
                new PowderTierInfo(Powder.WATER, 2, 4, 6, 15, 6, 1),
                new PowderTierInfo(Powder.WATER, 3, 5, 8, 17, 11, 2),
                new PowderTierInfo(Powder.WATER, 4, 6, 8, 21, 18, 4),
                new PowderTierInfo(Powder.WATER, 5, 7, 10, 26, 28, 7),
                new PowderTierInfo(Powder.WATER, 6, 9, 11, 32, 40, 10),
                new PowderTierInfo(Powder.FIRE, 1, 2, 5, 14, 3, 1),
                new PowderTierInfo(Powder.FIRE, 2, 4, 8, 16, 5, 2),
                new PowderTierInfo(Powder.FIRE, 3, 5, 9, 19, 9, 3),
                new PowderTierInfo(Powder.FIRE, 4, 6, 9, 24, 16, 5),
                new PowderTierInfo(Powder.FIRE, 5, 8, 10, 30, 25, 9),
                new PowderTierInfo(Powder.FIRE, 6, 10, 12, 37, 36, 13),
                new PowderTierInfo(Powder.AIR, 1, 2, 6, 11, 3, 1),
                new PowderTierInfo(Powder.AIR, 2, 3, 10, 14, 6, 2),
                new PowderTierInfo(Powder.AIR, 3, 4, 11, 17, 10, 3),
                new PowderTierInfo(Powder.AIR, 4, 5, 11, 22, 16, 5),
                new PowderTierInfo(Powder.AIR, 5, 7, 12, 28, 24, 9),
                new PowderTierInfo(Powder.AIR, 6, 8, 14, 35, 34, 13),
                new PowderTierInfo(Powder.EARTH, 1, 3, 6, 17, 2, 1),
                new PowderTierInfo(Powder.EARTH, 2, 5, 8, 21, 4, 2),
                new PowderTierInfo(Powder.EARTH, 3, 6, 10, 25, 8, 3),
                new PowderTierInfo(Powder.EARTH, 4, 7, 10, 31, 14, 5),
                new PowderTierInfo(Powder.EARTH, 5, 9, 11, 38, 22, 9),
                new PowderTierInfo(Powder.EARTH, 6, 11, 13, 46, 30, 13),
                new PowderTierInfo(Powder.THUNDER, 1, 1, 8, 9, 3, 1),
                new PowderTierInfo(Powder.THUNDER, 2, 1, 12, 11, 5, 1),
                new PowderTierInfo(Powder.THUNDER, 3, 2, 15, 13, 9, 2),
                new PowderTierInfo(Powder.THUNDER, 4, 3, 15, 17, 14, 4),
                new PowderTierInfo(Powder.THUNDER, 5, 4, 17, 22, 20, 7),
                new PowderTierInfo(Powder.THUNDER, 6, 5, 20, 28, 28, 10));
    }
}
