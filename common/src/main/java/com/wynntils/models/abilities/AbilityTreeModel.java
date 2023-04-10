/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.abilities.type.AbilityTreeLocation;
import com.wynntils.models.abilities.type.AbilityTreeSkillNode;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.IterationDecision;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AbilityTreeModel extends Model {
    private static final StyledText CONNECTION_NAME = StyledText.fromString(" ");

    private static final Pattern NODE_NAME_PATTERN = Pattern.compile("§.(Unlock )?§l(.+)(§r§. ability)?");
    private static final Pattern NODE_POINT_COST_PATTERN = Pattern.compile("§.. §7Ability Points: §f(\\d+)");
    private static final Pattern NODE_BLOCKS_ABILITY_PATTERN = Pattern.compile("§c- §7(.+)");
    private static final Pattern NODE_REQUIRED_ABILITY_PATTERN = Pattern.compile("§.. §7Required Ability: §f(.+)");
    private static final Pattern NODE_REQUIRED_ARCHETYPE_PATTERN =
            Pattern.compile("§.. §7Min (.+) Archetype: §c(\\d+)§7/(\\d+)");
    private static final Pattern NODE_ARCHETYPE_PATTERN = Pattern.compile("§.§l(.+) Archetype");

    public AbilityTreeModel() {
        super(List.of());
    }

    public AbilityTreeSkillNode parseNodeFromItem(ItemStack itemStack, int page, int slot) {
        StyledText nameStyledText = StyledText.fromComponent(itemStack.getHoverName());

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

        List<StyledText> loreStyledText = LoreUtils.getLoreStyledText(itemStack);

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
        }

        AbilityTreeSkillNode.ItemInformation itemInformation =
                new AbilityTreeSkillNode.ItemInformation(Item.getId(itemStack.getItem()), itemStack.getDamageValue());

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
        return node;
    }

    public boolean isNodeItem(ItemStack itemStack, int slot) {
        StyledText nameStyledText = StyledText.fromComponent(itemStack.getHoverName());
        return itemStack.getItem() == Items.STONE_AXE
                && slot < 54
                && nameStyledText.getMatcher(NODE_NAME_PATTERN).matches();
    }

    public boolean isConnectionItem(ItemStack itemStack) {
        return itemStack.getItem() == Items.STONE_AXE
                && StyledText.fromComponent(itemStack.getHoverName()).equals(CONNECTION_NAME);
    }
}
