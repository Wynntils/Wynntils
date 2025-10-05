/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.bonustotems.BonusTotem;
import com.wynntils.models.bonustotems.type.BonusTotemType;
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
        protected List<String> getAliases() {
            return List.of("world");
        }
    }

    public static class WorldUptimeFunction extends Function<String> {
        private static final String NO_DATA = "<unknown>";
        private static final String NO_WORLD = "<not on world>";

        @Override
        public String getValue(FunctionArguments arguments) {
            String worldName = arguments.getArgument("worldName").getStringValue();

            // Replace world name with the current server, if not provided
            // This is done for backwards compatibility with the old function
            if (worldName.isEmpty()) {
                if (!Models.WorldState.onWorld()) {
                    return NO_WORLD;
                }

                worldName = Models.WorldState.getCurrentWorldName();
            }

            ServerProfile server = Models.ServerList.getServer(worldName);

            if (server == null) {
                return NO_DATA;
            }

            return server.getUptime();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("worldName", String.class, "")));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("uptime", "current_world_uptime");
        }
    }

    public static class NewestWorldFunction extends Function<String> {
        private static final String NO_DATA = "<unknown>";

        @Override
        public String getValue(FunctionArguments arguments) {
            String server = Models.ServerList.getNewestServer();

            if (server == null) {
                return NO_DATA;
            }

            return server;
        }
    }

    public static class WorldStateFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.WorldState.getCurrentState().toString().toUpperCase(Locale.ROOT);
        }
    }

    public static class InStreamFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.WorldState.isInStream();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("streamer");
        }
    }

    public static class GatheringTotemCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.BonusTotem.getBonusTotemsByType(BonusTotemType.GATHERING)
                    .size();
        }
    }

    public static class GatheringTotemOwnerFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(
                    BonusTotemType.GATHERING,
                    arguments.getArgument("totemNumber").getIntegerValue() - 1);

            if (bonusTotem == null) {
                return "";
            }

            return bonusTotem.getOwner();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class GatheringTotemDistanceFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(
                    BonusTotemType.GATHERING,
                    arguments.getArgument("totemNumber").getIntegerValue() - 1);

            if (bonusTotem == null) {
                return 0.0d;
            }

            return bonusTotem.getDistanceToPlayer();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class GatheringTotemFunction extends Function<Location> {
        @Override
        public Location getValue(FunctionArguments arguments) {
            BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(
                    BonusTotemType.GATHERING,
                    arguments.getArgument("totemNumber").getIntegerValue() - 1);

            if (bonusTotem == null) {
                return new Location(0, 0, 0);
            }

            return Location.containing(bonusTotem.getPosition());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class GatheringTotemTimeLeftFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(
                    BonusTotemType.GATHERING,
                    arguments.getArgument("totemNumber").getIntegerValue() - 1);

            if (bonusTotem == null) {
                return "";
            }

            return bonusTotem.getTimerString();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class TokenGatekeeperCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Token.getGatekeepers().size();
        }

        @Override
        protected List<String> getAliases() {
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
                    List.of(new Argument<>("gatekeeperNumber", Integer.class, 0)));
        }

        @Override
        protected List<String> getAliases() {
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
                    List.of(new Argument<>("gatekeeperNumber", Integer.class, 0)));
        }

        @Override
        protected List<String> getAliases() {
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
                    List.of(new Argument<>("gatekeeperNumber", Integer.class, 0)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("token_type");
        }
    }

    public static class MobTotemCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.BonusTotem.getBonusTotemsByType(BonusTotemType.MOB).size();
        }
    }

    public static class MobTotemOwnerFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(
                    BonusTotemType.MOB, arguments.getArgument("totemNumber").getIntegerValue() - 1);

            if (bonusTotem == null) {
                return "";
            }

            return bonusTotem.getOwner();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class MobTotemDistanceFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(
                    BonusTotemType.MOB, arguments.getArgument("totemNumber").getIntegerValue() - 1);

            if (bonusTotem == null) {
                return 0.0d;
            }

            return bonusTotem.getDistanceToPlayer();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class MobTotemFunction extends Function<Location> {
        @Override
        public Location getValue(FunctionArguments arguments) {
            BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(
                    BonusTotemType.MOB, arguments.getArgument("totemNumber").getIntegerValue() - 1);

            if (bonusTotem == null) {
                return new Location(0, 0, 0);
            }

            return Location.containing(bonusTotem.getPosition());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("totemNumber", Integer.class, null)));
        }
    }

    public static class MobTotemTimeLeftFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(
                    BonusTotemType.MOB, arguments.getArgument("totemNumber").getIntegerValue() - 1);

            if (bonusTotem == null) {
                return "";
            }

            return bonusTotem.getTimerString();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("totemNumber", Integer.class, null)));
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
        protected List<String> getAliases() {
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
        protected List<String> getAliases() {
            return List.of("territory_owner");
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("prefixOnly", Boolean.class, false)));
        }
    }

    public static class InMappedAreaFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            float width = arguments.getArgument("width").getDoubleValue().floatValue();
            float height = arguments.getArgument("height").getDoubleValue().floatValue();
            float scale = arguments.getArgument("scale").getDoubleValue().floatValue();

            return Services.Map.isPlayerInMappedArea(width, height, scale);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(List.of(
                    new Argument<>("width", Number.class, 130),
                    new Argument<>("height", Number.class, 130),
                    new Argument<>("scale", Number.class, 1)));
        }
    }
}
