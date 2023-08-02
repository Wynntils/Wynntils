/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ConnectionEvent;
import com.wynntils.mc.event.SetXpEvent;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.Locale;
import net.minecraft.core.Position;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DiscordRichPresenceFeature extends Feature {
    @Persisted
    public final Config<Boolean> displayLocation = new Config<>(true);

    @Persisted
    public final Config<Boolean> displayCharacterInfo = new Config<>(true);

    @Persisted
    public final Config<Boolean> displayWorld = new Config<>(true);

    private static final int TERRITORY_TICKS_DELAY = 10;

    private boolean stopTerritoryCheck = false;
    private TerritoryProfile lastTerritoryProfile = null;

    @SubscribeEvent
    public void onCharacterUpdate(CharacterUpdateEvent event) {
        if (!Models.WorldState.onWorld()) return;

        if (displayCharacterInfo.get()) {
            displayCharacterDetails();
        }
    }

    @SubscribeEvent
    public void onXpChange(SetXpEvent event) {
        if (!Models.WorldState.onWorld()) return;

        if (displayCharacterInfo.get()) {
            displayCharacterDetails();
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent event) {
        if (displayWorld.get()) {
            switch (event.getNewState()) {
                case WORLD -> {
                    Services.Discord.setState(Models.WorldState.getCurrentWorldName());
                    checkTerritory();
                }
                case HUB, CONNECTING -> {
                    Services.Discord.setDetails("");
                    Services.Discord.setState("In Hub");
                    Services.Discord.setWynncraftLogo();
                }
                case CHARACTER_SELECTION -> {
                    if (displayLocation.get()) {
                        Services.Discord.setDetails("Selecting a character");
                    }
                    Services.Discord.setState("");
                    Services.Discord.setWynncraftLogo();
                }
            }
        } else {
            Services.Discord.setState("");
        }
    }

    @SubscribeEvent
    public void onDisconnect(ConnectionEvent.DisconnectedEvent e) {
        Services.Discord.unload();
    }

    private void displayCharacterDetails() {
        if (McUtils.player() == null) return;

        CappedValue combatLevel = Models.CombatXp.getCombatLevel();
        int level = combatLevel.current();
        ClassType classType = Models.Character.getClassType();

        if (classType == null) return;
        String name = StyledText.fromComponent(McUtils.player().getName()).getString(PartStyle.StyleType.NONE);
        Services.Discord.setImageText(name + " - Level " + level + " " + classType.getName());
        Services.Discord.setImage(classType.getActualName(false).toLowerCase(Locale.ROOT));
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
                Services.Discord.setDetails(location);
            }
        }

        Managers.TickScheduler.scheduleLater(this::checkTerritory, TERRITORY_TICKS_DELAY);
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        if (this.isEnabled()) {
            // This isReady() check is required for Linux to not crash on config change.
            if (!Services.Discord.isReady()) {
                // Even though this is in the onConfigUpdate method, it is how the library is first loaded on launch
                if (!Services.Discord.load()) {
                    Managers.Feature.disableFeature(this);
                }
            }

            if (!Models.WorldState.onWorld() && Services.Discord.isReady()) return;

            if (displayLocation.get()) {
                if (lastTerritoryProfile == null) {
                    stopTerritoryCheck = false;
                    checkTerritory();
                }
            } else {
                stopTerritoryCheck = true;
                Services.Discord.setDetails("");
            }

            if (displayCharacterInfo.get()) {
                displayCharacterDetails();
            } else {
                Services.Discord.setWynncraftLogo();
            }

            if (displayWorld.get()) {
                Services.Discord.setState(Models.WorldState.getCurrentWorldName());
            } else {
                Services.Discord.setState("");
            }
        } else {
            Services.Discord.unload();
        }
    }
}
