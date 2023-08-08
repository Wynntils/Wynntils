/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.character.type.ClassType;
import java.util.List;
import java.util.Locale;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum GearType {
    SPEAR(ClassType.WARRIOR, Items.IRON_SHOVEL, 0),
    WAND(ClassType.MAGE, Items.STICK, 0, List.of(Items.WOODEN_SHOVEL)),
    DAGGER(ClassType.ASSASSIN, Items.SHEARS, 0),
    BOW(ClassType.ARCHER, Items.BOW, 0),
    RELIK(ClassType.SHAMAN, Items.STONE_SHOVEL, 7),
    // This is a fallback for signed, crafted gear with a skin
    WEAPON(null, Items.DIAMOND_SHOVEL, 0),
    // FIXME: We need a complete mapping of damage values for ring, bracelet and necklace to be able
    // to get rid of this (needed for crafted and unknown gear)
    ACCESSORY(null, Items.FLINT_AND_STEEL, 0),
    RING(null, Items.FLINT_AND_STEEL, 2),
    BRACELET(null, Items.FLINT_AND_STEEL, 19),
    NECKLACE(null, Items.FLINT_AND_STEEL, 36),
    HELMET(
            null,
            Items.LEATHER_HELMET,
            0,
            List.of(Items.CHAINMAIL_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.DIAMOND_HELMET)),
    CHESTPLATE(
            null,
            Items.LEATHER_CHESTPLATE,
            0,
            List.of(
                    Items.CHAINMAIL_CHESTPLATE,
                    Items.IRON_CHESTPLATE,
                    Items.GOLDEN_CHESTPLATE,
                    Items.DIAMOND_CHESTPLATE)),
    LEGGINGS(
            null,
            Items.LEATHER_LEGGINGS,
            0,
            List.of(Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS)),
    BOOTS(
            null,
            Items.LEATHER_BOOTS,
            0,
            List.of(Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS)),
    MASTERY_TOME(null, Items.ENCHANTED_BOOK, 0),
    CHARM(null, Items.CLAY_BALL, 0);

    private final ClassType classReq;
    private final Item defaultItem;
    private final int defaultDamage;
    private final List<Item> otherItems;

    GearType(ClassType classReq, Item defaultItem, int defaultDamage, List<Item> otherItems) {
        this.classReq = classReq;
        this.defaultItem = defaultItem;
        this.defaultDamage = defaultDamage;
        this.otherItems = otherItems;
    }

    GearType(ClassType classReq, Item defaultItem, int defaultDamage) {
        this(classReq, defaultItem, defaultDamage, List.of());
    }

    public static GearType fromString(String typeStr) {
        try {
            return GearType.valueOf(typeStr.toUpperCase(Locale.ROOT).replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static GearType fromItemStack(ItemStack itemStack) {
        Item item = itemStack.getItem();
        for (GearType gearType : values()) {
            // We only want to match for proper gear, not rewards
            if (gearType.isReward()) continue;

            if (gearType.defaultItem.equals(item)) return gearType;
            if (gearType.otherItems.contains(item)) return gearType;
        }
        return null;
    }

    public ClassType getClassReq() {
        return classReq;
    }

    public Item getDefaultItem() {
        return defaultItem;
    }

    public int getDefaultDamage() {
        return defaultDamage;
    }

    public boolean isReward() {
        return this == MASTERY_TOME || this == CHARM;
    }

    public boolean isWeapon() {
        return (classReq != null) || this == WEAPON;
    }

    public boolean isAccessory() {
        return defaultItem == Items.FLINT_AND_STEEL;
    }

    public boolean isArmor() {
        return switch (this) {
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> true;
            default -> false;
        };
    }

    public boolean isValidWeapon(ClassType classType) {
        if (!isWeapon()) return false;
        // We can't really know what kind of weapon this is, so assume it is valid
        if (this == WEAPON) return true;

        return classReq == classType;
    }
}
