/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type.wynnplayer;

import java.util.Map;

public record ContentCompletedData(int total, Map<String, Integer> completions) {}
