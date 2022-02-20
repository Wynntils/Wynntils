/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.Feature;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wc.objects.items.IdentificationContainer;
import com.wynntils.wc.objects.items.ItemProfile;
import com.wynntils.wc.utils.WynnUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemStatPercentageInfoFeature extends Feature {
    private static final Pattern ITEM_STATUS_PATTERN =
            Pattern.compile("([+\\-])(\\d+)(%|\\stier|/3s|/4s|/5s|)\\*{0,3}\\s(.+)");

    @Override
    protected void init(ImmutableList.Builder<Condition> conditions) {
        conditions.add(new WebLoadedCondition());
    }

    @Override
    protected boolean onEnable() {
        WynntilsMod.getEventBus().register(this);
        return WebManager.tryLoadItemList();
    }

    @Override
    protected void onDisable() {}

    @SubscribeEvent
    public void onItemToolTipRender(ItemTooltipRenderEvent e) {
        ItemStack itemStack = e.getItemStack();

        replaceLore(itemStack);
    }

    private void replaceLore(ItemStack itemStack) {
        // FIXME: Check if CTRL or SHIFT is pressed and replace lore accordingly
        replaceLorePercentage(itemStack);
    }

    private void replaceLorePercentage(ItemStack itemStack) {
        String itemName =
                WynnUtils.normalizeBadString(
                        ChatFormatting.stripFormatting(itemStack.getHoverName().getString()));

        if (!WebManager.getItemsMap().containsKey(itemName)) return;

        ItemProfile profile = WebManager.getItemsMap().get(itemName);

        ListTag lore = ItemUtils.getLoreTagElseEmpty(itemStack);
        ListTag newLore = new ListTag();

        // Used to not give set bonuses a different lore
        boolean endOfStatuses = false;

        for (int i = 0; i < lore.size(); i++) {
            String formattedLoreLine =
                    WynnUtils.normalizeBadString(ComponentUtils.getFormatted(lore.getString(i)));
            String unformattedLoreLine =
                    WynnUtils.normalizeBadString(ComponentUtils.getUnformatted(lore.getString(i)));

            if (formattedLoreLine.equals(ChatFormatting.GREEN + "Set Bonus:")) {
                endOfStatuses = true;
                newLore.add(lore.get(i));
                continue;
            }

            Matcher statusMatcher = ITEM_STATUS_PATTERN.matcher(unformattedLoreLine);

            if (statusMatcher.matches() && !endOfStatuses) {
                String newLoreLine = formattedLoreLine;
                int statValue = Integer.parseInt(statusMatcher.group(1) + statusMatcher.group(2));
                String statusName = statusMatcher.group(4);

                // FIXME: Fix "Spell Name Cost" (For example: Totem Cost is recognized as NEW)
                if (!profile.getLongNameStatusMap().containsKey(statusName)) {
                    newLoreLine += " " + ChatFormatting.GOLD + "[NEW]";
                    newLore.add(StringTag.valueOf(ItemUtils.toLoreForm(newLoreLine)));
                    continue;
                }

                IdentificationContainer idContainer =
                        profile.getLongNameStatusMap().get(statusName);

                if (idContainer.hasConstantValue()) {
                    newLore.add(StringTag.valueOf(ItemUtils.toLoreForm(newLoreLine)));
                    continue;
                }

                if (!idContainer.isValidValue(statValue)) {
                    newLoreLine += " " + ChatFormatting.GOLD + "[NEW]";
                    newLore.add(StringTag.valueOf(ItemUtils.toLoreForm(newLoreLine)));
                    continue;
                }

                float percentage =
                        mapStatInRange(statValue, idContainer.getMin(), idContainer.getMax()) * 100;
                ChatFormatting color = getPercentageColor(percentage);

                newLoreLine += " " + color + "[" + String.format("%.1f", percentage) + "%]";
                newLore.add(StringTag.valueOf(ItemUtils.toLoreForm(newLoreLine)));
                continue;
            }

            newLore.add(lore.get(i));
        }

        ItemUtils.replaceLore(itemStack, newLore);
    }

    private ChatFormatting getPercentageColor(float percentage) {
        if (percentage < 30f) {
            return ChatFormatting.RED;
        } else if (percentage < 80f) {
            return ChatFormatting.YELLOW;
        } else if (percentage < 96f) {
            return ChatFormatting.GREEN;
        } else {
            return ChatFormatting.AQUA;
        }
    }

    // Maps stat into a 0 to 1 range
    private float mapStatInRange(float stat, float minRoll, float maxRoll) {
        return ((stat - minRoll) / (maxRoll - minRoll));
    }
}
