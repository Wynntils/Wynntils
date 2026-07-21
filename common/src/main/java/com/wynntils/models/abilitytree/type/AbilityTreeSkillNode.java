/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.google.gson.JsonArray;
import com.wynntils.core.text.StyledText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.SequencedSet;
import java.util.Set;

public record AbilityTreeSkillNode(
        int id,
        String name,
        String formattedName,
        AbilityTreeNodeType abilityTreeNodeType,
        List<JsonArray> description,
        int cost,
        List<String> willBlock,
        List<String> blockedBy,
        String requiredAbility,
        ArchetypeRequirement requiredArchetype,
        int requiredLevel,
        ArchetypeInfo archetypeInfo,
        AbilityTreeLocation location,
        List<Integer> connections) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbilityTreeSkillNode that = (AbilityTreeSkillNode) o;
        return id == that.id
                && cost == that.cost
                && Objects.equals(formattedName, that.formattedName)
                && Objects.equals(requiredAbility, that.requiredAbility)
                && Objects.equals(requiredArchetype, that.requiredArchetype)
                && Objects.equals(archetypeInfo, that.archetypeInfo)
                && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, formattedName, cost, requiredAbility, requiredArchetype, archetypeInfo, location);
    }

    public ItemStack generateItemStack() {
        //not pretty but it's needed to remove the empty component at the bottom.
        ItemStack itemStack = new ItemStack(Items.POTION) {
            @Override
            public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag isAdvanced) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(this.getHoverName());

                ItemLore lore = this.get(DataComponents.LORE);
                if (lore != null) {
                    tooltip.addAll(lore.lines());
                }

                return tooltip;
            }
        };


        float customModelData = abilityTreeNodeType.getUnlockedType().getCustomModelData().orElse(-1f);

        itemStack.set(
                DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(customModelData), List.of(), List.of(), List.of()));

        itemStack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);

        itemStack.set(DataComponents.CUSTOM_NAME, StyledText.fromString(formattedName).getComponent());

        List<Component> loreLines = new ArrayList<>();

        // Description
        for (JsonArray descLine : description) {
            loreLines.add(StyledText.fromJson(descLine).getComponent());
        }

        // Archetype
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

            loreLines.add(Component.literal(archetypeInfo.archetype() + " Archetype").withStyle(Style.EMPTY.withColor(color).withBold(true).withItalic(false)));
        }

        // Cost
        loreLines.add(Component.empty());
        loreLines.add(
                Component.literal("✔ ").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false))
                        .append(Component.literal("Ability Points: ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                                .append(Component.literal(String.valueOf(cost)).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false)))
                        )
        );

        // Required ability
        if (requiredAbility != null && !requiredAbility.isEmpty()) {
            loreLines.add(
                    Component.literal("✔ ").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false))
                            .append(Component.literal("Required Ability: ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                                    .append(Component.literal(requiredAbility).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false)))
                            )
            );
        }

        // required archetype
        if (requiredArchetype != null) {
            loreLines.add(
                    Component.literal("✔ ").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false))
                            .append(Component.literal("Min " + requiredArchetype.name() + " Archetype: ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                                    .append(Component.literal(String.valueOf(requiredArchetype.required())).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false)))
                            )
            );
        }

        // required combat level
        if (requiredLevel > 0) {
            loreLines.add(
                    Component.literal("✔ ").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false))
                            .append(Component.literal("Combat Lv. min: ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                                    .append(Component.literal(String.valueOf(requiredLevel)).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false)))
                            )
            );
        }

        itemStack.set(DataComponents.LORE, new ItemLore(loreLines));
        itemStack.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);

        return itemStack;
    }

    public AbilityTreeSkillNode withDefaultType() {
        AbilityTreeNodeType defaultType = abilityTreeNodeType.getDefaultType();
        if (defaultType == abilityTreeNodeType) return this;
        return new AbilityTreeSkillNode(
                id,
                name,
                formattedName,
                defaultType,
                description,
                cost,
                willBlock,
                blockedBy,
                requiredAbility,
                requiredArchetype,
                requiredLevel,
                archetypeInfo,
                location,
                connections);
    }

    public AbilityTreeSkillNode withUnlockedType() {
        AbilityTreeNodeType unlockedType = abilityTreeNodeType.getUnlockedType();
        if (unlockedType == abilityTreeNodeType) return this;
        return new AbilityTreeSkillNode(
                id,
                name,
                formattedName,
                unlockedType,
                description,
                cost,
                willBlock,
                blockedBy,
                requiredAbility,
                requiredArchetype,
                requiredLevel,
                archetypeInfo,
                location,
                connections);
    }

    public AbilityTreeSkillNode withoutDescriptions() {
        return new AbilityTreeSkillNode(
                id,
                name,
                formattedName,
                abilityTreeNodeType,
                List.of(),
                cost,
                willBlock,
                blockedBy,
                requiredAbility,
                requiredArchetype,
                requiredLevel,
                archetypeInfo,
                location,
                connections);
    }
}
