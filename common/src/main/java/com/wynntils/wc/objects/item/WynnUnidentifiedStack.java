/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.Reference;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.profiles.ItemGuessProfile;
import com.wynntils.core.webapi.profiles.item.ItemProfile;
import com.wynntils.core.webapi.profiles.item.ItemTier;
import com.wynntils.core.webapi.profiles.item.ItemType;
import com.wynntils.features.ItemGuessFeature;
import com.wynntils.mc.utils.RenderUtils;
import com.wynntils.utils.reference.EmeraldSymbols;
import com.wynntils.wc.objects.item.render.RenderedBackground;
import com.wynntils.wc.objects.item.render.RenderedHotbarBackground;
import com.wynntils.wc.utils.WynnUtils;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class WynnUnidentifiedStack extends WynnItemStack implements RenderedBackground, RenderedHotbarBackground {

    private ItemGuessProfile guessProfile;
    private ItemTier tier;

    private List<Component> tooltip;

    public WynnUnidentifiedStack(ItemStack stack) {
        super(stack);

        tooltip = super.getTooltipLines(null, TooltipFlag.Default.NORMAL);

        tier = ItemTier.fromComponent(getHoverName());
        if (tier == null) return;

        String itemType = itemName.split(" ", 2)[1];
        if (itemType == null) return;

        String levelRange = null;
        for (Component lineComp : tooltip) {
            String line = WynnUtils.normalizeBadString(lineComp.getString());
            if (line.contains("Lv. Range")) {
                levelRange = line.replace("- Lv. Range: ", "");
                break;
            }
        }

        if (levelRange == null) return;
        if (WebManager.getItemGuesses() == null || WebManager.getItemsMap() == null) return;

        guessProfile = WebManager.getItemGuesses().get(levelRange);
        if (guessProfile == null) return;

        Map<ItemTier, List<String>> rarityMap;
        try {
            rarityMap = guessProfile.getItems().get(ItemType.valueOf(itemType));
        } catch (IllegalArgumentException exception) { // itemType is invalid
            Reference.LOGGER.warn(String.format("ItemType was invalid for itemType: %s", itemType));
            return;
        }
        if (rarityMap == null) return;

        List<String> items = rarityMap.get(tier);
        if (items == null) return;

        MutableComponent guesses = new TextComponent("");
        for (String item : items) {
            MutableComponent itemDesc = new TextComponent(item).withStyle(tier.getChatFormatting());

            ItemProfile itemProfile = WebManager.getItemsMap().get(item);
            if (ItemGuessFeature.showGuessesPrice && itemProfile != null) {
                int level = itemProfile.getRequirements().getLevel();
                int itemCost = tier.getItemIdentificationCost(level);
                itemDesc.append(new TextComponent(" [")
                        .append(new TextComponent(itemCost + " " + EmeraldSymbols.E_STRING)
                                .withStyle(ChatFormatting.GREEN))
                        .append(new TextComponent("]"))
                        .withStyle(ChatFormatting.GRAY));
            }

            if (guesses.getSiblings().size() > 0)
                guesses.append(new TextComponent(", ").withStyle(ChatFormatting.GRAY));

            guesses.append(itemDesc);
        }

        tooltip.add(new TranslatableComponent("feature.wynntils.itemGuess.possibilities", guesses));
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag flag) {
        return tooltip;
    }

    @Override
    public void renderBackground(PoseStack poseStack, Slot slot) {
        int color = tier.getChatFormatting().getColor();
        color = 0xFF000000 | color;

        RenderUtils.drawTexturedRectWithColor(RenderUtils.highlight, color, slot.x - 1, slot.y - 1, 18, 18, 256, 256);
    }

    @Override
    public void renderHotbarBackground(int x, int y, ItemStack stack) {
        int color = tier.getChatFormatting().getColor();
        color = 0x80000000 | color;

        RenderUtils.drawRect(color, x, y, 16, 16);
    }
}
