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
    GREEN((int) (0.78125 * 32), Items.GOLDEN_SHOVEL, ContentType.BOTH),
    PINK((int) (0.75 * 32), Items.GOLDEN_SHOVEL, ContentType.CONTENT), // This is not used in lootruns
    YELLOW((int) (0.09375 * 32), Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    BLUE((int) (0.125 * 32), Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    PURPLE((int) (0.15625 * 32), Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    GRAY((int) (0.1875 * 32), Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    ORANGE((int) (0.21875 * 32), Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    RED((int) (0.25 * 32), Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    DARK_GRAY((int) (0.28125 * 32), Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    WHITE((int) (0.3125 * 32), Items.GOLDEN_PICKAXE, ContentType.LOOTRUN),
    AQUA((int) (0.34375 * 32), Items.GOLDEN_PICKAXE, ContentType.LOOTRUN), // This is CYAN in the resource pack
    RAINBOW((int) (0.375 * 32), Items.GOLDEN_PICKAXE, ContentType.LOOTRUN);

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
