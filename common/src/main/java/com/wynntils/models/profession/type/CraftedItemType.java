/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import com.wynntils.handlers.tooltip.type.ItemEmblemSprite;
import com.wynntils.models.character.type.ClassType;
import java.util.Locale;

public enum CraftedItemType {
    SPEAR(ItemEmblemSprite.SPEAR),
    WAND(ItemEmblemSprite.WAND),
    DAGGER(ItemEmblemSprite.DAGGER),
    BOW(ItemEmblemSprite.BOW),
    RELIK(ItemEmblemSprite.RELIK),
    HELMET(ItemEmblemSprite.HELMET),
    CHESTPLATE(ItemEmblemSprite.CHESTPLATE),
    LEGGINGS(ItemEmblemSprite.LEGGINGS),
    BOOTS(ItemEmblemSprite.BOOTS),
    RING(ItemEmblemSprite.RING),
    BRACELET(ItemEmblemSprite.BRACELET),
    NECKLACE(ItemEmblemSprite.NECKLACE),
    POTION(ItemEmblemSprite.POTION),
    FOOD(ItemEmblemSprite.FOOD),
    SCROLL(ItemEmblemSprite.SCROLL);

    private final ItemEmblemSprite emblemSprite;

    CraftedItemType(ItemEmblemSprite emblemSprite) {
        this.emblemSprite = emblemSprite;
    }

    public static CraftedItemType fromString(String typeStr) {
        try {
            return CraftedItemType.valueOf(typeStr.toUpperCase(Locale.ROOT).replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public ItemEmblemSprite getEmblemSprite() {
        return emblemSprite;
    }

    public boolean isGear() {
        return this != POTION && this != FOOD && this != SCROLL;
    }

    public boolean isArmor() {
        return this == HELMET || this == CHESTPLATE || this == LEGGINGS || this == BOOTS;
    }

    public boolean isAccessory() {
        return this == RING || this == BRACELET || this == NECKLACE;
    }

    public boolean isWeapon() {
        return this == SPEAR || this == WAND || this == DAGGER || this == BOW || this == RELIK;
    }

    public ClassType getItemClassType() {
        return switch (this) {
            case SPEAR -> ClassType.WARRIOR;
            case WAND -> ClassType.MAGE;
            case DAGGER -> ClassType.ASSASSIN;
            case BOW -> ClassType.ARCHER;
            case RELIK -> ClassType.SHAMAN;
            default -> ClassType.NONE;
        };
    }
}
