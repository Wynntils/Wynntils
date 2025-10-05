/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.raid.type.RaidInfo;
import com.wynntils.models.raid.type.RaidRoomInfo;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Time;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RaidFunctions {
    public static class CurrentRaidFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            RaidInfo raidInfo = Models.Raid.getCurrentRaid();

            if (raidInfo == null) return "";

            return raidInfo.getRaidKind().getRaidName();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("raid");
        }
    }

    public static class CurrentRaidRoomNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.Raid.getCurrentRoomName();
        }
    }

    public static class CurrentRaidStartFunction extends Function<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            RaidInfo currentRaid = Models.Raid.getCurrentRaid();
            if (currentRaid == null) return Time.NONE;
            return Time.of(currentRaid.getRaidStartTime());
        }

        @Override
        protected List<String> getAliases() {
            return List.of("raid_start");
        }
    }

    public static class CurrentRaidTimeFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return -1L;

            return Models.Raid.currentRaidTime();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("raid_time");
        }
    }

    public static class CurrentRaidDamageFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return -1L;

            return Models.Raid.getRaidDamage();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("raid_damage");
        }
    }

    public static class CurrentRaidRoomStartFunction extends Function<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            RaidInfo currentRaid = Models.Raid.getCurrentRaid();
            if (currentRaid == null) return Time.NONE;
            RaidRoomInfo currentRoom = currentRaid.getCurrentRoom();
            if (currentRoom == null) return Time.NONE;

            return Time.of(currentRoom.getRoomStartTime());
        }
    }

    public static class CurrentRaidRoomTimeFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return -1L;

            return Models.Raid.currentRoomTime();
        }
    }

    public static class CurrentRaidRoomDamageFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return -1L;

            return Models.Raid.getCurrentRoomDamage();
        }
    }

    public static class CurrentRaidChallengeCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return -1;

            return Models.Raid.getRaidChallengeCount();
        }
    }

    public static class CurrentRaidBossCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return -1;

            return Models.Raid.getRaidBossCount();
        }
    }

    public static class RaidChallengesFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Raid.getChallenges();
        }
    }

    public static class RaidIntermissionTimeFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return -1L;

            return Models.Raid.getIntermissionTime();
        }
    }

    public static class RaidRoomNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return "";

            int roomNum = arguments.getArgument("roomNumber").getIntegerValue();

            return Models.Raid.getRoomName(roomNum);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("roomNumber", Integer.class, null)));
        }
    }

    public static class RaidRoomStartFunction extends Function<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            RaidInfo currentRaid = Models.Raid.getCurrentRaid();
            if (currentRaid == null) return Time.NONE;

            int roomNum = arguments.getArgument("roomNumber").getIntegerValue();
            RaidRoomInfo room = currentRaid.getRoomByNumber(roomNum);
            if (room == null) return Time.NONE;

            return Time.of(room.getRoomStartTime());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("roomNumber", Integer.class, null)));
        }
    }

    public static class RaidRoomTimeFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return -1L;

            int roomNum = arguments.getArgument("roomNumber").getIntegerValue();

            return Models.Raid.getRoomTime(roomNum);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("roomNumber", Integer.class, null)));
        }
    }

    public static class RaidRoomDamageFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return -1L;

            int roomNum = arguments.getArgument("roomNumber").getIntegerValue();

            return Models.Raid.getRoomDamage(roomNum);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("roomNumber", Integer.class, null)));
        }
    }

    public static class RaidHasRoomFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return false;

            int roomNum = arguments.getArgument("roomNumber").getIntegerValue();

            return Models.Raid.raidHasRoom(roomNum);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("roomNumber", Integer.class, null)));
        }
    }

    public static class RaidIsBossRoomFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return false;

            int roomNum = arguments.getArgument("roomNumber").getIntegerValue();

            return Models.Raid.isBossRoom(roomNum);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("roomNumber", Integer.class, null)));
        }
    }

    public static class RaidPersonalBestTimeFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            return Models.Raid.getRaidBestTime(arguments.getArgument("raidName").getStringValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("raidName", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("raid_pb");
        }
    }

    public static class RaidTimeRemainingFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Raid.getTimeLeft();
        }
    }

    public static class DryAspectsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Raid.getAspectPullsWithoutMythicAspect();
        }
    }

    public static class DryRaidsAspectsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Raid.getRaidsWithoutMythicAspect();
        }
    }

    public static class DryRaidRewardPullsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Raid.getRewardPullsWithoutMythicTome();
        }
    }

    public static class DryRaidsTomesFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Raid.getRaidsWithoutMythicTome();
        }
    }

    public static class RaidsRunsSinceFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            int sinceDays = arguments.getArgument("sinceDays").getIntegerValue();
            return Math.toIntExact(Models.Raid.historicRaids.get().stream()
                    .filter(historicRaidInfo -> historicRaidInfo.endedTimestamp()
                            >= System.currentTimeMillis() - TimeUnit.DAYS.toMillis(sinceDays))
                    .count());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("sinceDays", Integer.class, 7)));
        }
    }

    public static class SpecificRaidRunsSinceFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            String raidName = arguments.getArgument("raidName").getStringValue();
            int sinceDays = arguments.getArgument("sinceDays").getIntegerValue();
            return Math.toIntExact(Models.Raid.historicRaids.get().stream()
                    .filter(historicRaidInfo -> (historicRaidInfo.name().equalsIgnoreCase(raidName)
                                    || historicRaidInfo.abbreviation().equalsIgnoreCase(raidName))
                            && historicRaidInfo.endedTimestamp()
                                    >= System.currentTimeMillis() - TimeUnit.DAYS.toMillis(sinceDays))
                    .count());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("raidName", String.class, null), new Argument<>("sinceDays", Integer.class, null)));
        }
    }
}
