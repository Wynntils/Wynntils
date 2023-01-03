/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.core.components.Managers;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import net.minecraft.world.item.ItemStack;

public final class GearAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        // Lookup Gear Profile
        ItemProfile itemProfile = Managers.ItemProfiles.getItemsProfile(name);
        if (itemProfile == null) return null;

        // Verify that rarity matches
        if (!name.startsWith(itemProfile.getTier().getChatFormatting().toString())) return null;

        return Managers.GearItem.fromItemStack(itemStack, itemProfile);
    }
}
