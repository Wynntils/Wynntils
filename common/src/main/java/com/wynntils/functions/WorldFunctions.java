/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
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
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class WorldFunctions {

    private static final String NO_WORLD = "<not on world>";

    private static final String NO_DATA = "<unknown>";

    @TemplateFunction(name = "current_world", aliases = { "world" })
    public String currentWorldFunction() {
        if (!Models.WorldState.onWorld()) {
            return NO_WORLD;
        }
        String currentWorldName = Models.WorldState.getCurrentWorldName();
        return currentWorldName.isEmpty() ? NO_DATA : currentWorldName;
    }

    @TemplateFunction(name = "world_uptime", aliases = { "uptime", "current_world_uptime" })
    public String worldUptimeFunction(String worldName) {
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

    @TemplateFunction(name = "newest_world")
    public String newestWorldFunction() {
        String server = Models.ServerList.getNewestServer();
        if (server == null) {
            return NO_DATA;
        }
        return server;
    }

    @TemplateFunction(name = "world_state")
    public String worldStateFunction() {
        return Models.WorldState.getCurrentState().toString().toUpperCase(Locale.ROOT);
    }

    @TemplateFunction(name = "in_stream", aliases = { "streamer" })
    public boolean inStreamFunction() {
        return Models.StreamerMode.isInStream();
    }

    @TemplateFunction(name = "gathering_totem_count")
    public int gatheringTotemCountFunction() {
        return Models.BonusTotem.getBonusTotemsByType(BonusTotemType.GATHERING).size();
    }

    @TemplateFunction(name = "gathering_totem_owner")
    public String gatheringTotemOwnerFunction(int totemNumber) {
        BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(BonusTotemType.GATHERING, totemNumber - 1);
        if (bonusTotem == null) {
            return "";
        }
        return bonusTotem.getOwner();
    }

    @TemplateFunction(name = "gathering_totem_distance")
    public double gatheringTotemDistanceFunction(int totemNumber) {
        BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(BonusTotemType.GATHERING, totemNumber - 1);
        if (bonusTotem == null) {
            return 0.0d;
        }
        return bonusTotem.getDistanceToPlayer();
    }

    @TemplateFunction(name = "gathering_totem")
    public Location gatheringTotemFunction(int totemNumber) {
        BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(BonusTotemType.GATHERING, totemNumber - 1);
        if (bonusTotem == null) {
            return new Location(0, 0, 0);
        }
        return Location.containing(bonusTotem.getPosition());
    }

    @TemplateFunction(name = "gathering_totem_time_left")
    public String gatheringTotemTimeLeftFunction(int totemNumber) {
        BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(BonusTotemType.GATHERING, totemNumber - 1);
        if (bonusTotem == null) {
            return "";
        }
        return bonusTotem.getTimerString();
    }

    @TemplateFunction(name = "token_gatekeeper_count", aliases = { "token_count" })
    public int tokenGatekeeperCountFunction() {
        return Models.Token.getGatekeepers().size();
    }

    @TemplateFunction(name = "token_gatekeeper_deposited", aliases = { "token_dep" })
    public CappedValue tokenGatekeeperDepositedFunction(int gatekeeperNumber) {
        int index = gatekeeperNumber - 1;
        List<TokenGatekeeper> gatekeeperList = Models.Token.getGatekeepers();
        if (index >= gatekeeperList.size() || index < 0)
            return CappedValue.EMPTY;
        return gatekeeperList.get(index).getDeposited();
    }

    @TemplateFunction(name = "token_gatekeeper", aliases = { "token" })
    public CappedValue tokenGatekeeperFunction(int gatekeeperNumber) {
        int index = gatekeeperNumber - 1;
        List<TokenGatekeeper> gatekeeperList = Models.Token.getGatekeepers();
        if (index >= gatekeeperList.size() || index < 0)
            return CappedValue.EMPTY;
        return Models.Token.getCollected(gatekeeperList.get(index));
    }

    @TemplateFunction(name = "token_gatekeeper_type", aliases = { "token_type" })
    public String tokenGatekeeperTypeFunction(int gatekeeperNumber) {
        int index = gatekeeperNumber - 1;
        List<TokenGatekeeper> gatekeeperList = Models.Token.getGatekeepers();
        if (index >= gatekeeperList.size() || index < 0)
            return "";
        return gatekeeperList.get(index).getGatekeeperTokenName().getString();
    }

    @TemplateFunction(name = "mob_totem_count")
    public int mobTotemCountFunction() {
        return Models.BonusTotem.getBonusTotemsByType(BonusTotemType.MOB).size();
    }

    @TemplateFunction(name = "mob_totem_owner")
    public String mobTotemOwnerFunction(int totemNumber) {
        BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(BonusTotemType.MOB, totemNumber - 1);
        if (bonusTotem == null) {
            return "";
        }
        return bonusTotem.getOwner();
    }

    @TemplateFunction(name = "mob_totem_distance")
    public double mobTotemDistanceFunction(int totemNumber) {
        BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(BonusTotemType.MOB, totemNumber - 1);
        if (bonusTotem == null) {
            return 0.0d;
        }
        return bonusTotem.getDistanceToPlayer();
    }

    @TemplateFunction(name = "mob_totem")
    public Location mobTotemFunction(int totemNumber) {
        BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(BonusTotemType.MOB, totemNumber - 1);
        if (bonusTotem == null) {
            return new Location(0, 0, 0);
        }
        return Location.containing(bonusTotem.getPosition());
    }

    @TemplateFunction(name = "mob_totem_time_left")
    public String mobTotemTimeLeftFunction(int totemNumber) {
        BonusTotem bonusTotem = Models.BonusTotem.getBonusTotem(BonusTotemType.MOB, totemNumber - 1);
        if (bonusTotem == null) {
            return "";
        }
        return bonusTotem.getTimerString();
    }

    @TemplateFunction(name = "ping")
    public int pingFunction() {
        return Services.Ping.getPing();
    }

    @TemplateFunction(name = "current_territory", aliases = { "territory" })
    public String currentTerritoryFunction() {
        TerritoryProfile territoryProfile = Models.Territory.getTerritoryProfileForPosition(McUtils.player().position());
        if (territoryProfile == null) {
            return "";
        }
        return territoryProfile.getName();
    }

    @TemplateFunction(name = "current_territory_owner", aliases = { "territory_owner" })
    public String currentTerritoryOwnerFunction(boolean prefixOnly) {
        TerritoryProfile territoryProfile = Models.Territory.getTerritoryProfileForPosition(McUtils.player().position());
        if (territoryProfile == null) {
            return "";
        }
        return prefixOnly ? territoryProfile.getGuildPrefix() : territoryProfile.getGuild();
    }

    @TemplateFunction(name = "in_mapped_area")
    public boolean inMappedAreaFunction(Number width, Number scale, Number height) {
        return Services.Map.isPlayerInMappedArea(width.floatValue(), height.floatValue(), scale.floatValue());
    }
}
