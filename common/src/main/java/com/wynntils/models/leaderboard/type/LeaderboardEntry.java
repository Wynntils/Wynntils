/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.leaderboard.type;

import java.util.Map;

public record LeaderboardEntry(String name, int timePlayed, Map<String, Integer> ranks) {}
