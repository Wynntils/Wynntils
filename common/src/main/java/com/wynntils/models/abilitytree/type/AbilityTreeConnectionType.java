/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.utils.type.Pair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

public enum AbilityTreeConnectionType {
    VERTICAL(
            "abilityTree.branchVertical",
            Map.of(new boolean[] {true, false, true, false}, "abilityTree.branchVerticalActiveUD"),
            new boolean[] {true, false, true, false},
            List.of()),
    HORIZONTAL(
            "abilityTree.branchHorizontal",
            Map.of(new boolean[] {false, true, false, true}, "abilityTree.branchHorizontalActiveRL"),
            new boolean[] {false, true, false, true},
            List.of()),

    DOWN_LEFT_TURN(
            "abilityTree.branchDownLeftTurn",
            Map.of(new boolean[] {false, false, true, true}, "abilityTree.branchDownLeftTurnActiveDL"),
            new boolean[] {false, false, true, true},
            List.of()),
    DOWN_RIGHT_TURN(
            "abilityTree.branchDownRightTurn",
            Map.of(new boolean[] {false, true, true, false}, "abilityTree.branchDownRightTurnActiveRD"),
            new boolean[] {false, true, true, false},
            List.of()),

    UP_LEFT_TURN(
            "abilityTree.branchUpLeftTurn",
            Map.of(new boolean[] {true, false, false, true}, "abilityTree.branchUpLeftTurnActiveUL"),
            new boolean[] {true, false, false, true},
            List.of()), // Due to the nature of the ability tree connections, this type is unused
    UP_RIGHT_TURN(
            "abilityTree.branchUpRightTurn",
            Map.of(new boolean[] {true, true, false, false}, "abilityTree.branchUpRightTurnActiveUR"),
            new boolean[] {true, true, false, false},
            List.of()), // Due to the nature of the ability tree connections, this type is unused

    THREE_WAY_UP(
            "abilityTree.branchThreeWayUp",
            Map.of(
                    new boolean[] {true, true, false, true}, "abilityTree.branchThreeWayUpActiveURL",
                    new boolean[] {true, false, false, true}, "abilityTree.branchThreeWayUpActiveUL",
                    new boolean[] {true, true, false, false}, "abilityTree.branchThreeWayUpActiveUR",
                    new boolean[] {false, true, false, true}, "abilityTree.branchThreeWayUpActiveRL"),
            new boolean[] {true, true, false, true},
            List.of(
                    Pair.of(HORIZONTAL, UP_LEFT_TURN),
                    Pair.of(HORIZONTAL, UP_RIGHT_TURN),
                    Pair.of(UP_LEFT_TURN, UP_RIGHT_TURN))),

    THREE_WAY_RIGHT(
            "abilityTree.branchThreeWayRight",
            Map.of(
                    new boolean[] {true, true, true, false}, "abilityTree.branchThreeWayRightActiveURD",
                    new boolean[] {true, true, false, false}, "abilityTree.branchThreeWayRightActiveUR",
                    new boolean[] {false, true, true, false}, "abilityTree.branchThreeWayRightActiveRD",
                    new boolean[] {true, false, true, false}, "abilityTree.branchThreeWayRightActiveUD"),
            new boolean[] {true, true, true, false},
            List.of(
                    Pair.of(VERTICAL, DOWN_RIGHT_TURN),
                    Pair.of(VERTICAL, UP_RIGHT_TURN),
                    Pair.of(DOWN_LEFT_TURN, UP_LEFT_TURN))),
    THREE_WAY_DOWN(
            "abilityTree.branchThreeWayDown",
            Map.of(
                    new boolean[] {false, true, true, true}, "abilityTree.branchThreeWayDownActiveRDL",
                    new boolean[] {false, false, true, true}, "abilityTree.branchThreeWayDownActiveDL",
                    new boolean[] {false, true, true, false}, "abilityTree.branchThreeWayDownActiveRD",
                    new boolean[] {false, true, false, true}, "abilityTree.branchThreeWayDownActiveRL"),
            new boolean[] {false, true, true, true},
            List.of(
                    Pair.of(HORIZONTAL, DOWN_LEFT_TURN),
                    Pair.of(HORIZONTAL, DOWN_RIGHT_TURN),
                    Pair.of(DOWN_LEFT_TURN, DOWN_RIGHT_TURN))),
    THREE_WAY_LEFT(
            "abilityTree.branchThreeWayLeft",
            Map.of(
                    new boolean[] {true, false, true, true}, "abilityTree.branchThreeWayLeftActiveUDL",
                    new boolean[] {true, false, false, true}, "abilityTree.branchThreeWayLeftActiveUL",
                    new boolean[] {false, false, true, true}, "abilityTree.branchThreeWayLeftActiveDL",
                    new boolean[] {true, false, true, false}, "abilityTree.branchThreeWayLeftActiveUD"),
            new boolean[] {true, false, true, true},
            List.of(
                    Pair.of(VERTICAL, DOWN_LEFT_TURN),
                    Pair.of(VERTICAL, UP_LEFT_TURN),
                    Pair.of(DOWN_RIGHT_TURN, UP_RIGHT_TURN))),

    FOUR_WAY(
            "abilityTree.branchFourWay",
            Map.ofEntries(
                    Map.entry(new boolean[] {true, true, true, true}, "abilityTree.branchFourWayActiveURDL"),
                    Map.entry(new boolean[] {true, true, false, true}, "abilityTree.branchFourWayActiveURL"),
                    Map.entry(new boolean[] {true, true, true, false}, "abilityTree.branchFourWayActiveURD"),
                    Map.entry(new boolean[] {false, true, true, true}, "abilityTree.branchFourWayActiveRDL"),
                    Map.entry(new boolean[] {true, false, true, true}, "abilityTree.branchFourWayActiveUDL"),
                    Map.entry(new boolean[] {true, false, false, true}, "abilityTree.branchFourWayActiveUL"),
                    Map.entry(new boolean[] {true, true, false, false}, "abilityTree.branchFourWayActiveUR"),
                    Map.entry(new boolean[] {false, true, true, false}, "abilityTree.branchFourWayActiveRD"),
                    Map.entry(new boolean[] {false, false, true, true}, "abilityTree.branchFourWayActiveDL"),
                    Map.entry(new boolean[] {true, false, true, false}, "abilityTree.branchFourWayActiveUD"),
                    Map.entry(new boolean[] {false, true, false, true}, "abilityTree.branchFourWayActiveRL")),
            new boolean[] {true, true, true, true},
            List.of(
                    Pair.of(VERTICAL, HORIZONTAL),
                    Pair.of(DOWN_LEFT_TURN, UP_RIGHT_TURN),
                    Pair.of(DOWN_RIGHT_TURN, UP_LEFT_TURN),
                    Pair.of(VERTICAL, THREE_WAY_UP),
                    Pair.of(DOWN_LEFT_TURN, THREE_WAY_UP),
                    Pair.of(DOWN_RIGHT_TURN, THREE_WAY_UP),
                    Pair.of(HORIZONTAL, THREE_WAY_RIGHT),
                    Pair.of(DOWN_LEFT_TURN, THREE_WAY_RIGHT),
                    Pair.of(UP_LEFT_TURN, THREE_WAY_RIGHT),
                    Pair.of(VERTICAL, THREE_WAY_DOWN),
                    Pair.of(UP_LEFT_TURN, THREE_WAY_DOWN),
                    Pair.of(UP_RIGHT_TURN, THREE_WAY_DOWN),
                    Pair.of(HORIZONTAL, THREE_WAY_LEFT),
                    Pair.of(DOWN_RIGHT_TURN, THREE_WAY_LEFT),
                    Pair.of(UP_RIGHT_TURN, THREE_WAY_LEFT)));

    private final String baseKey;

    // boolean[] is {up, right, down, left}
    private final Map<boolean[], String> activeKeyMap;

    // This is a list of all possible directions that this type can connect to.
    private final boolean[] possibleDirections;

    // This is a list of all possible merges between two AbilityTreeConnectionTypes. Self merges (this type + other
    // type) are not included.
    private final List<Pair<AbilityTreeConnectionType, AbilityTreeConnectionType>> possibleMerges;

    // A set of compatible types. If this type is merged with a type in this set, the result will be this type.
    private final Set<AbilityTreeConnectionType> selfMergeTypes;

    private final ItemStack baseItemStack;
    private final Map<Integer, ItemStack> itemStackMap;

    AbilityTreeConnectionType(
            String baseKey,
            Map<boolean[], String> activeKeyMap,
            boolean[] possibleDirections,
            List<Pair<AbilityTreeConnectionType, AbilityTreeConnectionType>> possibleMerges) {
        this.baseKey = baseKey;
        this.activeKeyMap = activeKeyMap;
        this.possibleDirections = possibleDirections;
        this.possibleMerges = possibleMerges;

        this.itemStackMap = new HashMap<>();

        this.baseItemStack = generateItemStack(baseKey);
        for (boolean[] active : activeKeyMap.keySet()) {
            this.itemStackMap.put(Arrays.hashCode(active), generateItemStack(activeKeyMap.get(active)));
        }

        this.selfMergeTypes = new HashSet<>();

        this.selfMergeTypes.add(this);

        for (Pair<AbilityTreeConnectionType, AbilityTreeConnectionType> merge : possibleMerges) {
            this.selfMergeTypes.add(merge.a());
            this.selfMergeTypes.add(merge.b());
        }
    }

    public static AbilityTreeConnectionType fromItemStack(ItemStack itemStack) {
        if (!itemStack.has(DataComponents.CUSTOM_MODEL_DATA)) return null;

        List<Float> floats = itemStack.get(DataComponents.CUSTOM_MODEL_DATA).floats();
        if (floats.isEmpty()) return null;

        return fromCustomModelData(floats.getFirst());
    }

    public static AbilityTreeConnectionType fromCustomModelData(float customModelData) {
        for (AbilityTreeConnectionType type : values()) {
            if (matchesKey(type.baseKey, customModelData)) {
                return type;
            }

            for (String activeKey : type.activeKeyMap.values()) {
                if (matchesKey(activeKey, customModelData)) {
                    return type;
                }
            }
        }

        return null;
    }

    private static boolean matchesKey(String key, float customModelData) {
        return Services.CustomModel.getFloat(key)
                .map(value -> value == customModelData)
                .orElse(false);
    }

    public ItemStack getItemStack(boolean[] active) {
        return itemStackMap.getOrDefault(Arrays.hashCode(active), baseItemStack);
    }

    public boolean[] getPossibleDirections() {
        return possibleDirections;
    }

    public boolean isCompatible(AbilityTreeConnectionType other) {
        return selfMergeTypes.contains(other);
    }

    public static AbilityTreeConnectionType merge(AbilityTreeConnectionType first, AbilityTreeConnectionType second) {
        // If we are merging the same type, it does not change
        if (first == second) {
            return first;
        }

        // If the second type is compatible, the result is the first type
        if (first.selfMergeTypes.contains(second)) {
            return first;
        }

        // If the first type is compatible, the result is the second type
        if (second.selfMergeTypes.contains(first)) {
            return second;
        }

        // Swap the two variables so they are in order
        if (second.ordinal() < first.ordinal()) {
            AbilityTreeConnectionType temp = first;
            first = second;
            second = temp;
        }

        // Check if the two types can be merged
        for (AbilityTreeConnectionType type : values()) {
            for (Pair<AbilityTreeConnectionType, AbilityTreeConnectionType> pair : type.possibleMerges) {
                if (pair.a() == first && pair.b() == second) {
                    return type;
                }
            }
        }

        WynntilsMod.error(
                "Tried to merge two incompatible AbilityTreeConnectionTypes: " + first + " and " + second + ".");

        return first;
    }

    private ItemStack generateItemStack(String customModelDataKey) {
        ItemStack itemStack = new ItemStack(Items.POTION);

        float customModelData =
                Services.CustomModel.getFloat(customModelDataKey).orElse(-1f);

        itemStack.set(
                DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(customModelData), List.of(), List.of(), List.of()));

        itemStack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);

        return itemStack;
    }
}
