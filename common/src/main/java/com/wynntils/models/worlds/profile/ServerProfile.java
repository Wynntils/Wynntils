/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.profile;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ServerProfile {
    private long firstSeen;
    private final Set<String> players;

    public ServerProfile(long firstSeem, Set<String> players) {
        this.firstSeen = firstSeem;
        this.players = players;
    }

    public Set<String> getPlayers() {
        return players;
    }

    public long getFirstSeen() {
        return firstSeen;
    }

    public String getUptime() {
        long millis = System.currentTimeMillis() - firstSeen;

        return String.format(
                "%dh %dm",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
    }

    /**
     * This makes the firstSeen match the user computer time instead of the server time
     * @param serverTime the input server time
     */
    public void matchTime(long serverTime) {
        firstSeen = (System.currentTimeMillis() - serverTime) + firstSeen;
    }
}
