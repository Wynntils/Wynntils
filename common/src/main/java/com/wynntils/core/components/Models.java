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
import com.wynntils.wynn.model.item.ItemStackModel.EmeraldPouchItemStackModel;
import com.wynntils.wynn.model.item.ItemStackModel.GearItemStackModel;
import com.wynntils.wynn.model.item.ItemStackModel.IngredientItemStackModel;
import com.wynntils.wynn.model.item.ItemStackModel.IntelligenceSkillPointsItemStackModel;
import com.wynntils.wynn.model.item.ItemStackModel.PowderItemStackModel;
import com.wynntils.wynn.model.item.ItemStackModel.ServerItemStackModel;
import com.wynntils.wynn.model.item.ItemStackModel.SoulPointItemStackModel;
import com.wynntils.wynn.model.item.ItemStackModel.UnidentifiedItemStackModel;
import com.wynntils.wynn.model.item.PropertyModel.AmplifierTierPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.ConsumableChargePropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.CosmeticTierPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.DailyRewardMultiplierPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.DungeonKeyPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.DurabilityPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.EmeraldPouchTierPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.GatheringToolPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.HorsePropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.IngredientPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.ItemTierPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.MaterialPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.PowderTierPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.SearchOverlayPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.ServerCountPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.SkillIconPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.SkillPointPropertyModel;
import com.wynntils.wynn.model.item.PropertyModel.TeleportScrollPropertyModel;
import com.wynntils.wynn.model.map.MapModel;
import com.wynntils.wynn.model.scoreboard.ScoreboardModel;

public final class Models {
    public static final ActionBarModel ActionBar = new ActionBarModel();
    public static final AmplifierTierPropertyModel AmplifierTierProperty = new AmplifierTierPropertyModel();
    public static final BombBellModel BombBell = new BombBellModel();
    public static final BossBarModel BossBar = new BossBarModel();
    public static final ChatTabModel ChatTab = new ChatTabModel();
    public static final CompassModel Compass = new CompassModel();
    public static final ConsumableChargePropertyModel ConsumableChargeProperty = new ConsumableChargePropertyModel();
    public static final CosmeticTierPropertyModel CosmeticTierProperty = new CosmeticTierPropertyModel();
    public static final DailyRewardMultiplierPropertyModel DailyRewardMultiplierProperty =
            new DailyRewardMultiplierPropertyModel();
    public static final DungeonKeyPropertyModel DungeonKeyProperty = new DungeonKeyPropertyModel();
    public static final DurabilityPropertyModel DurabilityProperty = new DurabilityPropertyModel();
    public static final EmeraldPouchItemStackModel EmeraldPouchItemStack = new EmeraldPouchItemStackModel();
    public static final EmeraldPouchTierPropertyModel EmeraldPouchTierProperty = new EmeraldPouchTierPropertyModel();
    public static final GatheringToolPropertyModel GatheringToolProperty = new GatheringToolPropertyModel();
    public static final GearItemStackModel GearItemStack = new GearItemStackModel();
    public static final GuildAttackTimerModel GuildAttackTimer = new GuildAttackTimerModel();
    public static final HadesModel Hades = new HadesModel();
    public static final HadesUserModel HadesUser = new HadesUserModel();
    public static final HorsePropertyModel HorseProperty = new HorsePropertyModel();
    public static final IngredientItemStackModel IngredientItemStack = new IngredientItemStackModel();
    public static final IngredientPropertyModel IngredientProperty = new IngredientPropertyModel();
    public static final IntelligenceSkillPointsItemStackModel IntelligenceSkillPointsItemStack =
            new IntelligenceSkillPointsItemStackModel();
    public static final ItemModel Item = new ItemModel();
    public static final ItemTierPropertyModel ItemTierProperty = new ItemTierPropertyModel();
    public static final LootChestModel LootChest = new LootChestModel();
    public static final LootrunModel Lootrun = new LootrunModel();
    public static final MapModel Map = new MapModel();
    public static final MaterialPropertyModel MaterialProperty = new MaterialPropertyModel();
    public static final PlayerInventoryModel PlayerInventory = new PlayerInventoryModel();
    public static final PlayerRelationsModel PlayerRelations = new PlayerRelationsModel();
    public static final PowderItemStackModel PowderItemStack = new PowderItemStackModel();
    public static final PowderTierPropertyModel PowderTierProperty = new PowderTierPropertyModel();
    public static final RemoteWynntilsUserInfoModel RemoteWynntilsUserInfo = new RemoteWynntilsUserInfoModel();
    public static final ScoreboardModel Scoreboard = new ScoreboardModel();
    public static final SearchOverlayPropertyModel SearchOverlayProperty = new SearchOverlayPropertyModel();
    public static final ServerCountPropertyModel ServerCountProperty = new ServerCountPropertyModel();
    public static final ServerItemStackModel ServerItemStack = new ServerItemStackModel();
    public static final ServerListModel ServerList = new ServerListModel();
    public static final ShamanMaskModel ShamanMask = new ShamanMaskModel();
    public static final SkillIconPropertyModel SkillIconProperty = new SkillIconPropertyModel();
    public static final SkillPointPropertyModel SkillPointProperty = new SkillPointPropertyModel();
    public static final SoulPointItemStackModel SoulPointItemStack = new SoulPointItemStackModel();
    public static final TabModel Tab = new TabModel();
    public static final TeleportScrollPropertyModel TeleportScrollProperty = new TeleportScrollPropertyModel();
    public static final TranslationModel Translation = new TranslationModel();
    public static final UnidentifiedItemStackModel UnidentifiedItemStack = new UnidentifiedItemStackModel();
}
