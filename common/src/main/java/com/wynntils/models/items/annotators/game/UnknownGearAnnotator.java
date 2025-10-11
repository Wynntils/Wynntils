/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

public final class UnknownGearAnnotator implements GameItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(Models.Gear.GEAR_PATTERN);
        if (!matcher.matches()) return null;

        GearType gearType = GearType.fromItemStack(itemStack, false);
        if (gearType == null) return null;

        String gearName = matcher.group("name");

        String unidentifiedRarity = matcher.group("unidrarity");
        String identifiedRarity = matcher.group("idrarity");

        GearTier unidentifiedTier = unidentifiedRarity != null && unidentifiedRarity.length() == 2
                ? GearTier.fromChatFormatting(ChatFormatting.getByCode(unidentifiedRarity.charAt(1)))
                : null;
        GearTier identifiedTier = identifiedRarity != null && identifiedRarity.length() == 2
                ? GearTier.fromChatFormatting(ChatFormatting.getByCode(identifiedRarity.charAt(1)))
                : null;

        GearTier gearTier = unidentifiedTier != null ? unidentifiedTier : identifiedTier;

        if (gearTier == null) {
            WynntilsMod.warn("UnknownGearAnnotator: No rarity information found in item name: " + name);
            return null;
        }

        boolean isUnidentified = matcher.group("unidentified") != null;

        return Models.Gear.parseUnknownGearItem(gearName, gearType, gearTier, isUnidentified, itemStack);
    }
}
