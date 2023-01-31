/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gearinfo.parsing.GearParseResult;
import com.wynntils.models.gearinfo.parsing.GearParser;
import com.wynntils.models.gearinfo.type.GearTier;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeType;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class RewardsModel extends Model {
    public static final Pattern CHARM_PATTERN = Pattern.compile("^§[5abcdef](Charm of the (?<Type>\\w+))$");
    public static final Pattern TOME_PATTERN = Pattern.compile(
            "^§[5abcdef]((?<Variant>[\\w']+)? ?Tome of (?<Type>\\w+)" + "(?:| Mastery (?<Tier>[IVX]{1,4})))$");

    public RewardsModel() {
        super(List.of());
    }

    public ItemAnnotation fromCharmItemStack(ItemStack itemStack, String name, Matcher matcher) {
        GearTier tier = GearTier.fromFormattedString(name);
        String type = matcher.group("Type");

        // TODO: replace with API lookup
        String displayName = matcher.group(1);
        CharmInfo charmInfo = new CharmInfo(displayName, tier, type);

        GearParseResult result = GearParser.parseItemStack(itemStack);
        if (result.tier() != charmInfo.tier()) {
            WynntilsMod.warn("Tier for " + charmInfo.displayName() + " is reported as " + result.tier());
        }

        return new CharmItem(charmInfo, result.identifications(), result.tierCount());
    }

    public static TomeItem fromTomeItemStack(ItemStack itemStack, String name, Matcher matcher) {
        Optional<TomeType> tomeTypeOpt = TomeType.fromString(matcher.group("Type"));
        if (tomeTypeOpt.isEmpty()) return null;

        TomeType tomeType = tomeTypeOpt.get();
        GearTier gearTier = GearTier.fromFormattedString(name);
        String variant = tomeType.hasVariants() ? matcher.group("Variant") : null;
        String tier = tomeType.isTiered() ? matcher.group("Tier") : null;

        // TODO: replace with API lookup
        TomeInfo tomeInfo = new TomeInfo(matcher.group(1), gearTier, variant, tomeType, tier);

        GearParseResult result = GearParser.parseItemStack(itemStack);
        if (result.tier() != tomeInfo.gearTier()) {
            WynntilsMod.warn("Tier for " + tomeInfo.displayName() + " is reported as " + result.tier());
        }

        return new TomeItem(tomeInfo, result.identifications(), result.tierCount());
    }
}
