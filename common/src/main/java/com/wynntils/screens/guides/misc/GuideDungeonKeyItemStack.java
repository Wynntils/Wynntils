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
        super(getItemStack(broken), new DungeonKeyItem(dungeon, corrupted), getName(dungeon, corrupted, broken));
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
                .withStyle(highlightColor)
                .append(Component.literal(getName(dungeon, corrupted, broken)));
    }

    private List<Component> generateLore() {
        List<Component> itemLore = new ArrayList<>();

        if (broken) {
            itemLore.addAll(getBrokenKeyTooltip());
        } else {
            itemLore.addAll(getUnbrokenKeyTooltip());
        }

        itemLore.addAll(getDungeonInfoTooltipSection());
        itemLore.addAll(getObtainTooltipSection());

        return itemLore;
    }

    private List<Component> getUnbrokenKeyTooltip() {
        List<Component> itemLore = new ArrayList<>();

        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage1")
                .withStyle(ChatFormatting.GRAY));
        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage2", dungeon.getName())
                .withStyle(ChatFormatting.WHITE)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage3")
                        .withStyle(ChatFormatting.GRAY)));

        return itemLore;
    }

    private List<Component> getBrokenKeyTooltip() {
        List<Component> itemLore = new ArrayList<>();

        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.broken.usage1")
                .withStyle(ChatFormatting.GRAY));
        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.broken.usage2")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.broken.usage3")
                        .withStyle(ChatFormatting.GRAY)));
        itemLore.add(Component.literal(getName(dungeon, corrupted, false)).withStyle(highlightColor));

        return itemLore;
    }

    private List<Component> getDungeonInfoTooltipSection() {
        List<Component> itemLore = new ArrayList<>();

        itemLore.add(Component.empty());
        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage4")
                .withStyle(highlightColor));

        Dungeon.DungeonData dungeonData =
                (isCorrupted() ? dungeon.getCorruptedDungeonData() : dungeon.getDungeonData()).orElseThrow();
        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage5")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(dungeonData.getCombatLevel() + "").withStyle(ChatFormatting.WHITE)));

        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage6")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(dungeonData.getXPos() + " " + dungeonData.getZPos())
                        .withStyle(ChatFormatting.WHITE)));

        itemLore.add(Component.empty());

        return itemLore;
    }

    private List<Component> getObtainTooltipSection() {
        List<Component> itemLore = new ArrayList<>();

        itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.obtain1"));

        if (broken) {
            itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.broken.obtain2")
                    .withStyle(ChatFormatting.AQUA));
        } else {
            itemLore.add(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.obtain2")
                    .withStyle(ChatFormatting.AQUA));
        }

        return itemLore;
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
