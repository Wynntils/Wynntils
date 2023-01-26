/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.TomeProfile;
import com.wynntils.models.gear.type.TomeType;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class TomeAnnotator implements ItemAnnotator {
    private static final Pattern TOME_PATTERN = Pattern.compile(
            "^§[5abcdef]((?<Variant>[\\w']+)? ?Tome of (?<Type>\\w+)" + "(?:| Mastery (?<Tier>[IVX]{1,4})))$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        if (!(itemStack.getItem() == Items.ENCHANTED_BOOK)) return null;
        Matcher matcher = TOME_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        Optional<TomeType> tomeTypeOpt = TomeType.fromString(matcher.group("Type"));
        if (tomeTypeOpt.isEmpty()) return null;

        TomeType tomeType = tomeTypeOpt.get();
        GearTier gearTier = GearTier.fromFormattedString(name);
        String variant = tomeType.hasVariants() ? matcher.group("Variant") : null;
        String tier = tomeType.isTiered() ? matcher.group("Tier") : null;

        // TODO: replace with API lookup
        TomeProfile tomeProfile = new TomeProfile(matcher.group(1), gearTier, variant, tomeType, tier);

        return Models.GearItem.fromTomeItemStack(itemStack, tomeProfile);
    }
}
