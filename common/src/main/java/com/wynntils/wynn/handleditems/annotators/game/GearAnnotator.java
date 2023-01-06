/*
 * Copyright © Wynntils 2022, 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.core.components.Managers;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
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
        ItemProfile itemProfile = Managers.ItemProfiles.getItemsProfile(Managers.GearItem.getLookupName(itemName));
        if (itemProfile == null) return null;

        // Verify that rarity matches
        if (!name.startsWith(itemProfile.getTier().getChatFormatting().toString())) return null;

        if (Managers.GearItem.isUnidentified(itemName)) {
            return Managers.GearItem.fromUnidentified(itemProfile);
        } else {
            return Managers.GearItem.fromItemStack(itemStack, itemProfile);
        }
    }
}
