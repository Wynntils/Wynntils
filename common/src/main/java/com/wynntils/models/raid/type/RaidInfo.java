/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.type;

import com.wynntils.models.raid.raids.RaidKind;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class RaidInfo {
    private final RaidKind raidKind;
    private final Map<Integer, RaidRoomInfo> challenges = new TreeMap<>();
    private final long raidStartTime;

    public RaidInfo(RaidKind raidKind) {
        this.raidKind = raidKind;

        raidStartTime = System.currentTimeMillis();
    }

    public void startChallenge(int challengeNum, String roomName) {
        if (challenges.containsKey(challengeNum)) return;

        challenges.put(challengeNum, new RaidRoomInfo(roomName));
    }

    public void completeCurrentChallenge() {
        if (!challenges.containsKey(challenges.size())) return;

        challenges.get(challenges.size()).setRoomEndTime(System.currentTimeMillis());
    }

    public void addDamageToCurrentRoom(long damage) {
        if (!challenges.containsKey(challenges.size())) return;

        challenges.get(challenges.size()).addDamage(damage);
    }

    public RaidRoomInfo getCurrentRoom() {
        return challenges.getOrDefault(challenges.size(), null);
    }

    public RaidRoomInfo getRoomByNumber(int roomNum) {
        if (!challenges.containsKey(roomNum)) return null;

        return challenges.get(roomNum);
    }

    public long getRaidStartTime() {
        return raidStartTime;
    }

    public long getTimeInRaid() {
        return System.currentTimeMillis() - raidStartTime;
    }

    public long getIntermissionTime() {
        return getTimeInRaid() - getTimeInRooms();
    }

    public long getRaidDamage() {
        return challenges.values().stream()
                .mapToLong(RaidRoomInfo::getRoomDamage)
                .sum();
    }

    public RaidKind getRaidKind() {
        return raidKind;
    }

    public int completedChallengeCount() {
        return challenges.size();
    }

    public Map<Integer, RaidRoomInfo> getChallenges() {
        return Collections.unmodifiableMap(challenges);
    }

    private long getTimeInRooms() {
        return challenges.values().stream()
                .mapToLong(RaidRoomInfo::getRoomTotalTime)
                .sum();
    }
}
