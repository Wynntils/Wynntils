/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.territory;

import com.google.common.base.CaseFormat;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.type.GuildResourceValues;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class TerritoryDefenseStatProvider extends TerritoryStatProvider<String> {
    @Override
    public Optional<String> getValue(TerritoryItem territoryItem) {
        return Optional.of(territoryItem.getDefenseDifficulty().getAsString().replace(" ", ""));
    }

    @Override
    public List<String> getValidInputs() {
        return Arrays.stream(GuildResourceValues.normalValues())
                .map(GuildResourceValues::getAsString)
                .map(s -> s.replace(" ", ""))
                .toList();
    }

    @Override
    public int compare(WynnItem wynnItem1, WynnItem wynnItem2) {
        Optional<String> itemValue1 = this.getValue((TerritoryItem) wynnItem1);
        Optional<String> itemValue2 = this.getValue((TerritoryItem) wynnItem2);

        if (itemValue1.isEmpty() && itemValue2.isPresent()) return 1;
        if (itemValue1.isPresent() && itemValue2.isEmpty()) return -1;
        if (itemValue1.isEmpty() && itemValue2.isEmpty()) return 0;

        GuildResourceValues guildResource1 = GuildResourceValues.valueOf(CaseFormat.UPPER_CAMEL
                .to(CaseFormat.UPPER_UNDERSCORE, itemValue1.get())
                .toUpperCase(Locale.ROOT));
        GuildResourceValues guildResource2 = GuildResourceValues.valueOf(CaseFormat.UPPER_CAMEL
                .to(CaseFormat.UPPER_UNDERSCORE, itemValue2.get())
                .toUpperCase(Locale.ROOT));

        return -guildResource1.compareTo(guildResource2);
    }
}
