/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.war.type;

public record HistoricWarInfo(
        String territory, String ownerGuild, WarTowerState initialTower, WarTowerState endTower, long endedTimestamp) {}
