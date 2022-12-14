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
    public static final NetManager NET = new NetManager();
    public static final UrlManager URL = new UrlManager();
    public static final ConfigManager CONFIG = new ConfigManager();
    public static final CharacterManager CHARACTER = new CharacterManager();
    public static final CharacterSelectionManager CHARACTER_SELECTION = new CharacterSelectionManager();
    public static final ClientCommandManager CLIENT_COMMAND = new ClientCommandManager();
    public static final ContainerQueryManager CONTAINER_QUERY = new ContainerQueryManager();
    public static final DiscoveryManager DISCOVERY = new DiscoveryManager();
    public static final FunctionManager FUNCTION = new FunctionManager();
    public static final KeyBindManager KEY_BIND = new KeyBindManager();
    public static final MinecraftSchedulerManager MINECRAFT_SCHEDULER = new MinecraftSchedulerManager();
    public static final OverlayManager OVERLAY = new OverlayManager();
    public static final QuestManager QUEST = new QuestManager();
    public static final UpdateManager UPDATE = new UpdateManager();
    public static final WynntilsAccountManager WYNNTILS_ACCOUNT = new WynntilsAccountManager();
    public static final ItemProfilesManager ITEM_PROFILES = new ItemProfilesManager();
    public static final ItemStackTransformManager ITEM_STACK_TRANSFORM = new ItemStackTransformManager();
    public static final SplashManager SPLASH = new SplashManager();
    public static final WorldStateManager WORLD_STATE = new WorldStateManager();
    public static final TerritoryManager TERRITORY = new TerritoryManager();
}
