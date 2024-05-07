/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.widgets.quickfilters;

import com.wynntils.screens.territorymanagement.TerritoryManagementScreen;

public abstract class TerritoryToggleQuickFilterWidget extends TerritoryQuickFilterWidget {
    protected Boolean filterToggle = null;

    protected TerritoryToggleQuickFilterWidget(int x, int y, int width, int height, TerritoryManagementScreen screen) {
        super(x, y, width, height, screen);
    }

    @Override
    protected void forwardClick() {
        if (filterToggle == null) {
            filterToggle = true;
        } else if (filterToggle) {
            filterToggle = false;
        } else {
            filterToggle = null;
        }
    }

    @Override
    protected void backwardClick() {
        if (filterToggle == null) {
            filterToggle = false;
        } else if (filterToggle) {
            filterToggle = null;
        } else {
            filterToggle = true;
        }
    }

    @Override
    protected void resetClick() {
        filterToggle = null;
    }
}
