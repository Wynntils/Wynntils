/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.type;

import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.screens.itemfilter.widgets.ProviderFilterListWidget;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;

public record WidgetParams(
        int x,
        int y,
        int width,
        int height,
        StatProviderAndFilterPair filterPair,
        ProviderFilterListWidget parent,
        ItemFilterScreen filterScreen) {}
