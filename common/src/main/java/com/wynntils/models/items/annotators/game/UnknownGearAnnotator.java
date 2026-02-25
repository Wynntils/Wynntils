/*
 * Copyright © Wynntils 2023-2026.
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
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

public final class UnknownGearAnnotator implements GameItemAnnotator {
    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(Models.Gear.GEAR_PATTERN);
        if (!matcher.matches()) return null;

        String frameSpriteCode = WynnItemParser.extractFrameSpriteCode(itemStack);
        GearType gearType = GearType.fromFrameSprite(frameSpriteCode);
        if (gearType == null) return null;

        String gearName = matcher.group("name");

        String rarity = matcher.group("rarity");

        GearTier gearTier = rarity != null && rarity.length() == 2
                ? GearTier.fromChatFormatting(ChatFormatting.getByCode(rarity.charAt(1)))
                : null;

        if (gearTier == null) {
            WynntilsMod.warn("UnknownGearAnnotator: No rarity information found in item name: " + name);
            return null;
        }

        boolean isUnidentified = matcher.group("unidentified") != null;

        return Models.Gear.parseUnknownGearItem(gearName, gearType, gearTier, isUnidentified, itemStack);
    }
}
