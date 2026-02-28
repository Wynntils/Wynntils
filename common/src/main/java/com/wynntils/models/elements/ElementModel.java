/*
 * Copyright © Wynntils 2023-2026.
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
                new PowderTierInfo(Powder.WATER, 1, 3, 4, 13, 5, 3, 1),
                new PowderTierInfo(Powder.WATER, 2, 5, 6, 15, 10, 6, 1),
                new PowderTierInfo(Powder.WATER, 3, 6, 8, 17, 20, 11, 3),
                new PowderTierInfo(Powder.WATER, 4, 7, 8, 21, 30, 16, 4),
                new PowderTierInfo(Powder.WATER, 5, 8, 10, 26, 45, 23, 6),
                new PowderTierInfo(Powder.WATER, 6, 10, 13, 32, 60, 32, 10),
                new PowderTierInfo(Powder.WATER, 7, 11, 15, 38, 75, 40, 15),
                new PowderTierInfo(Powder.FIRE, 1, 2, 5, 14, 5, 3, 1),
                new PowderTierInfo(Powder.FIRE, 2, 4, 7, 16, 10, 6, 1),
                new PowderTierInfo(Powder.FIRE, 3, 5, 9, 19, 20, 10, 2),
                new PowderTierInfo(Powder.FIRE, 4, 6, 9, 24, 30, 15, 3),
                new PowderTierInfo(Powder.FIRE, 5, 7, 11, 30, 45, 22, 5),
                new PowderTierInfo(Powder.FIRE, 6, 9, 14, 37, 60, 31, 9),
                new PowderTierInfo(Powder.FIRE, 7, 10, 16, 44, 75, 39, 14),
                new PowderTierInfo(Powder.AIR, 1, 2, 6, 11, 5, 3, 1),
                new PowderTierInfo(Powder.AIR, 2, 3, 9, 14, 10, 6, 2),
                new PowderTierInfo(Powder.AIR, 3, 4, 11, 17, 20, 10, 3),
                new PowderTierInfo(Powder.AIR, 4, 5, 11, 22, 30, 16, 5),
                new PowderTierInfo(Powder.AIR, 5, 7, 12, 28, 45, 23, 7),
                new PowderTierInfo(Powder.AIR, 6, 8, 15, 35, 60, 30, 8),
                new PowderTierInfo(Powder.AIR, 7, 9, 17, 42, 75, 38, 13),
                new PowderTierInfo(Powder.EARTH, 1, 4, 5, 17, 5, 2, 1),
                new PowderTierInfo(Powder.EARTH, 2, 6, 7, 21, 10, 5, 2),
                new PowderTierInfo(Powder.EARTH, 3, 7, 9, 25, 20, 9, 3),
                new PowderTierInfo(Powder.EARTH, 4, 8, 9, 31, 30, 14, 4),
                new PowderTierInfo(Powder.EARTH, 5, 9, 11, 38, 45, 22, 7),
                new PowderTierInfo(Powder.EARTH, 6, 11, 12, 46, 60, 29, 7),
                new PowderTierInfo(Powder.EARTH, 7, 12, 14, 52, 75, 37, 12),
                new PowderTierInfo(Powder.THUNDER, 1, 1, 8, 9, 5, 2, 1),
                new PowderTierInfo(Powder.THUNDER, 2, 1, 12, 11, 10, 4, 1),
                new PowderTierInfo(Powder.THUNDER, 3, 2, 14, 13, 20, 8, 2),
                new PowderTierInfo(Powder.THUNDER, 4, 2, 15, 17, 30, 13, 3),
                new PowderTierInfo(Powder.THUNDER, 5, 3, 17, 22, 45, 20, 5),
                new PowderTierInfo(Powder.THUNDER, 6, 4, 19, 28, 60, 28, 6),
                new PowderTierInfo(Powder.THUNDER, 7, 5, 21, 32, 75, 36, 11));
    }
}
