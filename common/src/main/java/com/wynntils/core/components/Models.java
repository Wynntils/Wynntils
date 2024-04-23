/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.models.abilities.ArrowShieldModel;
import com.wynntils.models.abilities.BossBarModel;
import com.wynntils.models.abilities.ShamanMaskModel;
import com.wynntils.models.abilities.ShamanTotemModel;
import com.wynntils.models.abilitytree.AbilityTreeModel;
import com.wynntils.models.activities.ActivityModel;
import com.wynntils.models.activities.caves.CaveModel;
import com.wynntils.models.activities.discoveries.DiscoveryModel;
import com.wynntils.models.activities.quests.QuestModel;
import com.wynntils.models.beacons.BeaconModel;
import com.wynntils.models.character.CharacterModel;
import com.wynntils.models.character.CharacterSelectionModel;
import com.wynntils.models.character.SkillPointModel;
import com.wynntils.models.characterstats.CharacterStatsModel;
import com.wynntils.models.characterstats.CombatXpModel;
import com.wynntils.models.containers.BankModel;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.containers.LootChestModel;
import com.wynntils.models.damage.DamageModel;
import com.wynntils.models.dungeon.DungeonModel;
import com.wynntils.models.elements.ElementModel;
import com.wynntils.models.emeralds.EmeraldModel;
import com.wynntils.models.gear.GearModel;
import com.wynntils.models.gear.SetModel;
import com.wynntils.models.horse.HorseModel;
import com.wynntils.models.ingredients.IngredientModel;
import com.wynntils.models.inventory.InventoryModel;
import com.wynntils.models.items.ItemEncodingModel;
import com.wynntils.models.items.ItemModel;
import com.wynntils.models.lootrun.LootrunModel;
import com.wynntils.models.marker.MarkerModel;
import com.wynntils.models.mobtotem.MobTotemModel;
import com.wynntils.models.npc.NpcModel;
import com.wynntils.models.npcdialogue.NpcDialogueModel;
import com.wynntils.models.objectives.ObjectivesModel;
import com.wynntils.models.players.FriendsModel;
import com.wynntils.models.players.GuildModel;
import com.wynntils.models.players.PartyModel;
import com.wynntils.models.players.PlayerModel;
import com.wynntils.models.profession.ProfessionModel;
import com.wynntils.models.rewards.RewardsModel;
import com.wynntils.models.seaskipper.SeaskipperModel;
import com.wynntils.models.spells.SpellModel;
import com.wynntils.models.stats.ShinyModel;
import com.wynntils.models.stats.StatModel;
import com.wynntils.models.statuseffects.StatusEffectModel;
import com.wynntils.models.territories.GuildAttackTimerModel;
import com.wynntils.models.territories.TerritoryModel;
import com.wynntils.models.token.TokenModel;
import com.wynntils.models.trademarket.TradeMarketModel;
import com.wynntils.models.war.GuildWarTowerModel;
import com.wynntils.models.war.WarModel;
import com.wynntils.models.worlds.BombModel;
import com.wynntils.models.worlds.ServerListModel;
import com.wynntils.models.worlds.WorldStateModel;
import com.wynntils.models.wynnalphabet.WynnAlphabetModel;
import com.wynntils.models.wynnitem.WynnItemModel;

public final class Models {
    public static final AbilityTreeModel AbilityTree = new AbilityTreeModel();
    public static final ArrowShieldModel ArrowShield = new ArrowShieldModel();
    public static final BankModel Bank = new BankModel();
    public static final BeaconModel Beacon = new BeaconModel();
    public static final BombModel Bomb = new BombModel();
    public static final BossBarModel BossBar = new BossBarModel();
    public static final CaveModel Cave = new CaveModel();
    public static final CharacterModel Character = new CharacterModel();
    public static final CharacterSelectionModel CharacterSelection = new CharacterSelectionModel();
    public static final CharacterStatsModel CharacterStats = new CharacterStatsModel();
    public static final CombatXpModel CombatXp = new CombatXpModel();
    public static final ContainerModel Container = new ContainerModel();
    public static final DamageModel Damage = new DamageModel();
    public static final DiscoveryModel Discovery = new DiscoveryModel();
    public static final DungeonModel Dungeon = new DungeonModel();
    public static final ElementModel Element = new ElementModel();
    public static final EmeraldModel Emerald = new EmeraldModel();
    public static final FriendsModel Friends = new FriendsModel();
    public static final GearModel Gear = new GearModel();
    public static final GuildModel Guild = new GuildModel();
    public static final GuildWarTowerModel GuildWarTower = new GuildWarTowerModel();
    public static final HorseModel Horse = new HorseModel();
    public static final ItemEncodingModel ItemEncoding = new ItemEncodingModel();
    public static final ItemModel Item = new ItemModel();
    public static final LootChestModel LootChest = new LootChestModel();
    public static final MarkerModel Marker = new MarkerModel();
    public static final MobTotemModel MobTotem = new MobTotemModel();
    public static final NpcDialogueModel NpcDialogue = new NpcDialogueModel();
    public static final NpcModel Npc = new NpcModel();
    public static final ObjectivesModel Objectives = new ObjectivesModel();
    public static final PartyModel Party = new PartyModel();
    public static final InventoryModel Inventory = new InventoryModel();
    public static final PlayerModel Player = new PlayerModel();
    public static final ProfessionModel Profession = new ProfessionModel();
    public static final QuestModel Quest = new QuestModel();
    public static final SeaskipperModel Seaskipper = new SeaskipperModel();
    public static final ServerListModel ServerList = new ServerListModel();
    public static final SetModel Set = new SetModel();
    public static final ShamanMaskModel ShamanMask = new ShamanMaskModel();
    public static final ShamanTotemModel ShamanTotem = new ShamanTotemModel();
    public static final ShinyModel Shiny = new ShinyModel();
    public static final SkillPointModel SkillPoint = new SkillPointModel();
    public static final SpellModel Spell = new SpellModel();
    public static final StatModel Stat = new StatModel();
    public static final StatusEffectModel StatusEffect = new StatusEffectModel();
    public static final TerritoryModel Territory = new TerritoryModel();
    public static final TokenModel Token = new TokenModel();
    public static final TradeMarketModel TradeMarket = new TradeMarketModel();
    public static final WarModel War = new WarModel();
    public static final WorldStateModel WorldState = new WorldStateModel();
    public static final WynnAlphabetModel WynnAlphabet = new WynnAlphabetModel();
    public static final WynnItemModel WynnItem = new WynnItemModel();

    // Models with constructor dependencies, ordered alphabetically as far as possible
    public static final ActivityModel Activity = new ActivityModel(Marker);
    public static final GuildAttackTimerModel GuildAttackTimer = new GuildAttackTimerModel(Marker);
    public static final LootrunModel Lootrun = new LootrunModel(Marker);
    public static final RewardsModel Rewards = new RewardsModel(WynnItem);
    public static final IngredientModel Ingredient = new IngredientModel(WynnItem);
}
