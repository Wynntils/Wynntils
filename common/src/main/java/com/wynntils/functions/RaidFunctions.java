/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.gambits.type.Gambit;
import com.wynntils.models.raid.type.RaidInfo;
import com.wynntils.models.raid.type.RaidRoomInfo;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Time;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class RaidFunctions {

    @TemplateFunction(name = "current_raid", aliases = { "raid" })
    public String currentRaidFunction() {
        RaidInfo raidInfo = Models.Raid.getCurrentRaid();
        if (raidInfo == null)
            return "";
        return raidInfo.getRaidKind().getRaidName();
    }

    @TemplateFunction(name = "current_raid_room_name")
    public String currentRaidRoomNameFunction() {
        return Models.Raid.getCurrentRoomName();
    }

    @TemplateFunction(name = "current_raid_start", aliases = { "raid_start" })
    public Time currentRaidStartFunction() {
        RaidInfo currentRaid = Models.Raid.getCurrentRaid();
        if (currentRaid == null)
            return Time.NONE;
        return Time.of(currentRaid.getRaidStartTime());
    }

    @TemplateFunction(name = "current_raid_time", aliases = { "raid_time" })
    public long currentRaidTimeFunction() {
        if (Models.Raid.getCurrentRaid() == null)
            return -1L;
        return Models.Raid.currentRaidTime();
    }

    @TemplateFunction(name = "current_raid_damage", aliases = { "raid_damage" })
    public long currentRaidDamageFunction() {
        if (Models.Raid.getCurrentRaid() == null)
            return -1L;
        return Models.Raid.getRaidDamage();
    }

    @TemplateFunction(name = "current_raid_room_start")
    public Time currentRaidRoomStartFunction() {
        RaidInfo currentRaid = Models.Raid.getCurrentRaid();
        if (currentRaid == null)
            return Time.NONE;
        RaidRoomInfo currentRoom = currentRaid.getCurrentRoom();
        if (currentRoom == null)
            return Time.NONE;
        return Time.of(currentRoom.getRoomStartTime());
    }

    @TemplateFunction(name = "current_raid_room_time")
    public long currentRaidRoomTimeFunction() {
        if (Models.Raid.getCurrentRaid() == null)
            return -1L;
        return Models.Raid.currentRoomTime();
    }

    @TemplateFunction(name = "current_raid_room_damage")
    public long currentRaidRoomDamageFunction() {
        if (Models.Raid.getCurrentRaid() == null)
            return -1L;
        return Models.Raid.getCurrentRoomDamage();
    }

    @TemplateFunction(name = "current_raid_challenge_count")
    public int currentRaidChallengeCountFunction() {
        if (Models.Raid.getCurrentRaid() == null)
            return -1;
        return Models.Raid.getRaidChallengeCount();
    }

    @TemplateFunction(name = "current_raid_boss_count")
    public int currentRaidBossCountFunction() {
        if (Models.Raid.getCurrentRaid() == null)
            return -1;
        return Models.Raid.getRaidBossCount();
    }

    @TemplateFunction(name = "raid_challenges")
    public CappedValue raidChallengesFunction() {
        return Models.Raid.getChallenges();
    }

    @TemplateFunction(name = "raid_intermission_time")
    public long raidIntermissionTimeFunction() {
        if (Models.Raid.getCurrentRaid() == null)
            return -1L;
        return Models.Raid.getIntermissionTime();
    }

    @TemplateFunction(name = "raid_room_name")
    public String raidRoomNameFunction(int roomNumber) {
        if (Models.Raid.getCurrentRaid() == null)
            return "";
        int roomNum = roomNumber;
        return Models.Raid.getRoomName(roomNum);
    }

    @TemplateFunction(name = "raid_room_start")
    public Time raidRoomStartFunction(int roomNumber) {
        RaidInfo currentRaid = Models.Raid.getCurrentRaid();
        if (currentRaid == null)
            return Time.NONE;
        int roomNum = roomNumber;
        RaidRoomInfo room = currentRaid.getRoomByNumber(roomNum);
        if (room == null)
            return Time.NONE;
        return Time.of(room.getRoomStartTime());
    }

    @TemplateFunction(name = "raid_room_time")
    public long raidRoomTimeFunction(int roomNumber) {
        if (Models.Raid.getCurrentRaid() == null)
            return -1L;
        int roomNum = roomNumber;
        return Models.Raid.getRoomTime(roomNum);
    }

    @TemplateFunction(name = "raid_room_damage")
    public long raidRoomDamageFunction(int roomNumber) {
        if (Models.Raid.getCurrentRaid() == null)
            return -1L;
        int roomNum = roomNumber;
        return Models.Raid.getRoomDamage(roomNum);
    }

    @TemplateFunction(name = "raid_has_room")
    public boolean raidHasRoomFunction(int roomNumber) {
        if (Models.Raid.getCurrentRaid() == null)
            return false;
        int roomNum = roomNumber;
        return Models.Raid.raidHasRoom(roomNum);
    }

    @TemplateFunction(name = "raid_is_boss_room")
    public boolean raidIsBossRoomFunction(int roomNumber) {
        if (Models.Raid.getCurrentRaid() == null)
            return false;
        int roomNum = roomNumber;
        return Models.Raid.isBossRoom(roomNum);
    }

    @TemplateFunction(name = "raid_personal_best_time", aliases = { "raid_pb" })
    public long raidPersonalBestTimeFunction(String raidName) {
        return Models.Raid.getRaidBestTime(raidName);
    }

    @TemplateFunction(name = "raid_time_remaining")
    public int raidTimeRemainingFunction() {
        return Models.Raid.getTimeLeft();
    }

    @TemplateFunction(name = "dry_aspects")
    public int dryAspectsFunction() {
        return Models.Raid.getAspectPullsWithoutMythicAspect();
    }

    @TemplateFunction(name = "dry_raids_aspects")
    public int dryRaidsAspectsFunction() {
        return Models.Raid.getRaidsWithoutMythicAspect();
    }

    @TemplateFunction(name = "dry_raid_reward_pulls")
    public int dryRaidRewardPullsFunction() {
        return Models.Raid.getRewardPullsWithoutMythicTome();
    }

    @TemplateFunction(name = "dry_raids_tomes")
    public int dryRaidsTomesFunction() {
        return Models.Raid.getRaidsWithoutMythicTome();
    }

    @TemplateFunction(name = "raids_runs_since")
    public int raidsRunsSinceFunction(int sinceDays) {
        return Math.toIntExact(Models.Raid.historicRaids.get().stream().filter(historicRaidInfo -> historicRaidInfo.endedTimestamp() >= System.currentTimeMillis() - TimeUnit.DAYS.toMillis(sinceDays)).count());
    }

    @TemplateFunction(name = "specific_raid_runs_since")
    public int specificRaidRunsSinceFunction(String raidName, int sinceDays) {
        return Math.toIntExact(Models.Raid.historicRaids.get().stream().filter(historicRaidInfo -> (historicRaidInfo.name().equalsIgnoreCase(raidName) || historicRaidInfo.abbreviation().equalsIgnoreCase(raidName)) && historicRaidInfo.endedTimestamp() >= System.currentTimeMillis() - TimeUnit.DAYS.toMillis(sinceDays)).count());
    }

    @TemplateFunction(name = "chosen_gambits")
    public int chosenGambitsFunction() {
        if (Models.Raid.getCurrentRaid() == null)
            return 0;
        return Models.Gambit.getActiveGambits().size();
    }

    @TemplateFunction(name = "chosen_gambit")
    public String chosenGambitFunction(int index) {
        if (Models.Raid.getCurrentRaid() == null)
            return "";
        List<Gambit> gambits = Models.Gambit.getActiveGambits();
        if (index < 0 || index >= gambits.size())
            return "";
        return gambits.get(index).getName();
    }

    @TemplateFunction(name = "chosen_buffs")
    public int chosenBuffsFunction() {
        if (Models.Raid.getCurrentRaid() == null)
            return 0;
        return Models.Raid.getChosenBuffs(McUtils.playerName()).size();
    }

    @TemplateFunction(name = "chosen_buff")
    public String chosenBuffFunction(int index) {
        if (Models.Raid.getCurrentRaid() == null)
            return "";
        List<String> buffs = Models.Raid.getChosenBuffs(McUtils.playerName());
        if (index < 0 || index >= buffs.size())
            return "";
        return buffs.get(index);
    }
}
