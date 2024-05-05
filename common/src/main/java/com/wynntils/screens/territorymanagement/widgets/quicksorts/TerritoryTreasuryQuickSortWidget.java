/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.widgets.quicksorts;

import com.wynntils.screens.territorymanagement.TerritoryManagementScreen;
import com.wynntils.services.itemfilter.statproviders.territory.TerritoryTreasuryStatProvider;
import com.wynntils.services.itemfilter.type.SortInfo;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;

public class TerritoryTreasuryQuickSortWidget extends TerritoryQuickSortWidget {
    public TerritoryTreasuryQuickSortWidget(int x, int y, int width, int height, TerritoryManagementScreen screen) {
        super(x, y, width, height, screen);
    }

    @Override
    protected String getSortName() {
        return "Treasury";
    }

    @Override
    protected CustomColor getSortColor() {
        return CommonColors.WHITE;
    }

    @Override
    protected List<SortInfo> getSortInfos() {
        return List.of(new SortInfo(sortDirection, new TerritoryTreasuryStatProvider()));
    }
}
