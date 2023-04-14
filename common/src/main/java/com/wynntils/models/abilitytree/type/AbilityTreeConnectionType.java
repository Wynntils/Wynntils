/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum AbilityTreeConnectionType {
    FOUR_WAY(
            1,
            Map.ofEntries(
                    Map.entry(new boolean[] {true, true, true, true}, 2),
                    Map.entry(new boolean[] {true, true, false, true}, 3),
                    Map.entry(new boolean[] {true, true, true, false}, 4),
                    Map.entry(new boolean[] {false, true, true, true}, 5),
                    Map.entry(new boolean[] {true, false, true, true}, 6),
                    Map.entry(new boolean[] {true, false, false, true}, 7),
                    Map.entry(new boolean[] {true, true, false, false}, 8),
                    Map.entry(new boolean[] {false, true, true, false}, 9),
                    Map.entry(new boolean[] {false, false, true, true}, 10),
                    Map.entry(new boolean[] {true, false, true, false}, 11),
                    Map.entry(new boolean[] {false, true, false, true}, 12))),
    THREE_WAY_UP(
            13,
            Map.of(
                    new boolean[] {true, true, false, true}, 14,
                    new boolean[] {true, false, false, true}, 15,
                    new boolean[] {true, true, false, false}, 16,
                    new boolean[] {false, true, false, true}, 17)),

    THREE_WAY_RIGHT(
            18,
            Map.of(
                    new boolean[] {true, true, true, false}, 19,
                    new boolean[] {true, true, false, false}, 20,
                    new boolean[] {false, true, true, false}, 21,
                    new boolean[] {true, false, true, false}, 22)),
    THREE_WAY_DOWN(
            23,
            Map.of(
                    new boolean[] {false, true, true, true}, 24,
                    new boolean[] {false, false, true, true}, 25,
                    new boolean[] {false, true, true, false}, 26,
                    new boolean[] {false, true, false, true}, 27)),
    THREE_WAY_LEFT(
            28,
            Map.of(
                    new boolean[] {true, false, true, true}, 29,
                    new boolean[] {true, false, false, true}, 30,
                    new boolean[] {false, false, true, true}, 31,
                    new boolean[] {true, false, true, false}, 32)),
    UP_LEFT_TURN(
            33,
            Map.of(
                    new boolean[] {true, false, false, true},
                    34)), // Due to the nature of the ability tree connections, this type is unused
    UP_RIGHT_TURN(
            35,
            Map.of(
                    new boolean[] {true, true, false, false},
                    36)), // Due to the nature of the ability tree connections, this type is unused
    DOWN_LEFT_TURN(37, Map.of(new boolean[] {false, true, true, false}, 38)),
    DOWN_RIGHT_TURN(39, Map.of(new boolean[] {false, false, true, true}, 40)),
    VERTICAL(41, Map.of(new boolean[] {true, false, true, false}, 42)),
    HORIZONTAL(43, Map.of(new boolean[] {false, true, false, true}, 44));

    private final int baseDamage;
    private final Map<boolean[], Integer> activeDamageMap; // boolean[] is {up, right, down, left}

    private final ItemStack baseItemStack;
    private final Map<Integer, ItemStack> itemStackMap;

    AbilityTreeConnectionType(int baseDamage, Map<boolean[], Integer> activeDamageMap) {
        this.baseDamage = baseDamage;
        this.activeDamageMap = activeDamageMap;

        this.itemStackMap = new HashMap<>();

        this.baseItemStack = generateItemStack(baseDamage);
        for (boolean[] active : activeDamageMap.keySet()) {
            this.itemStackMap.put(Arrays.hashCode(active), generateItemStack(activeDamageMap.get(active)));
        }
    }

    private ItemStack generateItemStack(int damage) {
        ItemStack itemStack = new ItemStack(Items.STONE_AXE);

        itemStack.setDamageValue(damage);

        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);

        return itemStack;
    }

    public ItemStack getItemStack(boolean[] active) {
        return itemStackMap.getOrDefault(Arrays.hashCode(active), baseItemStack);
    }
}
