/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import com.wynntils.models.activities.type.ActivityType;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.compress.utils.Lists;

public enum BeaconColor {
    GREEN(25, Items.GOLDEN_SHOVEL, true, ActivityType.STORYLINE_QUEST),
    PINK(24, Items.GOLDEN_SHOVEL, false, ActivityType.BOSS_ALTAR),
    YELLOW(3, Items.GOLDEN_PICKAXE, true, ActivityType.RAID),
    BLUE(4, Items.GOLDEN_PICKAXE, true, ActivityType.LOOTRUN_CAMP),
    PURPLE(5, Items.GOLDEN_PICKAXE, true, ActivityType.MINI_QUEST),
    GRAY(6, Items.GOLDEN_PICKAXE, true, null),
    ORANGE(7, Items.GOLDEN_PICKAXE, true, ActivityType.CAVE),
    RED(8, Items.GOLDEN_PICKAXE, true, ActivityType.DUNGEON),
    DARK_GRAY(9, Items.GOLDEN_PICKAXE, true, null),
    WHITE(
            10,
            Items.GOLDEN_PICKAXE,
            true,
            ActivityType.WORLD_DISCOVERY), // can also be ActivityType.TERRITORIAL_DISCOVERY
    AQUA(11, Items.GOLDEN_PICKAXE, true, ActivityType.QUEST), // This is CYAN in the resource pack
    RAINBOW(12, Items.GOLDEN_PICKAXE, true, null);

    private final int damageValue;
    private final Item item;
    private final boolean usedInLootruns;
    private final ActivityType activityType;

    BeaconColor(int damageValue, Item item, boolean usedInLootruns, ActivityType activityType) {
        this.damageValue = damageValue;
        this.item = item;
        this.usedInLootruns = usedInLootruns;
        this.activityType = activityType;
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

    public boolean isUsedInLootruns() {
        return usedInLootruns;
    }

    public ActivityType getActivityType() {
        return activityType;
    }
}
