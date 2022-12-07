/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

public enum RequirementType {
    QUEST("Quest Req: "),
    CLASSTYPE("Class Req: "),
    LEVEL("Combat Lv. Min: "),
    STRENGTH("Strength Min: "),
    AGILITY("Agility Min: "),
    DEFENSE("Defence Min: "),
    INTELLIGENCE("Intelligence Min: "),
    DEXTERITY("Dexterity Min: ");

    private final String lore;

    RequirementType(String lore) {
        this.lore = lore;
    }

    public String asLore() {
        return lore;
    }
}
