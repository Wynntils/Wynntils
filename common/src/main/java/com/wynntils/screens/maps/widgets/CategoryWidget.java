/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.wynntils.screens.base.widgets.SideListWidget;
import com.wynntils.screens.maps.WaypointCategoryScreen;
import com.wynntils.utils.mc.McUtils;

public class CategoryWidget extends SideListWidget {
    public CategoryWidget(int y, int width, int height, String category, boolean rootCategory) {
        super(y, width, height, category, rootCategory);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (McUtils.mc().screen instanceof WaypointCategoryScreen categoryScreen) {
            if (root) {
                categoryScreen.selectPreviousCategory();
            } else {
                categoryScreen.selectCategory(name);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
