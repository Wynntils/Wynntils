/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.IterationDecision;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record AbilityTreeSkillNode(
        String name,
        String formattedName,
        List<String> description,
        ItemInformation itemInformation,
        int cost,
        List<String> blocks,
        String requiredAbility,
        ArchetypeRequirement requiredArchetype,
        String archetype,
        AbilityTreeLocation location,
        List<String> connections) {
    private static final Pattern NODE_NAME_PATTERN = Pattern.compile("§.(Unlock )?§l(.+)(§r§. ability)?");
    private static final Pattern NODE_POINT_COST_PATTERN = Pattern.compile("§.. §7Ability Points: §f(\\d+)");
    private static final Pattern NODE_BLOCKS_ABILITY_PATTERN = Pattern.compile("§c- §7(.+)");
    private static final Pattern NODE_REQUIRED_ABILITY_PATTERN = Pattern.compile("§.. §7Required Ability: §f(.+)");
    private static final Pattern NODE_REQUIRED_ARCHETYPE_PATTERN =
            Pattern.compile("§.. §7Min (.+) Archetype: §c(\\d+)§7/(\\d+)");
    private static final Pattern NODE_ARCHETYPE_PATTERN = Pattern.compile("§.§l(.+) Archetype");
    private static final Pattern NODE_BLOCKED = Pattern.compile("§cBlocked by another ability");
    private static final Pattern NODE_UNLOCKED = Pattern.compile("§eYou already unlocked this ability");

    public static Pair<AbilityTreeSkillNode, AbilityTreeNodeState> parseNodeFromItem(
            ItemStack itemStack, int page, int slot) {
        StyledText nameStyledText = StyledText.fromComponent(itemStack.getHoverName());

        AbilityTreeNodeState state = AbilityTreeNodeState.LOCKED;
        StyledText actualName;
        if (nameStyledText.getPartCount() == 1) {
            actualName = nameStyledText;
        } else {
            actualName = nameStyledText.iterate((part, changes) -> {
                // The part which is bolded is the actual name of the ability
                if (!part.getPartStyle().isBold()) {
                    changes.clear();
                }

                return IterationDecision.CONTINUE;
            });
            state = AbilityTreeNodeState.UNLOCKABLE;
        }

        List<StyledText> loreStyledText = LoreUtils.getLoreStyledText(itemStack);

        if (state == AbilityTreeNodeState.UNLOCKABLE) {
            // Skip empty line + "click here to unlock"
            loreStyledText = loreStyledText.subList(0, loreStyledText.size() - 2);
        }
        // FIXME: Skip other...

        int cost = 0;
        List<String> blocks = new ArrayList<>();
        String requiredAbility = null;
        AbilityTreeSkillNode.ArchetypeRequirement requiredArchetype = null;
        String archetype = null;

        for (StyledText text : loreStyledText) {
            Matcher matcher = text.getMatcher(NODE_POINT_COST_PATTERN);
            if (matcher.matches()) {
                cost = Integer.parseInt(matcher.group(1));
                continue;
            }

            matcher = text.getMatcher(NODE_BLOCKS_ABILITY_PATTERN);
            if (matcher.matches()) {
                blocks.add(matcher.group(1));
                continue;
            }

            matcher = text.getMatcher(NODE_REQUIRED_ABILITY_PATTERN);
            if (matcher.matches()) {
                requiredAbility = matcher.group(1);
                continue;
            }

            matcher = text.getMatcher(NODE_REQUIRED_ARCHETYPE_PATTERN);
            if (matcher.matches()) {
                requiredArchetype = new AbilityTreeSkillNode.ArchetypeRequirement(
                        matcher.group(1), Integer.parseInt(matcher.group(3)));
                continue;
            }

            matcher = text.getMatcher(NODE_ARCHETYPE_PATTERN);
            if (matcher.matches()) {
                archetype = matcher.group(1);
                continue;
            }

            matcher = text.getMatcher(NODE_BLOCKED);
            if (matcher.matches()) {
                state = AbilityTreeNodeState.BLOCKED;
                continue;
            }

            matcher = text.getMatcher(NODE_UNLOCKED);
            if (matcher.matches()) {
                state = AbilityTreeNodeState.UNLOCKED;
                continue;
            }
        }

        AbilityTreeSkillNode.ItemInformation itemInformation = new AbilityTreeSkillNode.ItemInformation(
                Item.getId(itemStack.getItem()),
                switch (state) {
                    case LOCKED -> itemStack.getDamageValue();
                    case UNLOCKABLE -> itemStack.getDamageValue() - 1;
                    case UNLOCKED -> itemStack.getDamageValue() - 2;
                    case BLOCKED -> itemStack.getDamageValue() - 3;
                });

        AbilityTreeSkillNode node = new AbilityTreeSkillNode(
                actualName.getString(PartStyle.StyleType.NONE),
                actualName.getString(PartStyle.StyleType.DEFAULT),
                loreStyledText.stream()
                        .map(styledText -> styledText.getString(PartStyle.StyleType.DEFAULT))
                        .toList(),
                itemInformation,
                cost,
                blocks,
                requiredAbility,
                requiredArchetype,
                archetype,
                AbilityTreeLocation.fromSlot(slot, page),
                new ArrayList<>());

        return Pair.of(node, state);
    }

    public static boolean isNodeItem(ItemStack itemStack, int slot) {
        StyledText nameStyledText = StyledText.fromComponent(itemStack.getHoverName());
        return itemStack.getItem() == Items.STONE_AXE
                && slot < 54
                && nameStyledText.getMatcher(NODE_NAME_PATTERN).matches();
    }

    public record ArchetypeRequirement(String name, int required) {}

    public record ItemInformation(int itemId, int damage) {
        public int getBlockedDamage() {
            return damage + 3;
        }

        public int getLockedDamage() {
            return damage;
        }

        public int getUnlockableDamage() {
            return damage + 1;
        }

        public int getUnlockedDamage() {
            return damage + 2;
        }
    }
}
