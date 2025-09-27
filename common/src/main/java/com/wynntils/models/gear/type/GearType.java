/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.services.custommodel.ModelSupplier;
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
    SPEAR(ClassType.WARRIOR, ModelSupplier.forKey("spear_min"), ModelSupplier.forKey("spear_max"), 0),
    WAND(ClassType.MAGE, ModelSupplier.forKey("wand_min"), ModelSupplier.forKey("wand_max"), 1),
    DAGGER(ClassType.ASSASSIN, ModelSupplier.forKey("dagger_min"), ModelSupplier.forKey("dagger_max"), 2),
    BOW(ClassType.ARCHER, ModelSupplier.forKey("bow_min"), ModelSupplier.forKey("bow_max"), 3),
    RELIK(ClassType.SHAMAN, ModelSupplier.forKey("relik_min"), ModelSupplier.forKey("relik_max"), 4),
    // This is a fallback for signed, crafted gear with a skin
    WEAPON(null, null, null, 12),
    // Note: This fallback should basically be never be matched, but we use it in item encoding
    //       (as it's the same as WEAPON, and we have no other info)
    ACCESSORY(null, null, null, 13),
    RING(null, ModelSupplier.forKey("ring_min"), ModelSupplier.forKey("ring_max"), 5),
    BRACELET(null, ModelSupplier.forKey("bracelet_min"), ModelSupplier.forKey("bracelet_max"), 6),
    NECKLACE(null, ModelSupplier.forKey("necklace_min"), ModelSupplier.forKey("necklace_max"), 7),
    HELMET(
            null,
            Items.LEATHER_HELMET,
            ModelSupplier.forKey("helmet_min"),
            ModelSupplier.forKey("helmet_max"),
            ModelSupplier.forKey("helmet_skin_min"),
            ModelSupplier.forKey("helmet_skin_min"),
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
            ModelSupplier.forKey("chestplate_min"),
            ModelSupplier.forKey("chestplate_max"),
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
            ModelSupplier.forKey("leggings_min"),
            ModelSupplier.forKey("leggings_max"),
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
            ModelSupplier.forKey("boots_min"),
            ModelSupplier.forKey("boots_max"),
            List.of(
                    Items.CHAINMAIL_BOOTS,
                    Items.IRON_BOOTS,
                    Items.GOLDEN_BOOTS,
                    Items.DIAMOND_BOOTS,
                    Items.NETHERITE_BOOTS),
            11),
    MASTERY_TOME(null, ModelSupplier.forKey("tome_min"), ModelSupplier.forKey("tome_max"), -1),
    CHARM(null, ModelSupplier.forKey("charm_min"), ModelSupplier.forKey("charm_max"), -1);

    private final ClassType classReq;
    private final Item defaultItem;
    private final ModelSupplier minModelSupplier;
    private final ModelSupplier maxModelSupplier;
    private final ModelSupplier minSkinModelSupplier;
    private final ModelSupplier maxSkinModelSupplier;
    private final List<Item> otherItems;
    private final int encodingId;

    private List<Float> modelList = new ArrayList<>();

    GearType(
            ClassType classReq,
            Item defaultItem,
            ModelSupplier minModelSupplier,
            ModelSupplier maxModelSupplier,
            ModelSupplier minSkinModelSupplier,
            ModelSupplier maxSkinModelSupplier,
            List<Item> otherItems,
            int encodingId) {
        this.classReq = classReq;
        this.defaultItem = defaultItem;
        this.minModelSupplier = minModelSupplier;
        this.maxModelSupplier = maxModelSupplier;
        this.minSkinModelSupplier = minSkinModelSupplier;
        this.maxSkinModelSupplier = maxSkinModelSupplier;
        this.otherItems = otherItems;
        this.encodingId = encodingId;
    }

    GearType(
            ClassType classReq,
            Item defaultItem,
            ModelSupplier minModelSupplier,
            ModelSupplier maxModelSupplier,
            List<Item> otherItems,
            int encodingId) {
        this(classReq, defaultItem, minModelSupplier, maxModelSupplier, null, null, otherItems, encodingId);
    }

    GearType(ClassType classReq, ModelSupplier minModelSupplier, ModelSupplier maxModelSupplier, int encodingId) {
        this(classReq, Items.POTION, minModelSupplier, maxModelSupplier, List.of(), encodingId);
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
                            && gearType.getModelList().contains(modelValue)) {
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
        return minModelSupplier.get().orElse(-1f);
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

    private List<Float> getModelList() {
        if (modelList == null) {
            List<Float> tmp = new ArrayList<>();

            if (minModelSupplier != null
                    && maxModelSupplier != null
                    && minModelSupplier.get().isPresent()
                    && maxModelSupplier.get().isPresent()) {
                int min = minModelSupplier.get().get().intValue();
                int max = maxModelSupplier.get().get().intValue();

                IntStream.rangeClosed(min, max).mapToObj(i -> (float) i).forEach(tmp::add);
            }

            if (minSkinModelSupplier != null
                    && maxSkinModelSupplier != null
                    && minSkinModelSupplier.get().isPresent()
                    && maxSkinModelSupplier.get().isPresent()) {
                int min = minSkinModelSupplier.get().get().intValue();
                int max = maxSkinModelSupplier.get().get().intValue();

                IntStream.rangeClosed(min, max).mapToObj(i -> (float) i).forEach(tmp::add);
            }

            modelList = List.copyOf(tmp);
        }
        return modelList;
    }
}
