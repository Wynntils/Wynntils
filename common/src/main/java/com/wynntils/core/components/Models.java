/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.models.abilities.BossBarModel;
import com.wynntils.models.abilities.ShamanMaskModel;
import com.wynntils.models.abilities.ShamanTotemModel;
import com.wynntils.models.character.CharacterModel;
import com.wynntils.models.character.CharacterSelectionModel;
import com.wynntils.models.character.PlayerInventoryModel;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.containers.LootChestModel;
import com.wynntils.models.discoveries.DiscoveryModel;
import com.wynntils.models.emeralds.EmeraldModel;
import com.wynntils.models.favorites.FavoritesModel;
import com.wynntils.models.gear.GearItemModel;
import com.wynntils.models.gear.GearProfilesModel;
import com.wynntils.models.gearinfo.GearInfoModel;
import com.wynntils.models.horse.HorseModel;
import com.wynntils.models.ingredients.IngredientProfilesModel;
import com.wynntils.models.items.ItemModel;
import com.wynntils.models.lootruns.LootrunModel;
import com.wynntils.models.map.CompassModel;
import com.wynntils.models.map.MapModel;
import com.wynntils.models.objectives.ObjectivesModel;
import com.wynntils.models.players.PlayerModel;
import com.wynntils.models.players.PlayerRelationsModel;
import com.wynntils.models.players.hades.HadesModel;
import com.wynntils.models.quests.QuestModel;
import com.wynntils.models.spells.SpellModel;
import com.wynntils.models.stats.StatModel;
import com.wynntils.models.territories.GuildAttackTimerModel;
import com.wynntils.models.territories.TerritoryModel;
import com.wynntils.models.worlds.BombBellModel;
import com.wynntils.models.worlds.ServerListModel;
import com.wynntils.models.worlds.WorldStateModel;

public final class Models {
    public static final BombBellModel BombBell = new BombBellModel();
    public static final BossBarModel BossBar = new BossBarModel();
    public static final CharacterModel Character = new CharacterModel();
    public static final CharacterSelectionModel CharacterSelection = new CharacterSelectionModel();
    public static final CompassModel Compass = new CompassModel();
    public static final ContainerModel Container = new ContainerModel();
    public static final FavoritesModel Favorites = new FavoritesModel();
    public static final GearProfilesModel GearProfiles = new GearProfilesModel();
    public static final GuildAttackTimerModel GuildAttackTimer = new GuildAttackTimerModel();
    public static final IngredientProfilesModel IngredientProfiles = new IngredientProfilesModel();
    public static final ObjectivesModel Objectives = new ObjectivesModel();
    public static final PlayerInventoryModel PlayerInventory = new PlayerInventoryModel();
    public static final PlayerModel Player = new PlayerModel();
    public static final ServerListModel ServerList = new ServerListModel();
    public static final StatModel Stat = new StatModel();
    public static final TerritoryModel Territory = new TerritoryModel();
    public static final WorldStateModel WorldState = new WorldStateModel();

    // Models with dependencies, ordered alphabetically as far as possible
    public static final GearInfoModel GearInfo = new GearInfoModel(Stat);
    public static final GearItemModel GearItem = new GearItemModel(GearProfiles);
    public static final HadesModel Hades = new HadesModel(Character, WorldState);
    public static final ItemModel Item = new ItemModel(GearInfo, GearItem, GearProfiles, IngredientProfiles);
    public static final LootChestModel LootChest = new LootChestModel(Container);
    public static final LootrunModel Lootrun = new LootrunModel(Container);
    public static final MapModel Map = new MapModel(GuildAttackTimer);
    public static final PlayerRelationsModel PlayerRelations = new PlayerRelationsModel(WorldState);
    public static final QuestModel Quest = new QuestModel(Character);
    public static final ShamanMaskModel ShamanMask = new ShamanMaskModel(WorldState);
    public static final ShamanTotemModel ShamanTotem = new ShamanTotemModel(WorldState);
    public static final SpellModel Spell = new SpellModel(Character);
    public static final DiscoveryModel Discovery = new DiscoveryModel(Character, Compass, Quest, Territory);
    public static final EmeraldModel Emerald = new EmeraldModel(Item);
    public static final HorseModel Horse = new HorseModel(Item);
}
