/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.type;

import com.google.gson.JsonArray;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.abilitytree.type.ArchetypeInfo;
import com.wynntils.models.abilitytree.type.ArchetypeRequirement;
import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;

public class AbilityTreeNodeItemStack extends ItemStack {
    private final AbilityTreeSkillNode abilityTreeSkillNode;
    private final SavableAbilityTree savableAbilityTree;

    public AbilityTreeNodeItemStack(AbilityTreeSkillNode abilityTreeSkillNode, SavableAbilityTree savableAbilityTree) {
        super(Items.POTION, 1);

        this.abilityTreeSkillNode = abilityTreeSkillNode;
        this.savableAbilityTree = savableAbilityTree;

        buildTooltip();
    }

    public void buildTooltip() {
        this.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);

        // name
        this.set(
                DataComponents.CUSTOM_NAME,
                StyledText.fromString(abilityTreeSkillNode.formattedName()).getComponent());

        // Custom model data
        float customModelData = abilityTreeSkillNode
                .abilityTreeNodeType()
                .getUnlockedType()
                .getCustomModelData()
                .orElse(-1f);

        this.set(
                DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(customModelData), List.of(), List.of(), List.of()));

        // Lore/tooltip
        List<Component> loreLines = new ArrayList<>();

        // Description
        for (JsonArray descLine : abilityTreeSkillNode.description()) {
            loreLines.add(StyledText.fromJson(descLine).getComponent());
        }

        // Archetype
        ArchetypeInfo archetypeInfo = abilityTreeSkillNode.archetypeInfo();
        if (archetypeInfo != null) {
            loreLines.add(Component.empty());

            int color = 0xffffff;
            if (archetypeInfo.color() != null) {
                String colorStr = archetypeInfo.color();
                if (colorStr.startsWith("#")) {
                    colorStr = colorStr.substring(1);
                }
                // Strip alpha channel if present (RRGGBBAA -> RRGGBB)
                if (colorStr.length() == 8) {
                    colorStr = colorStr.substring(0, 6);
                }
                color = Integer.parseInt(colorStr, 16);
            }

            loreLines.add(Component.literal(archetypeInfo.archetype() + " Archetype")
                    .withStyle(Style.EMPTY.withColor(color).withBold(true).withItalic(false)));
        }

        // Cost
        loreLines.add(Component.empty());
        loreLines.add(Component.literal("✔ ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false))
                .append(Component.literal("Ability Points: ")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                        .append(Component.literal(String.valueOf(abilityTreeSkillNode.cost()))
                                .withStyle(Style.EMPTY
                                        .withColor(ChatFormatting.WHITE)
                                        .withItalic(false)))));

        // Required ability
        String requiredAbility = abilityTreeSkillNode.requiredAbility();
        if (requiredAbility != null && !requiredAbility.isEmpty()) {
            loreLines.add(Component.literal("✔ ")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false))
                    .append(Component.literal("Required Ability: ")
                            .withStyle(
                                    Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                            .append(Component.literal(requiredAbility)
                                    .withStyle(Style.EMPTY
                                            .withColor(ChatFormatting.WHITE)
                                            .withItalic(false)))));
        }

        // Required archetype
        ArchetypeRequirement requiredArchetype = abilityTreeSkillNode.requiredArchetype();
        if (requiredArchetype != null) {
            loreLines.add(Component.literal("✔ ")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false))
                    .append(Component.literal("Min " + requiredArchetype.name() + " Archetype: ")
                            .withStyle(
                                    Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                            .append(Component.literal(String.valueOf(
                                            savableAbilityTree.getArchetypeCount(requiredArchetype.name())))
                                    .withStyle(Style.EMPTY
                                            .withColor(ChatFormatting.WHITE)
                                            .withItalic(false)))
                            .append(Component.literal("/" + requiredArchetype.required())
                                    .withStyle(Style.EMPTY
                                            .withColor(ChatFormatting.GRAY)
                                            .withItalic(false)))));
        }

        // Required combat level
        int requiredLevel = abilityTreeSkillNode.requiredLevel();
        if (requiredLevel > 0) {
            loreLines.add(Component.literal("✔ ")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false))
                    .append(Component.literal("Combat Lv. min: ")
                            .withStyle(
                                    Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                            .append(Component.literal(String.valueOf(requiredLevel))
                                    .withStyle(Style.EMPTY
                                            .withColor(ChatFormatting.WHITE)
                                            .withItalic(false)))));
        }

        this.set(DataComponents.LORE, new ItemLore(loreLines));
        this.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext tooltipContext, Player player, TooltipFlag tooltipFlag) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(this.getHoverName());

        ItemLore lore = this.get(DataComponents.LORE);
        if (lore != null) {
            tooltip.addAll(lore.lines());
        }

        return tooltip;
    }
}
