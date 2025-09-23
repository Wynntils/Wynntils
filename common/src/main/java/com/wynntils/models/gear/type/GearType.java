/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.character.type.ClassType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

public enum GearType {
    SPEAR(ClassType.WARRIOR, 1565, 1642, 0),
    WAND(ClassType.MAGE, 1403, 1484, 1),
    DAGGER(ClassType.ASSASSIN, 1323, 1402, 2),
    BOW(ClassType.ARCHER, 1245, 1322, 3),
    RELIK(ClassType.SHAMAN, 1485, 1564, 4),
    // This is a fallback for signed, crafted gear with a skin
    WEAPON(null, 0, 0, 12),
    // Note: This fallback should basically be never be matched, but we use it in item encoding
    //       (as it's the same as WEAPON, and we have no other info)
    ACCESSORY(null, 0, 0, 13),
    RING(null, 1197, 1213, 5),
    BRACELET(null, 1214, 1227, 6),
    NECKLACE(null, 1228, 1244, 7),
    HELMET(
            null,
            Items.LEATHER_HELMET,
            1,
            18,
            1643,
            1813,
            List.of(
                    Items.LEATHER_HELMET,
                    Items.CHAINMAIL_HELMET,
                    Items.IRON_HELMET,
                    Items.GOLDEN_HELMET,
                    Items.DIAMOND_HELMET,
                    Items.NETHERITE_HELMET,
                    Items.POTION),
            8),
    CHESTPLATE(
            null,
            Items.LEATHER_CHESTPLATE,
            1,
            18,
            List.of(
                    Items.CHAINMAIL_CHESTPLATE,
                    Items.IRON_CHESTPLATE,
                    Items.GOLDEN_CHESTPLATE,
                    Items.DIAMOND_CHESTPLATE,
                    Items.NETHERITE_CHESTPLATE),
            9),
    LEGGINGS(
            null,
            Items.LEATHER_LEGGINGS,
            1,
            18,
            List.of(
                    Items.CHAINMAIL_LEGGINGS,
                    Items.IRON_LEGGINGS,
                    Items.GOLDEN_LEGGINGS,
                    Items.DIAMOND_LEGGINGS,
                    Items.NETHERITE_LEGGINGS),
            10),
    BOOTS(
            null,
            Items.LEATHER_BOOTS,
            1,
            18,
            List.of(
                    Items.CHAINMAIL_BOOTS,
                    Items.IRON_BOOTS,
                    Items.GOLDEN_BOOTS,
                    Items.DIAMOND_BOOTS,
                    Items.NETHERITE_BOOTS),
            11),
    MASTERY_TOME(null, 83, 89, -1),
    CHARM(null, 1080, 1083, -1);

    private final ClassType classReq;
    private final Item defaultItem;
    private final List<Float> models;
    private final List<Item> otherItems;
    private final int encodingId;

    GearType(
            ClassType classReq,
            Item defaultItem,
            int firstModel,
            int lastModel,
            int firstSkinModel,
            int lastSkinModel,
            List<Item> otherItems,
            int encodingId) {
        this.classReq = classReq;
        this.defaultItem = defaultItem;
        List<Float> modelList = new ArrayList<>();

        if (!(firstModel == 0 && lastModel == 0)) {
            IntStream.rangeClosed(firstModel, lastModel)
                    .mapToObj(i -> (float) i)
                    .forEach(modelList::add);
        }

        if (!(firstSkinModel == 0 && lastSkinModel == 0)) {
            IntStream.rangeClosed(firstSkinModel, lastSkinModel)
                    .mapToObj(i -> (float) i)
                    .forEach(modelList::add);
        }

        this.models = List.copyOf(modelList);
        this.otherItems = otherItems;
        this.encodingId = encodingId;
    }

    GearType(
            ClassType classReq,
            Item defaultItem,
            int firstModel,
            int lastModel,
            List<Item> otherItems,
            int encodingId) {
        this(classReq, defaultItem, firstModel, lastModel, 0, 0, otherItems, encodingId);
    }

    GearType(ClassType classReq, int firstModel, int lastModel, int encodingId) {
        this(classReq, Items.POTION, firstModel, lastModel, List.of(), encodingId);
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
        CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);

        if (customModelData != null) {
            for (GearType gearType : values()) {
                // We only want to match for proper gear, not rewards
                if (gearType.isReward()) continue;

                List<Float> customModelDataValue = customModelData.floats();
                for (Float modelValue : customModelDataValue) {
                    if ((gearType.defaultItem.equals(item) || gearType.otherItems.contains(item))
                            && gearType.models.contains(modelValue)) {
                        return gearType;
                    }
                }
            }
        }

        return null;
    }

    public static GearType fromClassType(ClassType classType) {
        for (GearType gearType : values()) {
            if (gearType.classReq == classType) return gearType;
        }
        return null;
    }

    public static GearType fromEncodingId(int id) {
        for (GearType gearType : values()) {
            if (gearType.encodingId == id) return gearType;
        }
        return null;
    }

    public ClassType getClassReq() {
        return classReq;
    }

    public Item getDefaultItem() {
        return defaultItem;
    }

    public float getDefaultModel() {
        return models.getFirst();
    }

    public int getEncodingId() {
        return encodingId;
    }

    public boolean isReward() {
        return this == MASTERY_TOME || this == CHARM;
    }

    public boolean isWeapon() {
        return (classReq != null) || this == WEAPON;
    }

    public boolean isAccessory() {
        // Flint and steel is used for crafted items, normal items are horse armor
        return defaultItem == Items.FLINT_AND_STEEL || defaultItem == Items.IRON_HORSE_ARMOR;
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
