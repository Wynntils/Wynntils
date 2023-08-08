/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard.type;

import com.wynntils.core.text.StyledText;

public record ScoreboardLine(StyledText line, int score) implements Comparable<ScoreboardLine> {
    @Override
    public int compareTo(ScoreboardLine other) {
        // Negate the result because we want the highest score to be first
        return -Integer.compare(score, other.score);
    }
}
