/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

import com.google.gson.annotations.SerializedName;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GearInfoContainer {
    private static final Pattern COLOR_PATTERN = Pattern.compile("(\\d{1,3}),(\\d{1,3}),(\\d{1,3})");

    private final GearType type;
    private final String set;
    private final GearDropType dropType;
    private final String armorColor;

    @SerializedName("name")
    private final String materialName;

    @SerializedName("damage")
    private final String metadata;

    public GearInfoContainer(
            GearType type, String set, GearDropType dropType, String armorColor, String materialName, String metadata) {
        this.type = type;
        this.set = set;
        this.dropType = dropType;
        this.armorColor = armorColor;
        this.materialName = materialName;
        this.metadata = metadata;
    }

    public GearDropType getDropType() {
        return dropType;
    }

    public GearType getType() {
        return type;
    }

    public String getArmorColor() {
        return armorColor;
    }

    public String getSet() {
        return set;
    }

    public boolean isArmorColorValid() {
        return armorColor != null && COLOR_PATTERN.matcher(armorColor).find();
    }

    public int getArmorColorAsInt() {
        if (armorColor == null) return 0;

        Matcher m = COLOR_PATTERN.matcher(getArmorColor());
        if (!m.find()) return 0;

        int r = Integer.parseInt(m.group(1));
        int g = Integer.parseInt(m.group(2));
        int b = Integer.parseInt(m.group(3));

        return (r << 16) + (g << 8) + b;
    }

    public ItemStack asItemStack() {
        if (materialName == null) {
            ItemStack stack = new ItemStack(type.getDefaultItem());
            stack.setDamageValue(type.getDefaultDamage());

            return stack;
        }

        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(materialName));
        ItemStack itemStack = item.map(ItemStack::new).orElseGet(() -> new ItemStack(Items.AIR));

        if (metadata != null) {
            itemStack.setDamageValue(Integer.parseInt(metadata));
        }

        return itemStack;
    }

    @Override
    public String toString() {
        return "ItemInfoContainer{" + "type="
                + type + ", set='"
                + set + '\'' + ", dropType="
                + dropType + ", armorColor='"
                + armorColor + '\'' + ", materialName='"
                + materialName + '\'' + ", metadata='"
                + metadata + '\'' + '}';
    }
}
