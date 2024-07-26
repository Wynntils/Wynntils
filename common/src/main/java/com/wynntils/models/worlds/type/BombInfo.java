/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.type;

import com.wynntils.core.components.Models;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;

public record BombInfo(String user, BombType bomb, String server, long startTime, float length) {
    // mm:ss format
    public String getRemainingString() {
        long millis = getRemainingLong();
        return String.format(
                "%02dm %02ds",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public long getRemainingLong() {
        return startTime + getLength() - System.currentTimeMillis();
    }

    public String asString() {
        return ChatFormatting.GOLD + bomb.getName()
                + ChatFormatting.GRAY
                + " on "
                + (server.equals(Models.WorldState.getCurrentWorldName()) ? ChatFormatting.GREEN : ChatFormatting.WHITE)
                + server
                + ChatFormatting.GOLD
                + " ("
                + getRemainingString()
                + ")";
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
