/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.models.gearinfo.parsing.GearParseResult;
import com.wynntils.models.gearinfo.parsing.GearParser;
import com.wynntils.models.gearinfo.type.GearTier;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeType;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.world.item.ItemStack;

public class RewardsModel extends Model {
    public RewardsModel() {
        // FIXME: Dependency to Models.Gear???
        super(List.of());
    }

    public TomeInfo getTomeInfo(Matcher matcher, TomeType tomeType, GearTier gearTier, String variant, String tier) {
        // TODO: replace with API lookup
        TomeInfo tomeInfo = new TomeInfo(matcher.group(1), gearTier, variant, tomeType, tier);
        return tomeInfo;
    }

    public CharmInfo getCharmInfo(Matcher matcher, GearTier tier, String type) {
        // TODO: replace with API lookup
        CharmInfo charmInfo = new CharmInfo(matcher.group(1), tier, type);
        return charmInfo;
    }

    public CharmItem fromCharmItemStack(ItemStack itemStack, CharmInfo charmInfo) {
         GearParseResult result = GearParser.parseItemStack(itemStack);
        if (result.tier() != charmInfo.tier()) {
            WynntilsMod.warn("Tier for " + charmInfo.displayName() + " is reported as " + result.tier());
        }

        return new CharmItem(charmInfo, result.identifications(), result.rerolls());
    }

    public TomeItem fromTomeItemStack(ItemStack itemStack, TomeInfo tomeInfo) {
        GearParseResult result = GearParser.parseItemStack(itemStack);
        if (result.tier() != tomeInfo.gearTier()) {
            WynntilsMod.warn("Tier for " + tomeInfo.displayName() + " is reported as " + result.tier());
        }

        return new TomeItem(tomeInfo, result.identifications(), result.rerolls());
    }
}
