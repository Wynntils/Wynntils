/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.items.game.GearItem;
import java.util.regex.Matcher;
import net.minecraft.world.item.ItemStack;

public final class GearAnnotator implements GameItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(Models.Gear.GEAR_PATTERN);
        if (!matcher.matches()) return null;

        // Lookup Gear Profile
        String itemName = matcher.group("name");
        GearInfo gearInfo = Models.Gear.getGearInfoFromDisplayName(itemName);
        if (gearInfo == null) return null;

        // Verify that rarity matches
        // If unidentified and shiny, the rarity is in both groups
        // If unidentified, the rarity is in unidrarity
        // If identified, the rarity is in idrarity
        String unidRarity = matcher.group("unidrarity");
        if (unidRarity != null
                && !unidRarity.equals(gearInfo.tier().getChatFormatting().toString())) return null;

        String idRarity = matcher.group("idrarity");
        if (idRarity != null
                && !idRarity.equals(gearInfo.tier().getChatFormatting().toString())) return null;

        // We have no rarity information, so we can't determine if the item is gear
        if (unidRarity == null && idRarity == null) {
            WynntilsMod.warn("GearAnnotator: No rarity information found in item name: " + name);
            return null;
        }

        GearInstance gearInstance =
                matcher.group("unidentified") != null ? null : Models.Gear.parseInstance(gearInfo, itemStack);
        return new GearItem(gearInfo, gearInstance);
    }
}
