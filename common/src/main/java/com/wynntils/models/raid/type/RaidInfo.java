/*
 * Copyright © Wynntils 2025-2026.
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
    private int currentChallengeNum = -1;

    public RaidInfo(RaidKind raidKind) {
        this.raidKind = raidKind;

        raidStartTime = System.currentTimeMillis();
    }

    public RaidInfo(RaidKind raidKind, long raidStartTime, Map<Integer, RaidRoomInfo> challenges) {
        this.raidKind = raidKind;
        this.raidStartTime = raidStartTime;
        this.challenges.putAll(challenges);

        if (!challenges.isEmpty()) {
            currentChallengeNum = Collections.max(challenges.keySet());
        }
    }

    public void startChallenge(int challengeNum, String roomName) {
        for (int i = 1; i < challengeNum; i++) {
            RaidRoomInfo room = challenges.get(i);
            if (room != null && room.getRoomEndTime() == -1L) {
                room.markAbandoned();
            }
        }

        if (!challenges.containsKey(challengeNum)) {
            challenges.put(challengeNum, new RaidRoomInfo(roomName));
        }

        currentChallengeNum = challengeNum;
    }

    public void completeCurrentChallenge() {
        if (currentChallengeNum < 0) return;

        RaidRoomInfo room = challenges.get(currentChallengeNum);
        if (room == null) return;

        room.setRoomEndTime(System.currentTimeMillis());
    }

    public void addDamageToCurrentRoom(long damage) {
        if (currentChallengeNum < 0) return;

        RaidRoomInfo room = challenges.get(currentChallengeNum);
        if (room == null) return;

        room.addDamage(damage);
    }

    public RaidRoomInfo getCurrentRoom() {
        if (currentChallengeNum < 0) return null;

        return challenges.get(currentChallengeNum);
    }

    public int getCurrentChallengeNum() {
        return currentChallengeNum;
    }

    public RaidRoomInfo getRoomByNumber(int roomNum) {
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
