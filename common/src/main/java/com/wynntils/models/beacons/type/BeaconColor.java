/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.compress.utils.Lists;

public enum BeaconColor {
    YELLOW((int) (0.09375 * 32)),
    BLUE((int) (0.125 * 32)),
    PURPLE((int) (0.15625 * 32)),
    GRAY((int) (0.1875 * 32)),
    ORANGE((int) (0.21875 * 32)),
    RED((int) (0.25 * 32)),
    DARK_GRAY((int) (0.28125 * 32)),
    WHITE((int) (0.3125 * 32)),
    CYAN((int) (0.34375 * 32)),
    RAINBOW((int) (0.375 * 32));

    private final int damage;

    BeaconColor(int damage) {
        this.damage = damage;
    }

    public static BeaconColor fromEntity(Entity entity) {
        List<ItemStack> armorSlots = Lists.newArrayList(entity.getArmorSlots().iterator());
        if (armorSlots.size() != 4) return null;

        ItemStack bootItem = armorSlots.get(3);
        if (bootItem.getItem() == Items.GOLDEN_PICKAXE) {
            return fromDamage(bootItem.getDamageValue());
        }

        return null;
    }

    public static BeaconColor fromDamage(int damage) {
        for (BeaconColor color : values()) {
            if (color.damage == damage) {
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
}
