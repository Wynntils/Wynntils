/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type.wynnplayer;

public record GlobalData(
        int contentCompletion,
        int wars,
        int totalLevel,
        long mobsKilled,
        int chestsFound,
        ContentCompletedData dungeons,
        ContentCompletedData raids,
        int worldEvents,
        int lootruns,
        int caves,
        int completedQuests,
        PvpData pvpData) {}
