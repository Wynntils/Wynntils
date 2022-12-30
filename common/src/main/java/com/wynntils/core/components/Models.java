/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.core.chat.tabs.ChatTabModel;
import com.wynntils.core.net.hades.model.HadesModel;
import com.wynntils.core.net.hades.model.HadesUserModel;
import com.wynntils.core.services.TranslationModel;
import com.wynntils.wynn.handleditems.ItemModel;
import com.wynntils.wynn.model.BombBellModel;
import com.wynntils.wynn.model.ChatItemModel;
import com.wynntils.wynn.model.CompassModel;
import com.wynntils.wynn.model.LootChestModel;
import com.wynntils.wynn.model.LootrunModel;
import com.wynntils.wynn.model.PlayerInventoryModel;
import com.wynntils.wynn.model.PlayerRelationsModel;
import com.wynntils.wynn.model.RemoteWynntilsUserInfoModel;
import com.wynntils.wynn.model.ServerListModel;
import com.wynntils.wynn.model.ShamanMaskModel;
import com.wynntils.wynn.model.TabModel;
import com.wynntils.wynn.model.actionbar.ActionBarModel;
import com.wynntils.wynn.model.bossbar.BossBarModel;
import com.wynntils.wynn.model.guild.GuildAttackTimerModel;
import com.wynntils.wynn.model.map.MapModel;
import com.wynntils.wynn.model.scoreboard.ScoreboardModel;

public final class Models {
    public static final ActionBarModel ActionBar = new ActionBarModel();
    public static final BombBellModel BombBell = new BombBellModel();
    public static final BossBarModel BossBar = new BossBarModel();
    public static final ChatItemModel ChatItem = new ChatItemModel();
    public static final ChatTabModel ChatTab = new ChatTabModel();
    public static final CompassModel Compass = new CompassModel();
    public static final GuildAttackTimerModel GuildAttackTimer = new GuildAttackTimerModel();
    public static final HadesModel Hades = new HadesModel();
    public static final HadesUserModel HadesUser = new HadesUserModel();
    public static final ItemModel Item = new ItemModel();
    public static final LootChestModel LootChest = new LootChestModel();
    public static final LootrunModel Lootrun = new LootrunModel();
    public static final MapModel Map = new MapModel();
    public static final PlayerInventoryModel PlayerInventory = new PlayerInventoryModel();
    public static final PlayerRelationsModel PlayerRelations = new PlayerRelationsModel();
    public static final RemoteWynntilsUserInfoModel RemoteWynntilsUserInfo = new RemoteWynntilsUserInfoModel();
    public static final ScoreboardModel Scoreboard = new ScoreboardModel();
    public static final ServerListModel ServerList = new ServerListModel();
    public static final ShamanMaskModel ShamanMask = new ShamanMaskModel();
    public static final TabModel Tab = new TabModel();
    public static final TranslationModel Translation = new TranslationModel();
}
