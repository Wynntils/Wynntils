/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.core.chat.ChatTabModel;
import com.wynntils.core.net.hades.model.HadesModel;
import com.wynntils.core.net.hades.model.HadesUserModel;
import com.wynntils.core.net.translation.TranslationModel;
import com.wynntils.models.abilities.BossBarModel;
import com.wynntils.models.abilities.ShamanMaskModel;
import com.wynntils.models.abilities.ShamanTotemModel;
import com.wynntils.models.character.PlayerInventoryModel;
import com.wynntils.models.character.statuseffects.TabModel;
import com.wynntils.models.containers.LootChestModel;
import com.wynntils.models.emeralds.EmeraldModel;
import com.wynntils.models.items.ItemModel;
import com.wynntils.models.lootruns.LootrunModel;
import com.wynntils.models.map.CompassModel;
import com.wynntils.models.map.MapModel;
import com.wynntils.models.objectives.ObjectivesModel;
import com.wynntils.models.players.PlayerModel;
import com.wynntils.models.players.PlayerRelationsModel;
import com.wynntils.models.spells.SpellModel;
import com.wynntils.models.territories.GuildAttackTimerModel;
import com.wynntils.models.worlds.BombBellModel;
import com.wynntils.models.worlds.ServerListModel;
import com.wynntils.wynn.model.actionbar.ActionBarModel;

public final class Models {
    public static final ActionBarModel ActionBar = new ActionBarModel();
    public static final BombBellModel BombBell = new BombBellModel();
    public static final BossBarModel BossBar = new BossBarModel();
    public static final ChatTabModel ChatTab = new ChatTabModel();
    public static final CompassModel Compass = new CompassModel();
    public static final EmeraldModel Emerald = new EmeraldModel();
    public static final GuildAttackTimerModel GuildAttackTimer = new GuildAttackTimerModel();
    public static final HadesModel Hades = new HadesModel();
    public static final HadesUserModel HadesUser = new HadesUserModel();
    public static final ItemModel Item = new ItemModel();
    public static final LootChestModel LootChest = new LootChestModel();
    public static final LootrunModel Lootrun = new LootrunModel();
    public static final MapModel Map = new MapModel();
    public static final ObjectivesModel Objectives = new ObjectivesModel();
    public static final PlayerInventoryModel PlayerInventory = new PlayerInventoryModel();
    public static final PlayerModel Player = new PlayerModel();
    public static final PlayerRelationsModel PlayerRelations = new PlayerRelationsModel();
    public static final ServerListModel ServerList = new ServerListModel();
    public static final ShamanMaskModel ShamanMask = new ShamanMaskModel();
    public static final ShamanTotemModel ShamanTotem = new ShamanTotemModel();
    public static final SpellModel Spell = new SpellModel();
    public static final TabModel Tab = new TabModel();
    public static final TranslationModel Translation = new TranslationModel();
}
