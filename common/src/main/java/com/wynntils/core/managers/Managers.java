/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

import com.wynntils.core.commands.ClientCommandManager;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.core.functions.FunctionManager;
import com.wynntils.core.keybinds.KeyBindManager;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlManager;
import com.wynntils.core.net.athena.WynntilsAccountManager;
import com.wynntils.mc.MinecraftSchedulerManager;
import com.wynntils.wynn.model.CharacterManager;
import com.wynntils.wynn.model.CharacterSelectionManager;
import com.wynntils.wynn.model.ItemProfilesManager;
import com.wynntils.wynn.model.SplashManager;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.model.container.ContainerQueryManager;
import com.wynntils.wynn.model.discoveries.DiscoveryManager;
import com.wynntils.wynn.model.item.ItemStackTransformManager;
import com.wynntils.wynn.model.quests.QuestManager;
import com.wynntils.wynn.model.territory.TerritoryManager;

public final class Managers {
    // Start with UrlManager to give it chance to update URLs in background
    public static final UrlManager Url = new UrlManager();

    // Managers with no dependencies, alphabetically sorted
    public static final CharacterManager Character = new CharacterManager();
    public static final CharacterSelectionManager CharacterSelection = new CharacterSelectionManager();
    public static final ClientCommandManager ClientCommand = new ClientCommandManager();
    public static final ConfigManager Config = new ConfigManager();
    public static final ContainerQueryManager ContainerQuery = new ContainerQueryManager();
    public static final FunctionManager Function = new FunctionManager();
    public static final ItemStackTransformManager ItemStackTransform = new ItemStackTransformManager();
    public static final KeyBindManager KeyBind = new KeyBindManager();
    public static final MinecraftSchedulerManager MinecraftScheduler = new MinecraftSchedulerManager();
    public static final OverlayManager Overlay = new OverlayManager();
    public static final WorldStateManager WorldState = new WorldStateManager();

    // Managers with dependencies, ordered by dependency and then alphabetically
    public static final NetManager Net = new NetManager(Url);
    public static final ItemProfilesManager ItemProfiles = new ItemProfilesManager(Net);
    public static final QuestManager Quest = new QuestManager(Net);
    public static final SplashManager Splash = new SplashManager(Net);
    public static final TerritoryManager Territory = new TerritoryManager(Net);
    public static final UpdateManager Update = new UpdateManager(Net);
    public static final WynntilsAccountManager WynntilsAccount = new WynntilsAccountManager(Net);
    public static final DiscoveryManager Discovery = new DiscoveryManager(Net, Territory, MinecraftScheduler);
}
