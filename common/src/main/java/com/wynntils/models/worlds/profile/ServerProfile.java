/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.profile;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ServerProfile {
    private final String serverName;
    private final Set<String> players;

    private long firstSeen;

    public ServerProfile(String serverName, Set<String> players, long firstSeem) {
        this.serverName = serverName;
        this.firstSeen = firstSeem;
        this.players = players;
    }

    public String getServerName() {
        return serverName;
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

    public int getUptimeInMinutes() {
        return (int) TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - firstSeen);
    }

    /**
     * This makes the firstSeen match the user computer time instead of the server time
     * @param serverTime the input server time
     */
    public void matchTime(long serverTime) {
        firstSeen = (System.currentTimeMillis() - serverTime) + firstSeen;
    }
}
