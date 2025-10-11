/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.core.components.Services;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

public enum GearType {
    SPEAR(ClassType.WARRIOR, "spear", Items.IRON_SHOVEL, 0),
    WAND(ClassType.MAGE, "wand", Items.WOODEN_SHOVEL, 1),
    DAGGER(ClassType.ASSASSIN, "dagger", Items.SHEARS, 2),
    BOW(ClassType.ARCHER, "bow", Items.BOW, 3),
    RELIK(ClassType.SHAMAN, "relik", Items.STONE_SHOVEL, 4),

    // This is a fallback for signed, crafted gear with a skin
    WEAPON(null, null, 12),
    // Note: This fallback should basically be never be matched, but we use it in item encoding
    //       (as it's the same as WEAPON, and we have no other info)
    ACCESSORY(null, null, 13),
    RING(null, "ring", 5),
    BRACELET(null, "bracelet", 6),
    NECKLACE(null, "necklace", 7),
    HELMET(
            null,
            Items.LEATHER_HELMET,
            "helmet",
            "helmet_skin",
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
            "chestplate",
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
            "leggings",
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
            "boots",
            List.of(
                    Items.CHAINMAIL_BOOTS,
                    Items.IRON_BOOTS,
                    Items.GOLDEN_BOOTS,
                    Items.DIAMOND_BOOTS,
                    Items.NETHERITE_BOOTS),
            11),
    MASTERY_TOME(null, "tome", -1),
    CHARM(null, "charm", -1);

    private final ClassType classReq;
    private final Item defaultItem;
    private final String modelKey;
    private final String skinModelKey;
    private final List<Item> otherItems;
    private final int encodingId;

    private List<Float> modelList = new ArrayList<>();
    private static final Map<Integer, GearType> craftedAccessoryMap = buildCraftedAccessoryMap();

    GearType(
            ClassType classReq,
            Item defaultItem,
            String modelKey,
            String skinModelKey,
            List<Item> otherItems,
            int encodingId) {
        this.classReq = classReq;
        this.defaultItem = defaultItem;
        this.modelKey = modelKey;
        this.skinModelKey = skinModelKey;
        this.otherItems = otherItems;
        this.encodingId = encodingId;
    }

    GearType(ClassType classReq, Item defaultItem, String modelKey, List<Item> otherItems, int encodingId) {
        this(classReq, defaultItem, modelKey, null, otherItems, encodingId);
    }

    GearType(ClassType classReq, String modelKey, Item craftedItem, int encodingId) {
        this(classReq, Items.POTION, modelKey, List.of(craftedItem), encodingId);
    }

    GearType(ClassType classReq, String modelKey, int encodingId) {
        this(classReq, Items.POTION, modelKey, List.of(), encodingId);
    }

    public static GearType fromString(String typeStr) {
        try {
            return GearType.valueOf(typeStr.toUpperCase(Locale.ROOT).replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static GearType fromItemStack(ItemStack itemStack, boolean crafted) {
        Item item = itemStack.getItem();
        CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);

        if (customModelData != null && !customModelData.floats().isEmpty()) {
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
        } else if (crafted) {
            // Crafted items still use the old textures, they have no custom model data but we can still
            // match them based on the item itself

            // For armor and weapons we can just match based on the item itself
            for (GearType gearType : values()) {
                if ((gearType.isArmor() && (gearType.defaultItem.equals(item) || gearType.otherItems.contains(item)))
                        || (gearType.isWeapon() && gearType.otherItems.contains(item))) {
                    return gearType;
                }
            }

            // For accessories, we use the damage value
            if (item == Items.FLINT_AND_STEEL) {
                int damage = itemStack.getDamageValue();

                return craftedAccessoryMap.getOrDefault(damage, null);
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
        if (Services.CustomModel.getRange(modelKey).isPresent()) {
            return Services.CustomModel.getRange(modelKey).get().a();
        } else {
            return -1f;
        }
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
        return defaultItem == Items.FLINT_AND_STEEL || defaultItem == Items.POTION;
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
        if (modelList.isEmpty()) {
            List<Float> tempModelList = new ArrayList<>();

            addRangeToList(Services.CustomModel.getRange(modelKey), tempModelList);
            addRangeToList(Services.CustomModel.getRange(skinModelKey), tempModelList);

            modelList = List.copyOf(tempModelList);
        }

        return modelList;
    }

    private static void addRangeToList(Optional<Pair<Float, Float>> modelRange, List<Float> out) {
        modelRange.ifPresent(range -> {
            int min = range.a().intValue();
            int max = range.b().intValue();
            IntStream.rangeClosed(min, max).mapToObj(i -> (float) i).forEach(out::add);
        });
    }

    private static Map<Integer, GearType> buildCraftedAccessoryMap() {
        Map<Integer, GearType> map = new HashMap<>();
        IntStream.rangeClosed(1, 17).forEach(i -> map.put(i, RING));
        IntStream.rangeClosed(18, 34).forEach(i -> map.put(i, NECKLACE));
        IntStream.rangeClosed(35, 49).forEach(i -> map.put(i, BRACELET));
        return Map.copyOf(map);
    }
}
