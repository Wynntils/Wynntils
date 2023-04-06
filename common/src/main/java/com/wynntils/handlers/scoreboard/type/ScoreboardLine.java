/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard.type;

import com.wynntils.core.text.CodedString;

public record ScoreboardLine(CodedString line, int score) implements Comparable {
    @Override
    public int compareTo(Object o) {
        if (o instanceof ScoreboardLine other) {
            // Negate the result because we want the highest score to be first
            return -Integer.compare(score, other.score);
        }

        return 0;
    }
}
