/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard.type;

import net.minecraft.server.ServerScoreboard;

public record ScoreboardLineChange(String lineText, ServerScoreboard.Method method, int lineIndex) {}
