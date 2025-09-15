/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.CrafterBagItem;
import com.wynntils.models.raid.raids.RaidKind;
import com.wynntils.utils.mc.LoreUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CrafterBagAnnotator implements GameItemAnnotator {
    private static final Pattern CRAFTER_BAG_PATTERN = Pattern.compile("§.+ Crafter Bag \\[\\d+\\/\\d+\\]");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (itemStack.getItem() != Items.POTION) return null;
        Matcher matcher = name.getMatcher(CRAFTER_BAG_PATTERN);
        if (!matcher.matches()) return null;

        GearTier gearTier = GearTier.fromStyledText(name);
        if (gearTier == null) return null;

        RaidKind raidKind = Models.Raid.getRaidFromColor(LoreUtils.getLore(itemStack)
                .getFirst()
                .getFirstPart()
                .getPartStyle()
                .getColor());

        if (raidKind == null) {
            WynntilsMod.warn("Unknown raid kind for crafter bag " + name);
            return null;
        }

        return new CrafterBagItem(gearTier, raidKind);
    }
}
