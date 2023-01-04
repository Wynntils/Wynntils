/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.game;

import com.wynntils.core.components.Managers;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.item.GearItemStack;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraft.world.item.ItemStack;

public final class GearAnnotator implements ItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        // Lookup Gear Profile

        // FIXME: Temporary workaround awaiting full merge
        if (!(itemStack instanceof GearItemStack gearItemStack)) return null;
        String strippedName = WynnUtils.normalizeBadString(ComponentUtils.stripFormatting(
                gearItemStack.getOriginalHoverName().getString()));

        ItemProfile itemProfile = Managers.ItemProfiles.getItemsProfile(strippedName);
        if (itemProfile == null) return null;

        // Verify that rarity matches
        if (!name.startsWith(itemProfile.getTier().getChatFormatting().toString())) return null;

        return Managers.GearItem.fromItemStack(itemStack, itemProfile);
    }
}
