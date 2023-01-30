/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.character.type.ClassType;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum GearType {
    SPEAR(ClassType.Warrior, Items.IRON_SHOVEL, 0),
    WAND(ClassType.Mage, Items.STICK, 0),
    DAGGER(ClassType.Assassin, Items.SHEARS, 0),
    BOW(ClassType.Archer, Items.BOW, 0),
    RELIK(ClassType.Shaman, Items.STONE_SHOVEL, 7),
    RING(null, Items.FLINT_AND_STEEL, 2),
    BRACELET(null, Items.FLINT_AND_STEEL, 19),
    NECKLACE(null, Items.FLINT_AND_STEEL, 36),
    HELMET(null, Items.LEATHER_HELMET, 0),
    CHESTPLATE(null, Items.LEATHER_CHESTPLATE, 0),
    LEGGINGS(null, Items.LEATHER_LEGGINGS, 0),
    BOOTS(null, Items.LEATHER_BOOTS, 0),
    MASTERY_TOME(null, Items.ENCHANTED_BOOK, 0),
    CHARM(null, Items.CLAY_BALL, 0);

    private final ClassType classReq;
    private final Item defaultItem;
    private final int defaultDamage;

    GearType(ClassType classReq, Item defaultItem, int defaultDamage) {
        this.classReq = classReq;
        this.defaultItem = defaultItem;
        this.defaultDamage = defaultDamage;
    }

    public ClassType getClassReq() {
        return classReq;
    }

    public int getDefaultDamage() {
        return defaultDamage;
    }

    public Item getDefaultItem() {
        return defaultItem;
    }

    public static Optional<GearType> fromString(String typeStr) {
        try {
            return Optional.of(GearType.valueOf(typeStr.toUpperCase(Locale.ROOT).replace(" ", "_")));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public boolean isWeapon() {
        switch (this) {
            case SPEAR, WAND, DAGGER, BOW, RELIK -> {
                return true;
            }
        }

        return false;
    }
}
