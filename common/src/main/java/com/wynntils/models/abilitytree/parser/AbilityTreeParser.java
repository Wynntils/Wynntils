/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.parser;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.models.abilitytree.type.AbilityTreeLocation;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeState;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeType;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.abilitytree.type.ArchetypeInfo;
import com.wynntils.models.abilitytree.type.ArchetypeRequirement;
import com.wynntils.models.abilitytree.type.LoreParserState;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.IterationDecision;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AbilityTreeParser {
    private static final Pattern NODE_NAME_PATTERN = Pattern.compile(
            "§(?:#[0-9a-fA-F]{8}|.)(?:(Unlock )§(?:#[0-9a-fA-F]{8}|.))?§l(.+?)(?:§(?:#[0-9a-fA-F]{8}|.) ability)?$");
    private static final Pattern NODE_POINT_COST_PATTERN = Pattern.compile("§.. §7Ability Points: §f(\\d+)");
    private static final Pattern NODE_REQUIRED_ABILITY_PATTERN = Pattern.compile("§.. §7Required Ability: §f(.+)");
    private static final Pattern NODE_REQUIRED_ARCHETYPE_PATTERN =
            Pattern.compile("§.. §7Min (.+) Archetype: §.(\\d+)§7/(\\d+)");
    private static final Pattern NODE_ARCHETYPE_PATTERN = Pattern.compile("§(#[0-9a-fA-F]{8})§l(.+) Archetype");
    private static final Pattern NODE_BLOCKED_BY = Pattern.compile("§c§lBlocked by:");
    private static final Pattern NODE_BLOCKED_BY_ABILITY = Pattern.compile("§c- (.*)");
    private static final Pattern NODE_UNLOCKING_WILL_BLOCK = Pattern.compile("§cUnlocking will block:");
    private static final Pattern NODE_UNLOCKING_WILL_BLOCK_ABILITY = Pattern.compile("§c- §7(.*)");
    private static final Pattern NODE_BLOCKED = Pattern.compile("§cBlocked by another ability");
    private static final Pattern NODE_REQUIREMENT_NOT_MET = Pattern.compile("§cYou do not meet the requirements");
    private static final Pattern NODE_COMBAT_LEVEL = Pattern.compile("§.. §7Combat Lv. Min: §f(\\d{1,3})");
    private static final Pattern NODE_CLICK_TO_UNLOCK = Pattern.compile("§f\uF000§a Click to unlock this ability");

    private static final StyledText CONNECTION_NAME = StyledText.fromString("\uDB3F\uDFFF");

    public Pair<AbilityTreeSkillNode, AbilityTreeNodeState> parseNodeFromItem(
            ItemStack itemStack, int page, int slot, int id) {
        StyledText nameStyledText = StyledText.fromComponent(itemStack.getHoverName());

        AbilityTreeNodeType abilityTreeNodeType = AbilityTreeNodeType.fromItemStack(itemStack);
        AbilityTreeNodeState state = abilityTreeNodeType.getState();

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
        }

        List<StyledText> loreStyledText = LoreUtils.getLore(itemStack);

        int cost = 0;
        List<String> blockedBy = new ArrayList<>();
        List<String> willBlock = new ArrayList<>();
        String requiredAbility = null;
        ArchetypeRequirement requiredArchetype = null;
        ArchetypeInfo archetypeInfo = null;
        int requiredLevel = 0;

        List<StyledText> includedLines = new ArrayList<>(loreStyledText);

        LoreParserState currentSection = LoreParserState.NONE;

        for (StyledText text : loreStyledText) {
            Matcher matcher = text.getMatcher(NODE_POINT_COST_PATTERN);
            if (matcher.matches()) {
                cost = Integer.parseInt(matcher.group(1));
                includedLines.remove(text); // skip in description
                currentSection = LoreParserState.NONE;
                continue;
            }

            matcher = text.getMatcher(NODE_REQUIRED_ABILITY_PATTERN);
            if (matcher.matches()) {
                requiredAbility = matcher.group(1);
                includedLines.remove(text); // skip in description
                currentSection = LoreParserState.NONE;
                continue;
            }

            matcher = text.getMatcher(NODE_REQUIRED_ARCHETYPE_PATTERN);
            if (matcher.matches()) {
                requiredArchetype = new ArchetypeRequirement(matcher.group(1), Integer.parseInt(matcher.group(3)));
                includedLines.remove(text); // skip in description
                currentSection = LoreParserState.NONE;
                continue;
            }

            matcher = text.getMatcher(NODE_ARCHETYPE_PATTERN);
            if (matcher.matches()) {
                archetypeInfo = new ArchetypeInfo(matcher.group(2), matcher.group(1));
                includedLines.remove(text); // skip in description
                currentSection = LoreParserState.NONE;
                continue;
            }

            matcher = text.getMatcher(NODE_BLOCKED);
            if (matcher.matches()) {
                includedLines.remove(text); // skip in description
                currentSection = LoreParserState.NONE;
                continue;
            }

            matcher = text.getMatcher(NODE_COMBAT_LEVEL);
            if (matcher.matches()) {
                requiredLevel = Integer.parseInt(matcher.group(1));
                includedLines.remove(text); // skip in description
                currentSection = LoreParserState.NONE;
                continue;
            }

            matcher = text.getMatcher(NODE_REQUIREMENT_NOT_MET);
            if (matcher.matches()) {
                includedLines.remove(text); // skip in description
                currentSection = LoreParserState.NONE;
                continue;
            }

            matcher = text.getMatcher(NODE_CLICK_TO_UNLOCK);
            if (matcher.matches()) {
                includedLines.remove(text); // skip in description
                currentSection = LoreParserState.NONE;
                continue;
            }

            matcher = text.getMatcher(NODE_BLOCKED_BY);
            if (matcher.matches()) {
                currentSection = LoreParserState.BLOCKED_BY;
                includedLines.remove(text); // skip in description
                continue;
            }

            matcher = text.getMatcher(NODE_UNLOCKING_WILL_BLOCK);
            if (matcher.matches()) {
                currentSection = LoreParserState.UNLOCKING_WILL_BLOCK;
                includedLines.remove(text); // skip in description
                continue;
            }

            if (currentSection == LoreParserState.BLOCKED_BY) {
                matcher = text.getMatcher(NODE_BLOCKED_BY_ABILITY);
                if (matcher.matches()) {
                    blockedBy.add(matcher.group(1));
                    includedLines.remove(text); // skip in description
                    continue;
                } else {
                    currentSection = LoreParserState.NONE;
                }
            }

            if (currentSection == LoreParserState.UNLOCKING_WILL_BLOCK) {
                matcher = text.getMatcher(NODE_UNLOCKING_WILL_BLOCK_ABILITY);
                if (matcher.matches()) {
                    willBlock.add(matcher.group(1));
                    includedLines.remove(text); // skip in description
                    continue;
                } else {
                    currentSection = LoreParserState.NONE;
                }
            }
        }

        // Remove empty lines from the end of the description
        while (includedLines.getLast().getString(StyleType.NONE).isBlank()) {
            includedLines.removeLast();
        }

        AbilityTreeSkillNode node = new AbilityTreeSkillNode(
                id,
                actualName.getString(StyleType.NONE),
                actualName.getString(StyleType.DEFAULT),
                abilityTreeNodeType,
                includedLines.stream().map(StyledText::toJson).toList(),
                cost,
                willBlock,
                blockedBy,
                requiredAbility,
                requiredArchetype,
                requiredLevel,
                archetypeInfo,
                AbilityTreeLocation.fromSlot(slot, page),
                new ArrayList<>());

        return Pair.of(node, state);
    }

    public boolean isNodeItem(ItemStack itemStack, int slot) {
        StyledText nameStyledText = StyledText.fromComponent(itemStack.getHoverName());
        return itemStack.getItem() == Items.POTION
                && slot < 54
                && nameStyledText.getMatcher(NODE_NAME_PATTERN).matches();
    }

    public boolean isConnectionItem(ItemStack itemStack, int slot) {
        return itemStack.getItem() == Items.POTION
                && slot < 54
                && StyledText.fromComponent(itemStack.getHoverName())
                        .getString()
                        .equals(CONNECTION_NAME.getString());
    }
}
