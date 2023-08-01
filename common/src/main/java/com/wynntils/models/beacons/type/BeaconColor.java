/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum BeaconColor {
    GREEN(25, Items.GOLDEN_SHOVEL, true, ActivityType.STORYLINE_QUEST, CommonColors.GREEN),
    PINK(24, Items.GOLDEN_SHOVEL, false, ActivityType.BOSS_ALTAR, CommonColors.PINK),
    YELLOW(3, Items.GOLDEN_PICKAXE, true, ActivityType.RAID, CommonColors.YELLOW),
    BLUE(4, Items.GOLDEN_PICKAXE, true, ActivityType.LOOTRUN_CAMP, CommonColors.BLUE),
    PURPLE(5, Items.GOLDEN_PICKAXE, true, ActivityType.MINI_QUEST, CommonColors.PURPLE),
    GRAY(6, Items.GOLDEN_PICKAXE, true, null, CommonColors.GRAY),
    ORANGE(7, Items.GOLDEN_PICKAXE, true, ActivityType.CAVE, CommonColors.ORANGE),
    RED(8, Items.GOLDEN_PICKAXE, true, ActivityType.DUNGEON, CommonColors.RED),
    DARK_GRAY(9, Items.GOLDEN_PICKAXE, true, null, CommonColors.DARK_GRAY),
    WHITE(
            10,
            Items.GOLDEN_PICKAXE,
            true,
            ActivityType.WORLD_DISCOVERY,
            CommonColors.WHITE), // or ActivityType.TERRITORIAL_DISCOVERY
    AQUA(11, Items.GOLDEN_PICKAXE, true, ActivityType.QUEST, CommonColors.CYAN), // This is CYAN in the resource pack
    RAINBOW(12, Items.GOLDEN_PICKAXE, true, null, CommonColors.RAINBOW);

    private final int damageValue;
    private final Item item;
    private final boolean usedInLootruns;
    private final ActivityType activityType;
    private final CustomColor color;

    BeaconColor(int damageValue, Item item, boolean usedInLootruns, ActivityType activityType, CustomColor color) {
        this.damageValue = damageValue;
        this.item = item;
        this.usedInLootruns = usedInLootruns;
        this.activityType = activityType;
        this.color = color;
    }

    public static BeaconColor fromItemStack(ItemStack itemStack) {
        for (BeaconColor color : values()) {
            if (color.damageValue == itemStack.getDamageValue() && color.item == itemStack.getItem()) {
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

    public static BeaconColor fromActivityType(ActivityType activityType) {
        for (BeaconColor color : values()) {
            if (color.activityType == activityType) {
                return color;
            }
        }

        // As good as any
        return WHITE;
    }

    public boolean isUsedInLootruns() {
        return usedInLootruns;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public CustomColor getColor() {
        return color;
    }
}
