/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.type;

public class RaidRoomInfo {
    private final String roomName;
    private final long roomStartTime;

    private long roomDamage = 0L;
    private long roomEndTime = -1L;
    private long roomTotalTime = -1L;

    public RaidRoomInfo(String roomName) {
        this.roomName = roomName;
        this.roomStartTime = System.currentTimeMillis();
    }

    public RaidRoomInfo(String roomName, long roomStartTime, long roomEndTime, long roomDamage) {
        this.roomName = roomName;
        this.roomStartTime = roomStartTime;
        this.roomDamage = roomDamage;

        if (roomEndTime != -1L) {
            setRoomEndTime(roomEndTime);
        }
    }

    public String getRoomName() {
        return roomName;
    }

    public long getRoomStartTime() {
        return roomStartTime;
    }

    public long getRoomEndTime() {
        return roomEndTime;
    }

    public void setRoomEndTime(long roomEndTime) {
        this.roomEndTime = roomEndTime;

        setRoomTotalTime();
    }

    public void markAbandoned() {
        if (roomEndTime != -1L) return;

        roomEndTime = roomStartTime;
        roomTotalTime = 0L;
    }

    public long getRoomTotalTime() {
        if (roomTotalTime != -1L) return roomTotalTime;

        return System.currentTimeMillis() - roomStartTime;
    }

    public void addDamage(long newDamage) {
        roomDamage += newDamage;
    }

    public long getRoomDamage() {
        return roomDamage;
    }

    private void setRoomTotalTime() {
        roomTotalTime = roomEndTime - roomStartTime;
    }
}
