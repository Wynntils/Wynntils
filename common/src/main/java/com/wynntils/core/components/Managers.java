/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.commands.ClientCommandManager;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.config.upfixers.ConfigUpfixerManager;
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.core.functions.FunctionManager;
import com.wynntils.core.keybinds.KeyBindManager;
import com.wynntils.core.mod.CrashReportManager;
import com.wynntils.core.mod.TickSchedulerManager;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlManager;
import com.wynntils.core.net.athena.UpdateManager;
import com.wynntils.core.net.athena.WynntilsAccountManager;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.core.splashes.SplashManager;
import com.wynntils.models.character.CharacterManager;
import com.wynntils.models.character.CharacterSelectionManager;
import com.wynntils.models.containers.ContainerManager;
import com.wynntils.models.discoveries.DiscoveryManager;
import com.wynntils.models.favorites.FavoritesManager;
import com.wynntils.models.gear.GearItemManager;
import com.wynntils.models.gear.GearProfilesManager;
import com.wynntils.models.horse.HorseManager;
import com.wynntils.models.quests.QuestManager;
import com.wynntils.models.territories.TerritoryManager;
import com.wynntils.models.worlds.WorldStateManager;
import org.apache.commons.lang3.reflect.FieldUtils;

public final class Managers {
    // Start with UrlManager to give it chance to update URLs in background
    public static final UrlManager Url = new UrlManager();

    // Managers with no dependencies, alphabetically sorted
    public static final CharacterManager Character = new CharacterManager();
    public static final CharacterSelectionManager CharacterSelection = new CharacterSelectionManager();
    public static final ClientCommandManager ClientCommand = new ClientCommandManager();
    public static final ConfigUpfixerManager ConfigUpfixer = new ConfigUpfixerManager();
    public static final ContainerManager Container = new ContainerManager();
    public static final CrashReportManager CrashReport = new CrashReportManager();
    public static final FavoritesManager Favorites = new FavoritesManager();
    public static final FunctionManager Function = new FunctionManager();
    public static final GearItemManager GearItem = new GearItemManager();
    public static final HorseManager Horse = new HorseManager();
    public static final KeyBindManager KeyBind = new KeyBindManager();
    public static final NotificationManager Notification = new NotificationManager();
    public static final TickSchedulerManager TickScheduler = new TickSchedulerManager();
    public static final WorldStateManager WorldState = new WorldStateManager();

    // Managers with dependencies, ordered by dependency and then alphabetically
    public static final ConfigManager Config = new ConfigManager(ConfigUpfixer);
    public static final NetManager Net = new NetManager(Url);
    public static final GearProfilesManager GearProfiles = new GearProfilesManager(Net, GearItem);
    public static final OverlayManager Overlay = new OverlayManager(CrashReport);
    public static final QuestManager Quest = new QuestManager(Net);
    public static final SplashManager Splash = new SplashManager(Net);
    public static final TerritoryManager Territory = new TerritoryManager(Net);
    public static final UpdateManager Update = new UpdateManager(Net);
    public static final WynntilsAccountManager WynntilsAccount = new WynntilsAccountManager(Net);
    public static final DiscoveryManager Discovery = new DiscoveryManager(Net, Territory, TickScheduler);

    public static void init() {
        // Register all manager singletons as event listeners

        FieldUtils.getAllFieldsList(Managers.class).stream()
                .filter(field -> Manager.class.isAssignableFrom(field.getType()))
                .forEach(field -> {
                    try {
                        WynntilsMod.registerEventListener(field.get(null));
                    } catch (IllegalAccessException e) {
                        WynntilsMod.error("Internal error in Managers", e);
                        throw new RuntimeException(e);
                    }
                });
    }
}
