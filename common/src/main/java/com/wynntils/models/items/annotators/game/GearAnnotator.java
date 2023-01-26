/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.gear.profile.GearProfile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class GearAnnotator implements ItemAnnotator {
    private static final Pattern GEAR_PATTERN = Pattern.compile("^§[5abcdef](.+)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        Matcher matcher = GEAR_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        // Lookup Gear Profile
        String itemName = matcher.group(1);
        GearProfile gearProfile = Models.GearProfiles.getItemsProfile(Models.GearItem.getLookupName(itemName));
        if (gearProfile == null) return null;

        // Verify that rarity matches
        if (!name.startsWith(gearProfile.getTier().getChatFormatting().toString())) return null;

        if (Models.GearItem.isUnidentified(itemName)) {
            // FIXME
            return Models.GearItem.fromItemStack(itemStack, gearProfile);
            //            return Models.GearItem.fromUnidentified(gearProfile);
        } else {
            return Models.GearItem.fromItemStack(itemStack, gearProfile);
        }
    }
}
