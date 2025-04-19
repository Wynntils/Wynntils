/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.raid.type.RaidInfo;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

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
                    List.of(new FunctionArguments.Argument<>("roomNumber", Integer.class, null)));
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
                    List.of(new FunctionArguments.Argument<>("roomNumber", Integer.class, null)));
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
                    List.of(new FunctionArguments.Argument<>("roomNumber", Integer.class, null)));
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
                    List.of(new FunctionArguments.Argument<>("roomNumber", Integer.class, null)));
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
                    List.of(new FunctionArguments.Argument<>("roomNumber", Integer.class, null)));
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
                    List.of(new FunctionArguments.Argument<>("raidName", String.class, null)));
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
}
