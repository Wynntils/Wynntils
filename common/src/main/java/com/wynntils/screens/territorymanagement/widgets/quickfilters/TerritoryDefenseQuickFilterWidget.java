/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.widgets.quickfilters;

import com.wynntils.models.territories.type.GuildResourceValues;
import com.wynntils.screens.territorymanagement.TerritoryManagementScreen;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.statproviders.territory.TerritoryDefenseStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TerritoryDefenseQuickFilterWidget extends TerritoryQuickFilterWidget {
    private GuildResourceValues guildResourceValues = null;

    public TerritoryDefenseQuickFilterWidget(int x, int y, int width, int height, TerritoryManagementScreen screen) {
        super(x, y, width, height, screen);
    }

    @Override
    protected void forwardClick() {
        // Cycle between null and the guild resource values
        if (guildResourceValues == null) {
            guildResourceValues = GuildResourceValues.normalValues()[0];
        } else {
            // ordinal has 1 subtracted as we are using normalValues which has NONE removed
            int index = (guildResourceValues.ordinal() - 1) + 1;
            if (index >= GuildResourceValues.normalValues().length) {
                guildResourceValues = null;
            } else {
                guildResourceValues = GuildResourceValues.normalValues()[index];
            }
        }
    }

    @Override
    protected void backwardClick() {
        // Cycle between null and the guild resource values
        if (guildResourceValues == null) {
            guildResourceValues = GuildResourceValues.normalValues()[GuildResourceValues.normalValues().length - 1];
        } else {
            // ordinal has 1 subtracted as we are using normalValues which has NONE removed
            int index = (guildResourceValues.ordinal() - 1) - 1;
            if (index < 0) {
                guildResourceValues = null;
            } else {
                guildResourceValues = GuildResourceValues.normalValues()[index];
            }
        }
    }

    @Override
    protected void resetClick() {
        guildResourceValues = null;
    }

    @Override
    protected MutableComponent getFilterName() {
        return Component.literal("Defense: " + (guildResourceValues == null ? "-" : guildResourceValues.getAsString()));
    }

    @Override
    protected CustomColor getFilterColor() {
        return guildResourceValues == null
                ? CommonColors.LIGHT_GRAY
                : CustomColor.fromChatFormatting(guildResourceValues.getDefenceColor());
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilters() {
        if (guildResourceValues == null) return List.of();

        return List.of(new StatProviderAndFilterPair(
                new TerritoryDefenseStatProvider(),
                new StringStatFilter.StringStatFilterFactory()
                        .create('"' + guildResourceValues.getAsString().replace(" ", "") + '"')
                        .orElseThrow()));
    }
}
