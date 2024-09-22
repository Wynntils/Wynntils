/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.highlights;

import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import java.util.Optional;

public interface TerritoryHighlighter {
    default List<CustomColor> getBackgroundColors(TerritoryItem territoryItem) {
        return List.of();
    }

    default Optional<CustomColor> getBorderColor(TerritoryItem territoryItem) {
        return Optional.empty();
    }
}
