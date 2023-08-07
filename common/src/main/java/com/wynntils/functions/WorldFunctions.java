/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.mobtotem.MobTotem;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.token.type.TokenGatekeeper;
import com.wynntils.models.worlds.profile.ServerProfile;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Locale;

public class WorldFunctions {
    public static class CurrentWorldFunction extends Function<String> {
        private static final String NO_DATA = "<unknown>";
        private static final String NO_WORLD = "<not on world>";

        @Override
        public String getValue(FunctionArguments arguments) {
            if (!Models.WorldState.onWorld()) {
                return NO_WORLD;
            }

            String currentWorldName = Models.WorldState.getCurrentWorldName();
            return currentWorldName.isEmpty() ? NO_DATA : currentWorldName;
        }

        @Override
        public List<String> getAliases() {
            return List.of("world");
        }
    }

    public static class CurrentWorldUptimeFunction extends Function<String> {
        private static final String NO_DATA = "<unknown>";
        private static final String NO_WORLD = "<not on world>";

        @Override
        public String getValue(FunctionArguments arguments) {
            if (!Models.WorldState.onWorld()) {
                return NO_WORLD;
            }

            String currentWorldName = Models.WorldState.getCurrentWorldName();

            ServerProfile server = Models.ServerList.getServer(currentWorldName);

            if (server == null) {
                return NO_DATA;
            }

            return server.getUptime();
        }

        @Override
        public List<String> getAliases() {
            return List.of("world_uptime", "uptime");
        }
    }

    public static class WorldStateFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.WorldState.getCurrentState().toString().toUpperCase(Locale.ROOT);
        }
    }

    public static class TokenGatekeeperCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Token.getGatekeepers().size();
        }

        @Override
        public List<String> getAliases() {
            return List.of("token_count");
        }
    }

    public static class TokenGatekeeperDepositedFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            int index = arguments.getArgument("gatekeeperNumber").getIntegerValue() - 1;
            List<TokenGatekeeper> gatekeeperList = Models.Token.getGatekeepers();
            if (index >= gatekeeperList.size() || index < 0) return CappedValue.EMPTY;

            return gatekeeperList.get(index).getDeposited();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("gatekeeperNumber", Integer.class, 0)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("token_dep");
        }
    }

    public static class TokenGatekeeperFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            int index = arguments.getArgument("gatekeeperNumber").getIntegerValue() - 1;
            List<TokenGatekeeper> gatekeeperList = Models.Token.getGatekeepers();
            if (index >= gatekeeperList.size() || index < 0) return CappedValue.EMPTY;

            return Models.Token.getCollected(gatekeeperList.get(index));
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("gatekeeperNumber", Integer.class, 0)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("token");
        }
    }

    public static class TokenGatekeeperTypeFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            int index = arguments.getArgument("gatekeeperNumber").getIntegerValue() - 1;
            List<TokenGatekeeper> gatekeeperList = Models.Token.getGatekeepers();
            if (index >= gatekeeperList.size() || index < 0) return "";

            return gatekeeperList.get(index).getGatekeeperTokenName().getString();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("gatekeeperNumber", Integer.class, 0)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("token_type");
        }
    }

    public static class MobTotemCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.MobTotem.getMobTotems().size();
        }
    }

    public static class MobTotemOwnerFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            MobTotem mobTotem = Models.MobTotem.getMobTotem(
                    arguments.getArgument("totemNumber").getIntegerValue() - 1);

            if (mobTotem == null) {
                return "";
            }

            return mobTotem.getOwner();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class MobTotemDistanceFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            MobTotem mobTotem = Models.MobTotem.getMobTotem(
                    arguments.getArgument("totemNumber").getIntegerValue() - 1);

            if (mobTotem == null) {
                return 0.0d;
            }

            return mobTotem.getDistanceToPlayer();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class MobTotemFunction extends Function<Location> {
        @Override
        public Location getValue(FunctionArguments arguments) {
            MobTotem mobTotem = Models.MobTotem.getMobTotem(
                    arguments.getArgument("totemNumber").getIntegerValue() - 1);

            if (mobTotem == null) {
                return new Location(0, 0, 0);
            }

            return Location.containing(mobTotem.getPosition());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class MobTotemTimeLeftFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            MobTotem mobTotem = Models.MobTotem.getMobTotem(
                    arguments.getArgument("totemNumber").getIntegerValue() - 1);

            if (mobTotem == null) {
                return "";
            }

            return mobTotem.getTimerString();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class PingFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Services.Ping.getPing();
        }
    }

    public static class CurrentTerritoryFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            TerritoryProfile territoryProfile = Models.Territory.getTerritoryProfileForPosition(
                    McUtils.player().position());

            if (territoryProfile == null) {
                return "";
            }

            return territoryProfile.getName();
        }

        @Override
        public List<String> getAliases() {
            return List.of("territory");
        }
    }

    public static class CurrentTerritoryOwnerFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            TerritoryProfile territoryProfile = Models.Territory.getTerritoryProfileForPosition(
                    McUtils.player().position());

            if (territoryProfile == null) {
                return "";
            }

            return arguments.getArgument("prefixOnly").getBooleanValue()
                    ? territoryProfile.getGuildPrefix()
                    : territoryProfile.getGuild();
        }

        @Override
        public List<String> getAliases() {
            return List.of("territory_owner");
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("prefixOnly", Boolean.class, false)));
        }
    }

    public static class GatheringCooldownFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            int cooldownLength = Models.Profession.getGatherCooldownTime();
            long gatherCooldownEndTimestamp = Models.WorldState.getServerJoinTimestamp() + cooldownLength * 1000L;
            int gatherCooldownSeconds = (int) ((gatherCooldownEndTimestamp - System.currentTimeMillis()) / 1000);

            if (gatherCooldownSeconds > cooldownLength || gatherCooldownSeconds < 0) return 0;

            return gatherCooldownSeconds;
        }
    }
}
