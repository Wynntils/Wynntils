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
import com.wynntils.utils.MathUtils;
import com.wynntils.wc.objects.items.IdentificationContainer;
import com.wynntils.wc.objects.items.ItemProfile;
import com.wynntils.wc.utils.WynnUtils;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

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
            String unformattedLoreLine =
                    WynnUtils.normalizeBadString(ComponentUtils.getUnformatted(lore.getString(i)));

            if (unformattedLoreLine.equals("Set Bonus:")) {
                endOfStatuses = true;
            }

            if (!endOfStatuses) {
                // FIXME: Check if CTRL or SHIFT is pressed and replace lore accordingly
                StringTag newTag = generateNewTag(profile, unformattedLoreLine);

                if (newTag != null) {
                    newLore.add(newTag);
                    continue;
                }
            }

            newLore.add(lore.get(i));
        }

        ItemUtils.replaceLore(itemStack, newLore);
    }

    @Nullable
    private StringTag generateNewTag(ItemProfile profile, String unformattedLoreLine) {
        Matcher statusMatcher = ITEM_STATUS_PATTERN.matcher(unformattedLoreLine);

        if (!statusMatcher.matches()) {
            return null;
        }

        MutableComponent newLoreLine = Component.Serializer.fromJson(unformattedLoreLine);

        if (newLoreLine == null) {
            return null;
        }

        int statValue = Integer.parseInt(statusMatcher.group(1) + statusMatcher.group(2));
        String statusName = statusMatcher.group(4);

        // FIXME: Fix "Spell Name Cost" (For example: Totem Cost is recognized as NEW)
        if (!profile.getLongNameStatusMap().containsKey(statusName)) {
            newLoreLine.append(new TextComponent(" [NEW]").withStyle(ChatFormatting.GOLD));
            return ItemUtils.toLoreForm(newLoreLine);
        }

        IdentificationContainer idContainer = profile.getLongNameStatusMap().get(statusName);

        if (idContainer.hasConstantValue()) {
            return null; // unchanged
        }

        if (!idContainer.isValidValue(statValue)) {
            newLoreLine.append(new TextComponent(" [NEW]").withStyle(ChatFormatting.GOLD));
            return ItemUtils.toLoreForm(newLoreLine);
        }

        float percentage =
                MathUtils.inverselerp(idContainer.getMin(), idContainer.getMax(), statValue) * 100;
        Style color = Style.EMPTY.withColor(getPercentageColor(percentage));

        newLoreLine.append(
                new TextComponent(String.format("[%.1f%%]", percentage)).withStyle(color));
        return ItemUtils.toLoreForm(newLoreLine);
    }

    private static final TreeMap<Float, TextColor> colorMap =
            new TreeMap<>() {
                {
                    put(15f, TextColor.fromLegacyFormat(ChatFormatting.RED));
                    put(55f, TextColor.fromLegacyFormat(ChatFormatting.YELLOW));
                    put(88f, TextColor.fromLegacyFormat(ChatFormatting.GRAY));
                    put(98f, TextColor.fromLegacyFormat(ChatFormatting.AQUA));
                }
            };

    private TextColor getPercentageColor(float percentage) {
        Map.Entry<Float, TextColor> lowerEntry = colorMap.floorEntry(percentage);
        Map.Entry<Float, TextColor> higherEntry = colorMap.ceilingEntry(percentage);

        // Boundary conditions
        if (lowerEntry == null) {
            return higherEntry.getValue();
        } else if (higherEntry == null) {
            return lowerEntry.getValue();
        }

        if (Objects.equals(lowerEntry.getValue(), higherEntry.getValue())) {
            return lowerEntry.getValue();
        }

        float t = MathUtils.inverselerp(lowerEntry.getKey(), higherEntry.getKey(), percentage);

        int lowerColor = lowerEntry.getValue().getValue();
        int higherColor = lowerEntry.getValue().getValue();

        int r = (int) MathUtils.lerp((lowerColor >> 16) & 0xff, (higherColor >> 16) & 0xff, t);
        int g = (int) MathUtils.lerp((lowerColor >> 8) & 0xff, (higherColor >> 8) & 0xff, t);
        int b = (int) MathUtils.lerp(lowerColor & 0xff, higherColor & 0xff, t);

        return TextColor.fromRgb((r << 16) | (g << 8) | b);
    }
}
