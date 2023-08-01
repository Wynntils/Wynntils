/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.compress.utils.Lists;

public enum BeaconColor {
    GREEN(25, Items.GOLDEN_SHOVEL, ContentType.BOTH),
    PINK(24, Items.GOLDEN_SHOVEL, ContentType.CONTENT), // This is not used in lootruns
    YELLOW(3, Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    BLUE(4, Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    PURPLE(5, Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    GRAY(6, Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    ORANGE(7, Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    RED(8, Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    DARK_GRAY(9, Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    WHITE(10, Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    AQUA(11, Items.GOLDEN_PICKAXE, ContentType.LOOTRUN), // This is CYAN in the resource pack
    RAINBOW(12, Items.GOLDEN_PICKAXE, ContentType.LOOTRUN);

    private final int damage;
    private final Item item;
    private final ContentType contentType;

    BeaconColor(int damage, Item item, ContentType contentType) {
        this.damage = damage;
        this.item = item;
        this.contentType = contentType;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public static BeaconColor fromUnverifiedBeacon(UnverifiedBeacon unverifiedBeacon) {
        List<Entity> entities = unverifiedBeacon.getEntities();
        if (entities.isEmpty()) return null;

        Entity entity = entities.get(0);
        List<ItemStack> armorSlots = Lists.newArrayList(entity.getArmorSlots().iterator());
        if (armorSlots.size() != 4) return null;

        ItemStack bootsItem = armorSlots.get(3);
        return BeaconColor.fromItemStack(bootsItem);
    }

    public static BeaconColor fromItemStack(ItemStack itemStack) {
        for (BeaconColor color : values()) {
            if (color.damage == itemStack.getDamageValue() && color.item == itemStack.getItem()) {
                return color;
            }
        }

        return null;
    }

    public static BeaconColor fromName(String name) {
        for (BeaconColor color : values()) {
            if (color.name().equalsIgnoreCase(name)) {
                return color;
            }
        }

        return null;
    }

    public enum ContentType {
        LOOTRUN,
        CONTENT,
        BOTH;

        public boolean showsUpInLootruns() {
            return this == LOOTRUN || this == BOTH;
        }
    }
}
