/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ConnectionEvent;
import com.wynntils.mc.event.PlayerMoveEvent;
import com.wynntils.mc.event.PlayerTeleportEvent;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DiscordRichPresenceFeature extends Feature {

    @RegisterConfig
    public final Config<Boolean> displayLocation = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> displayCharacterInfo = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> displayWorld = new Config<>(true);

    private TerritoryProfile lastTerritoryProfile = null;

    private void handleLocationChange() {
        if (displayLocation.get()) {
            TerritoryProfile territoryProfile = Models.Territory.getTerritoryProfileForPosition(
                    McUtils.player().position());
            if (territoryProfile == null || territoryProfile == lastTerritoryProfile) return;
            lastTerritoryProfile = territoryProfile;
            String location = territoryProfile.getName();
            Managers.Discord.setLocation(location);
        } else {
            Managers.Discord.setLocation("");
        }
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent event) {
        handleLocationChange();
    }

    @SubscribeEvent
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        handleLocationChange();
    }

    @SubscribeEvent
    public void onCharacterUpdate(CharacterUpdateEvent event) {
        if (displayCharacterInfo.get()) {
            String name = StyledText.fromComponent(McUtils.player().getName()).getString(PartStyle.StyleType.NONE);
            int level = Models.CombatXp.getCombatLevel().current();
            ClassType classType = Models.Character.getClassType();
            Managers.Discord.setCharacterInfo(name, level, classType);
        } else {
            Managers.Discord.setWynncraftLogo();
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent event) {
        if (displayWorld.get()) {
            if (event.getNewState() == WorldState.WORLD) {
                Managers.Discord.setWorld(Models.WorldState.getCurrentWorldName());
            } else if (event.getNewState() == WorldState.HUB || event.getNewState() == WorldState.CONNECTING) {
                Managers.Discord.setLocation("");
                Managers.Discord.setWorld("In Hub");
                Managers.Discord.setWynncraftLogo();
            }
        } else {
            Managers.Discord.setWorld("");
        }
    }

    @SubscribeEvent
    public void onDisconnect(ConnectionEvent.DisconnectedEvent e) {
        System.out.println("Disconnecting from Discord");
        Managers.Discord.clearAll();
    }
}
