/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item;

import com.wynntils.core.components.Managers;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.model.item.game.GearIdentification;
import com.wynntils.model.item.game.GearItem;
import com.wynntils.wynn.objects.Powder;
import com.wynntils.wynn.objects.SpellType;
import com.wynntils.wynn.objects.profiles.item.IdentificationProfile;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class GearAnnotator implements ItemAnnotator {
    private static final Pattern ITEM_TIER =
            Pattern.compile("(?<Quality>Normal|Unique|Rare|Legendary|Fabled|Mythic|Set) Item(?: \\[(?<Rolls>\\d+)])?");
    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9]+))");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        ItemProfile itemProfile;
        List<GearIdentification> identifications = new ArrayList<>();
        List<Powder> powders = List.of();
        int rerolls = 0;
        List<Component> setBonus = new ArrayList<>();

        // Lookup Gear Profile
        String name = itemStack.getHoverName().getString();
        String strippedName = WynnUtils.normalizeBadString(ComponentUtils.stripFormatting(name));
        itemProfile = Managers.ItemProfiles.getItemsProfile(strippedName);
        if (itemProfile == null) return null;

        // Verify that rarity matches
        if (!name.startsWith(itemProfile.getTier().getChatFormatting().toString())) return null;

        // Parse lore for identifications, powders and rerolls
        List<Component> lore = ComponentUtils.stripDuplicateBlank(itemStack.getTooltipLines(null, TooltipFlag.NORMAL));
        lore.remove(0); // remove item name

        boolean collectingSetBonus = false;
        for (Component loreLine : lore) {
            String unformattedLoreLine = WynnUtils.normalizeBadString(loreLine.getString());

            // Look for Set Bonus
            if (unformattedLoreLine.equals("Set Bonus:")) {
                collectingSetBonus = true;
                continue;
            }
            if (collectingSetBonus) {
                setBonus.add(loreLine);

                if (unformattedLoreLine.isBlank()) {
                    collectingSetBonus = false;
                }
                continue;
            }

            // Look for Powder
            if (unformattedLoreLine.contains("] Powder Slots")) {
                powders = Powder.findPowders(unformattedLoreLine);
                continue;
            }

            // Look for Rerolls
            Matcher rerollMatcher = ITEM_TIER.matcher(unformattedLoreLine);
            if (rerollMatcher.find()) {
                if (rerollMatcher.group("Rolls") == null) continue;
                rerolls = Integer.parseInt(rerollMatcher.group("Rolls"));
                continue;
            }

            // Look for identifications
            Matcher identificationMatcher = ITEM_IDENTIFICATION_PATTERN.matcher(unformattedLoreLine);
            if (identificationMatcher.find()) {
                String idName =
                        getShortName(identificationMatcher.group("ID"), identificationMatcher.group("Suffix") == null);
                int value = Integer.parseInt(identificationMatcher.group("Value"));
                int stars = identificationMatcher.group("Stars").length();
                identifications.add(new GearIdentification(idName, value, stars));
            }
        }

        return new GearItem(itemProfile, identifications, powders, rerolls, setBonus);
    }

    private String getShortName(String fullIdName, boolean isRaw) {
        SpellType spell = SpellType.fromName(fullIdName);
        if (spell != null) {
            return spell.getShortIdName(isRaw);
        }

        return IdentificationProfile.getAsShortName(fullIdName, isRaw);
    }
}
