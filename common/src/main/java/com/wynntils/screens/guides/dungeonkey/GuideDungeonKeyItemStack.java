/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.dungeonkey;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.handlers.tooltip.builders.DungeonKeyTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.activities.type.Dungeon;
import com.wynntils.models.items.items.game.DungeonKeyItem;
import com.wynntils.screens.guides.GuideItemStack;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;

public class GuideDungeonKeyItemStack extends GuideItemStack {
    private final Dungeon dungeon;
    private final boolean corrupted;
    private final boolean broken;
    private final ChatFormatting highlightColor;
    private List<Component> generatedTooltip;

    public GuideDungeonKeyItemStack(Dungeon dungeon, boolean corrupted, boolean broken) {
        super(
                getItemStack(broken),
                new DungeonKeyItem(dungeon, broken, corrupted),
                getName(dungeon, corrupted, broken));
        this.dungeon = dungeon;
        this.corrupted = corrupted;
        this.broken = broken;
        this.highlightColor = calculateHighlightColor();
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public ChatFormatting getHighlightColor() {
        return highlightColor;
    }

    public boolean isCorrupted() {
        return corrupted;
    }

    public boolean isBroken() {
        return broken;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        if (generatedTooltip == null) {
            generatedTooltip = DungeonKeyTooltipBuilder.buildNewItem(
                            dungeon, corrupted, broken, ItemSourceType.GUIDE, "")
                    .getTooltipLines(Models.Character.getClassType());
        }

        return generatedTooltip;
    }

    private static ItemStack getItemStack(boolean broken) {
        ItemStack itemStack = new ItemStack(Items.POTION);

        float model = broken
                ? Services.CustomModel.getFloat("dungeon_key_broken").orElse(-1f)
                : Services.CustomModel.getFloat("dungeon_key").orElse(-1f);

        CustomModelData customModelData = new CustomModelData(List.of(model), List.of(), List.of(), List.of());
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, customModelData);

        return itemStack;
    }

    private static String getName(Dungeon dungeon, boolean corrupted, boolean broken) {
        StringBuilder name = new StringBuilder();

        if (broken) {
            name.append("Broken ");
        }

        if (corrupted) {
            name.append("Corrupted ");
        }

        return name.append(dungeon.getName()).append(" Key").toString();
    }

    private ChatFormatting calculateHighlightColor() {
        ChatFormatting highlightColor = ChatFormatting.GOLD;

        if (corrupted || broken) {
            highlightColor = ChatFormatting.DARK_RED;
        }

        return highlightColor;
    }
}
