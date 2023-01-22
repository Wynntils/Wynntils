/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Managers;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.concepts.Powder;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.wynn.WynnItemMatchers;
import com.wynntils.utils.wynn.WynnUtils;
import com.wynntils.wynn.gear.types.GearIdentification;
import com.wynntils.wynn.gear.types.GearStat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class CraftedGearAnnotator implements ItemAnnotator {
    private static final Pattern CRAFTED_GEAR_PATTERN = Pattern.compile("^§3(.*)§b \\[\\d{1,3}%\\]$");
    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9]+))");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        Matcher matcher = CRAFTED_GEAR_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        CappedValue durability = WynnItemMatchers.getDurability(itemStack);

        List<GearIdentification> identifications = new ArrayList<>();
        List<Powder> powders = List.of();

        // Parse lore for identifications and powders
        List<Component> lore = ComponentUtils.stripDuplicateBlank(itemStack.getTooltipLines(null, TooltipFlag.NORMAL));
        if (lore.size() <= 1) {
            // We should always have the item name as the first line, unless some other mod interacts badly...
            return null;
        }
        lore.remove(0); // remove item name

        for (Component loreLine : lore) {
            // FIXME: This is partially shared with GearAnnotator
            String unformattedLoreLine = WynnUtils.normalizeBadString(loreLine.getString());

            // Look for Powder
            if (unformattedLoreLine.contains("] Powder Slots")) {
                powders = Powder.findPowders(unformattedLoreLine);
                continue;
            }

            // Look for identifications
            String formatId = ComponentUtils.getCoded(loreLine);
            Matcher statMatcher = Managers.GearItem.ID_NEW_PATTERN.matcher(formatId);
            if (statMatcher.matches()) {
                int value = Integer.parseInt(statMatcher.group(2));
                String unit = statMatcher.group(3);
                String statDisplayName = statMatcher.group(5);

                GearStat type = Managers.GearInfo.getGearStat(statDisplayName, unit);
                if (type == null && Skill.isSkill(statDisplayName)) {
                    // Skill point buff looks like stats when parsing
                    continue;
                }

                identifications.add(new GearIdentification(type, value));
            }
        }

        // FIXME: Missing requirements and damages
        return new CraftedGearItem(List.of(), List.of(), identifications, powders, durability);
    }
}
