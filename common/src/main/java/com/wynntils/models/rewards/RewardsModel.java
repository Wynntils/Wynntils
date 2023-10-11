/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.GameItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeType;
import com.wynntils.models.wynnitem.parsing.WynnItemParseResult;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class RewardsModel extends Model {
    public RewardsModel() {
        super(List.of());
    }

    public GameItem fromCharmItemStack(
            ItemStack itemStack, StyledText name, String displayName, String type, int emeraldPrice) {
        GearTier tier = GearTier.fromStyledText(name);

        // TODO: replace with API lookup
        CharmInfo charmInfo = new CharmInfo(displayName, tier, type);

        WynnItemParseResult result = WynnItemParser.parseItemStack(itemStack, null);
        if (result.tier() != charmInfo.tier()) {
            WynntilsMod.warn("Tier for " + charmInfo.displayName() + " is reported as " + result.tier());
        }

        return new CharmItem(emeraldPrice, charmInfo, result.identifications(), result.rerolls());
    }

    public static TomeItem fromTomeItemStack(
            ItemStack itemStack,
            StyledText name,
            String displayName,
            TomeType tomeType,
            String tier,
            String variant,
            int emeraldPrice) {
        GearTier gearTier = GearTier.fromStyledText(name);

        // TODO: replace with API lookup
        TomeInfo tomeInfo = new TomeInfo(displayName, gearTier, variant, tomeType, tier);

        WynnItemParseResult result = WynnItemParser.parseItemStack(itemStack, null);
        if (result.tier() != tomeInfo.gearTier()) {
            WynntilsMod.warn("Tier for " + tomeInfo.displayName() + " is reported as " + result.tier());
        }

        return new TomeItem(emeraldPrice, tomeInfo, result.identifications(), result.rerolls());
    }
}
