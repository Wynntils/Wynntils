/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.highlights;

import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.type.TerritoryConnectionType;
import com.wynntils.screens.territorymanagement.TerritoryManagementHolder;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.Map;
import java.util.Optional;

public class TerritoryTypeHighlighter implements TerritoryHighlighter {
    public static final CustomColor HEADQUARTERS_BORDER_COLOR = CommonColors.ORANGE;
    public static final CustomColor HEADQUARTERS_CONNECTION_BORDER_COLOR = CommonColors.YELLOW;
    public static final CustomColor NO_ROUTE_BORDER_COLOR = CommonColors.RED;

    private final TerritoryManagementHolder territoryManagementHolder;

    public TerritoryTypeHighlighter(TerritoryManagementHolder territoryManagementHolder) {
        this.territoryManagementHolder = territoryManagementHolder;
    }

    @Override
    public Optional<CustomColor> getBorderColor(TerritoryItem territoryItem) {
        Map<TerritoryItem, TerritoryConnectionType> territoryConnections =
                territoryManagementHolder.territoryConnections();

        TerritoryConnectionType territoryConnectionType = territoryConnections.get(territoryItem);

        if (territoryConnectionType == null) return Optional.empty();

        return switch (territoryConnectionType) {
            case HEADQUARTERS -> Optional.of(HEADQUARTERS_BORDER_COLOR);
            case HEADQUARTERS_CONNECTION -> Optional.of(HEADQUARTERS_CONNECTION_BORDER_COLOR);
            case UNCONNECTED -> Optional.of(NO_ROUTE_BORDER_COLOR);
            default -> Optional.empty();
        };
    }
}
