/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.misc;

import com.wynntils.core.components.Services;
import com.wynntils.models.activities.type.Dungeon;
import com.wynntils.models.items.items.game.DungeonKeyItem;
import com.wynntils.screens.guides.GuideItemStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;

public class GuideDungeonKeyItemStack extends GuideItemStack {
    private final Dungeon dungeon;
    private final boolean corrupted;
    private List<Component> generatedTooltip;

    public GuideDungeonKeyItemStack(Dungeon dungeon, boolean corrupted) {
        super(getItemStack(corrupted), new DungeonKeyItem(dungeon, corrupted), getName(dungeon, corrupted));
        this.dungeon = dungeon;
        this.corrupted = corrupted;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public boolean isCorrupted() {
        return corrupted;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        if (generatedTooltip == null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(getHoverName());
            tooltip.add(Component.empty());
            tooltip.addAll(generateLore());

            appendFavoriteInfo(tooltip);

            generatedTooltip = tooltip;
        }

        return generatedTooltip;
    }

    @Override
    public Component getHoverName() {
        return Component.empty()
                .withStyle(Style.EMPTY.withColor(isCorrupted() ? ChatFormatting.DARK_RED : ChatFormatting.GOLD))
                .append(Component.literal(getName(dungeon, corrupted)));
    }

    private List<Component> generateLore() {
        List<Component> itemLore = new ArrayList<>();

        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage1")
                .withStyle(ChatFormatting.GRAY));
        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage2", dungeon.getName())
                .withStyle(ChatFormatting.WHITE)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage3")
                        .withStyle(ChatFormatting.GRAY)));

        itemLore.add(Component.empty());
        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage4")
                .withStyle(isCorrupted() ? ChatFormatting.DARK_RED : ChatFormatting.GOLD));

        Dungeon.DungeonData dungeonData = isCorrupted() ? dungeon.getCorruptedDungeonData() : dungeon.getDungeonData();
        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage5")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(dungeonData.getCombatLevel() + "").withStyle(ChatFormatting.WHITE)));

        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage6")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(dungeonData.getXPos() + " " + dungeonData.getYPos())
                        .withStyle(ChatFormatting.WHITE)));

        itemLore.add(Component.empty());

        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.obtain1")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.obtain2")
                        .withStyle(ChatFormatting.AQUA)));

        return itemLore;
    }

    private static ItemStack getItemStack(boolean corrupted) {
        ItemStack itemStack = new ItemStack(Items.POTION);

        float model = corrupted
                ? Services.CustomModel.getFloat("dungeon_key_broken").orElse(-1f)
                : Services.CustomModel.getFloat("dungeon_key").orElse(0f);

        CustomModelData customModelData = new CustomModelData(List.of(model), List.of(), List.of(), List.of());
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, customModelData);

        return itemStack;
    }

    private static String getName(Dungeon dungeon, boolean corrupted) {
        StringBuilder name = new StringBuilder();

        if (corrupted) name.append("Corrupted ");

        return name.append(dungeon.getName()).append(" Key").toString();
    }
}
