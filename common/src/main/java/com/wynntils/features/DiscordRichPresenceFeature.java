/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ConnectionEvent;
import com.wynntils.mc.event.SetXpEvent;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.Locale;
import net.minecraft.core.Position;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DiscordRichPresenceFeature extends Feature {
    @RegisterConfig
    public final Config<Boolean> displayLocation = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> displayCharacterInfo = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> displayWorld = new Config<>(true);

    private static final int TERRITORY_TICKS_DELAY = 10;

    private boolean stopTerritoryCheck = false;
    private TerritoryProfile lastTerritoryProfile = null;
    private ClassType classType = null;
    private int level = 0;

    @SubscribeEvent
    public void onCharacterUpdate(CharacterUpdateEvent event) {
        if (!Models.WorldState.onWorld()) return;

        // classType needs to be set even when config is disabled so if the config is enabled later, it will not require
        // a relog or class change
        classType = Models.Character.getClassType();
        if (displayCharacterInfo.get()) {
            displayCharacterDetails();
        }
    }

    @SubscribeEvent
    public void onXpChange(SetXpEvent event) {
        if (!Models.WorldState.onWorld()) return;

        // same as above, level needs to be set even when config is disabled
        level = event.getExperienceLevel();
        if (displayCharacterInfo.get()) {
            displayCharacterDetails();
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent event) {
        if (displayWorld.get()) {
            switch (event.getNewState()) {
                case WORLD -> {
                    Managers.Discord.setState(Models.WorldState.getCurrentWorldName());
                    checkTerritory();
                }
                case HUB, CONNECTING -> {
                    Managers.Discord.setDetails("");
                    Managers.Discord.setState("In Hub");
                    Managers.Discord.setWynncraftLogo();
                }
                case CHARACTER_SELECTION -> {
                    if (displayLocation.get()) {
                        Managers.Discord.setDetails("Selecting a character");
                    }
                    Managers.Discord.setState("");
                    Managers.Discord.setWynncraftLogo();
                }
            }
        } else {
            Managers.Discord.setState("");
        }
    }

    @SubscribeEvent
    public void onDisconnect(ConnectionEvent.DisconnectedEvent e) {
        Managers.Discord.unload();
    }

    private void displayCharacterDetails() {
        if (classType == null) return;
        String name = StyledText.fromComponent(McUtils.player().getName()).getString(PartStyle.StyleType.NONE);
        Managers.Discord.setImageText(name + " - Level " + level + " " + classType.getName());
        Managers.Discord.setImage(classType.getActualName(false).toLowerCase(Locale.ROOT));
    }

    private void checkTerritory() {
        if (stopTerritoryCheck || !Models.WorldState.onWorld()) {
            lastTerritoryProfile = null;
            stopTerritoryCheck = false;
            return;
        }

        Position position = McUtils.player().position();
        if (position != null) {
            TerritoryProfile territoryProfile = Models.Territory.getTerritoryProfileForPosition(
                    McUtils.player().position());
            if (territoryProfile != null && territoryProfile != lastTerritoryProfile) {
                lastTerritoryProfile = territoryProfile;
                String location = territoryProfile.getName();
                Managers.Discord.setDetails(location);
            }
        }

        Managers.TickScheduler.scheduleLater(this::checkTerritory, TERRITORY_TICKS_DELAY);
    }

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        if (this.isEnabled()) {
            Managers.Discord.load();

            if (!Models.WorldState.onWorld() && Managers.Discord.isReady()) return;

            if (displayLocation.get()) {
                if (lastTerritoryProfile == null) {
                    stopTerritoryCheck = false;
                    checkTerritory();
                }
            } else {
                stopTerritoryCheck = true;
                Managers.Discord.setDetails("");
            }

            if (displayCharacterInfo.get()) {
                displayCharacterDetails();
            } else {
                Managers.Discord.setWynncraftLogo();
            }

            if (displayWorld.get()) {
                Managers.Discord.setState(Models.WorldState.getCurrentWorldName());
            } else {
                Managers.Discord.setState("");
            }
        } else {
            Managers.Discord.unload();
        }
    }
}
