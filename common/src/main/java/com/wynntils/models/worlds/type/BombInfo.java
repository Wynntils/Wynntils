/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.type;

import java.util.concurrent.TimeUnit;

public record BombInfo(String user, BombType bomb, String server, long startTime, float length) {
    // mm:ss format
    public String getRemainingString() {
        long millis = startTime + getLength() - System.currentTimeMillis();
        return String.format(
                "%02dm %02ds",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BombInfo bombInfo)) return false;

        // match user, bomb type, and server, ignoring time
        return user.equals(bombInfo.user()) && bomb == bombInfo.bomb() && server.equals(bombInfo.server());
    }

    public boolean isActive() {
        return System.currentTimeMillis() < startTime + getLength();
    }

    public long getLength() {
        return (long) (length * 60000L);
    }
}
