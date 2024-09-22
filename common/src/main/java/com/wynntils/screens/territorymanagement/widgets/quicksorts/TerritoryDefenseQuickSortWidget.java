/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.widgets.quicksorts;

import com.wynntils.screens.territorymanagement.TerritoryManagementScreen;
import com.wynntils.services.itemfilter.statproviders.territory.TerritoryDefenseStatProvider;
import com.wynntils.services.itemfilter.type.SortInfo;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;

public class TerritoryDefenseQuickSortWidget extends TerritoryQuickSortWidget {
    public TerritoryDefenseQuickSortWidget(int x, int y, int width, int height, TerritoryManagementScreen screen) {
        super(x, y, width, height, screen);
    }

    @Override
    protected String getSortName() {
        return "Defense";
    }

    @Override
    protected CustomColor getSortColor() {
        return CommonColors.LIGHT_GRAY;
    }

    @Override
    protected List<SortInfo> getSortInfos() {
        return List.of(new SortInfo(sortDirection, new TerritoryDefenseStatProvider()));
    }
}
