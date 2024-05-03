/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.raid.type.Raid;
import com.wynntils.models.raid.type.RaidRoomType;
import java.util.List;

public class RaidFunctions {
    public static class CurrentRaidFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            Raid currentRaid = Models.Raid.getCurrentRaid();

            return currentRaid == null ? "" : currentRaid.getName();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("raid");
        }
    }

    public static class CurrentRaidRoomFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return "";
            // Room should never be null if the raid is not but just in case the tracking fails
            if (Models.Raid.getCurrentRoom() == null) return "";

            return Models.Raid.getCurrentRoom().name();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("raid_room");
        }
    }

    public static class CurrentRaidTimeFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return -1;

            return Models.Raid.currentRaidTime();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("raid_time");
        }
    }

    public static class CurrentRaidRoomTimeFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return -1;
            // Room should never be null if the raid is not but just in case the tracking fails
            if (Models.Raid.getCurrentRoom() == null) return -1;

            return Models.Raid.currentRoomTime();
        }
    }

    public static class RaidRoomTimeFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            if (Models.Raid.getCurrentRaid() == null) return -1;

            RaidRoomType roomType =
                    RaidRoomType.fromName(arguments.getArgument("roomName").getStringValue());

            if (roomType == null) return -1;

            return Models.Raid.getRoomTime(roomType);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("roomName", String.class, null)));
        }
    }

    public static class RaidPersonalBestTimeFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            Raid raid = Raid.fromName(arguments.getArgument("raidName").getStringValue());

            if (raid == null) return -1;

            return Models.Raid.getRaidBestTime(raid);
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
}
