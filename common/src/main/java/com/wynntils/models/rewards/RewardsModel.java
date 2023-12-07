/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeInstance;
import com.wynntils.models.wynnitem.parsing.WynnItemParseResult;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class RewardsModel extends Model {
    private final TomeInfoRegistry tomeInfoRegistry = new TomeInfoRegistry();

    public RewardsModel() {
        super(List.of());
    }

    @Override
    public void reloadData() {
        tomeInfoRegistry.reloadData();
    }

    public ItemAnnotation fromCharmItemStack(ItemStack itemStack, StyledText name, String displayName, String type) {
        GearTier tier = GearTier.fromStyledText(name);

        // TODO: replace with API lookup
        CharmInfo charmInfo = new CharmInfo(displayName, tier, type);

        WynnItemParseResult result = WynnItemParser.parseItemStack(itemStack, null);
        if (result.tier() != charmInfo.tier()) {
            WynntilsMod.warn("Tier for " + charmInfo.displayName() + " is reported as " + result.tier());
        }

        return new CharmItem(charmInfo, result.identifications(), result.rerolls());
    }

    public TomeItem fromTomeItemStack(ItemStack itemStack, StyledText name) {
        GearTier gearTier = GearTier.fromStyledText(name);

        TomeInfo tomeInfo = tomeInfoRegistry.getFromDisplayName(name.getStringWithoutFormatting());

        WynnItemParseResult result = WynnItemParser.parseItemStack(itemStack, tomeInfo.getVariableStatsMap());
        if (result.tier() != tomeInfo.tier()) {
            WynntilsMod.warn("Tier for " + tomeInfo.name() + " is reported as " + result.tier());
        }

        return new TomeItem(tomeInfo, TomeInstance.create(tomeInfo, result.identifications()), result.rerolls());
    }
}
