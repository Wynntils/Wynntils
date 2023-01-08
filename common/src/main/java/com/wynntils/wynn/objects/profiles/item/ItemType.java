/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

import com.wynntils.wynn.objects.ClassType;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum ItemType {
    SPEAR(ClassType.Warrior, 16 * 1, 16 * 1, Items.IRON_SHOVEL, 0),
    WAND(ClassType.Mage, 16 * 0, 16 * 1, Items.STICK, 0),
    DAGGER(ClassType.Assassin, 16 * 2, 16 * 1, Items.SHEARS, 0),
    BOW(ClassType.Archer, 16 * 3, 16 * 1, Items.BOW, 0),
    RELIK(ClassType.Shaman, 16 * 0, 16 * 2, Items.STONE_SHOVEL, 7),
    RING(null, 16 * 1, 16 * 2, Items.FLINT_AND_STEEL, 2),
    BRACELET(null, 16 * 2, 16 * 2, Items.FLINT_AND_STEEL, 19),
    NECKLACE(null, 16 * 3, 16 * 2, Items.FLINT_AND_STEEL, 36),
    HELMET(null, 16 * 0, 16 * 0, Items.LEATHER_HELMET, 0),
    CHESTPLATE(null, 16 * 1, 16 * 0, Items.LEATHER_CHESTPLATE, 0),
    LEGGINGS(null, 16 * 2, 16 * 0, Items.LEATHER_LEGGINGS, 0),
    BOOTS(null, 16 * 3, 16 * 0, Items.LEATHER_BOOTS, 0),
    MASTERY_TOME(null, 16 * 0, 16 * 3, Items.ENCHANTED_BOOK, 0),
    CHARM(null, 16 * 1, 16 * 3, Items.CLAY_BALL, 0);

    private final ClassType classReq;
    private final int iconTextureX;
    private final int iconTextureY;
    private final Item defaultItem;
    private final int defaultDamage;

    ItemType(ClassType classReq, int iconTextureX, int iconTextureY, Item defaultItem, int defaultDamage) {
        this.classReq = classReq;
        this.iconTextureX = iconTextureX;
        this.iconTextureY = iconTextureY;
        this.defaultItem = defaultItem;
        this.defaultDamage = defaultDamage;
    }

    public ClassType getClassReq() {
        return classReq;
    }

    public int getIconTextureX() {
        return iconTextureX;
    }

    public int getIconTextureY() {
        return iconTextureY;
    }

    public int getDefaultDamage() {
        return defaultDamage;
    }

    public Item getDefaultItem() {
        return defaultItem;
    }

    public static Optional<ItemType> fromString(String typeStr) {
        try {
            return Optional.of(ItemType.valueOf(typeStr.toUpperCase(Locale.ROOT).replace(" ", "_")));
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
