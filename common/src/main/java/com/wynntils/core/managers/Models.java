/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

import com.wynntils.core.chat.ChatModel;
import com.wynntils.core.chat.tabs.ChatTabModel;
import com.wynntils.core.services.TranslationModel;
import com.wynntils.sockets.model.HadesModel;
import com.wynntils.sockets.model.HadesUserModel;
import com.wynntils.wynn.model.ActionBarModel;
import com.wynntils.wynn.model.BombBellModel;
import com.wynntils.wynn.model.CompassModel;
import com.wynntils.wynn.model.GuildAttackTimerModel;
import com.wynntils.wynn.model.LootChestModel;
import com.wynntils.wynn.model.PlayerInventoryModel;
import com.wynntils.wynn.model.PlayerRelationsModel;
import com.wynntils.wynn.model.RemoteWynntilsUserInfoModel;
import com.wynntils.wynn.model.ServerListModel;
import com.wynntils.wynn.model.ShamanMaskModel;
import com.wynntils.wynn.model.TabModel;
import com.wynntils.wynn.model.bossbar.BossBarModel;
import com.wynntils.wynn.model.item.EmeraldPouchItemStackModel;
import com.wynntils.wynn.model.item.GearItemStackModel;
import com.wynntils.wynn.model.item.IngredientItemStackModel;
import com.wynntils.wynn.model.item.IntelligenceSkillPointsItemStackModel;
import com.wynntils.wynn.model.item.PowderItemStackModel;
import com.wynntils.wynn.model.item.ServerItemStackModel;
import com.wynntils.wynn.model.item.SoulPointItemStackModel;
import com.wynntils.wynn.model.item.UnidentifiedItemStackModel;
import com.wynntils.wynn.model.item.properties.AmplifierTierPropertyModel;
import com.wynntils.wynn.model.item.properties.ConsumableChargePropertyModel;
import com.wynntils.wynn.model.item.properties.CosmeticTierPropertyModel;
import com.wynntils.wynn.model.item.properties.DailyRewardMultiplierPropertyModel;
import com.wynntils.wynn.model.item.properties.DungeonKeyPropertyModel;
import com.wynntils.wynn.model.item.properties.DurabilityPropertyModel;
import com.wynntils.wynn.model.item.properties.EmeraldPouchTierPropertyModel;
import com.wynntils.wynn.model.item.properties.GatheringToolPropertyModel;
import com.wynntils.wynn.model.item.properties.HorsePropertyModel;
import com.wynntils.wynn.model.item.properties.IngredientPropertyModel;
import com.wynntils.wynn.model.item.properties.ItemTierPropertyModel;
import com.wynntils.wynn.model.item.properties.MaterialPropertyModel;
import com.wynntils.wynn.model.item.properties.PowderTierPropertyModel;
import com.wynntils.wynn.model.item.properties.SearchOverlayPropertyModel;
import com.wynntils.wynn.model.item.properties.ServerCountPropertyModel;
import com.wynntils.wynn.model.item.properties.SkillIconPropertyModel;
import com.wynntils.wynn.model.item.properties.SkillPointPropertyModel;
import com.wynntils.wynn.model.item.properties.TeleportScrollPropertyModel;
import com.wynntils.wynn.model.map.MapModel;
import com.wynntils.wynn.model.scoreboard.ScoreboardModel;

public class Models {
    public static final TranslationModel Translation = new TranslationModel();
    public static final HadesUserModel HadesUser = new HadesUserModel();
    public static final HadesModel Hades = new HadesModel();
    public static final ChatModel Chat = new ChatModel();
    public static final ChatTabModel ChatTab = new ChatTabModel();
    public static final RemoteWynntilsUserInfoModel RemoteWynntilsUserInfo = new RemoteWynntilsUserInfoModel();
    public static final LootChestModel LootChest = new LootChestModel();
    public static final CompassModel Compass = new CompassModel();
    public static final BossBarModel BossBar = new BossBarModel();
    public static final ActionBarModel ActionBar = new ActionBarModel();
    public static final ShamanMaskModel ShamanMask = new ShamanMaskModel();
    public static final BombBellModel BombBell = new BombBellModel();
    public static final TabModel Tab = new TabModel();
    public static final GuildAttackTimerModel GuildAttackTimer = new GuildAttackTimerModel();
    public static final ScoreboardModel Scoreboard = new ScoreboardModel();
    public static final GatheringToolPropertyModel GatheringToolProperty = new GatheringToolPropertyModel();
    public static final ConsumableChargePropertyModel ConsumableChargeProperty = new ConsumableChargePropertyModel();
    public static final IngredientPropertyModel IngredientProperty = new IngredientPropertyModel();
    public static final ItemTierPropertyModel ItemTierProperty = new ItemTierPropertyModel();
    public static final MapModel Map = new MapModel();
    public static final PowderTierPropertyModel PowderTierProperty = new PowderTierPropertyModel();
    public static final DailyRewardMultiplierPropertyModel DailyRewardMultiplierProperty =
            new DailyRewardMultiplierPropertyModel();
    public static final HorsePropertyModel HorseProperty = new HorsePropertyModel();
    public static final SkillIconPropertyModel SkillIconProperty = new SkillIconPropertyModel();
    public static final MaterialPropertyModel MaterialProperty = new MaterialPropertyModel();
    public static final SearchOverlayPropertyModel SearchOverlayProperty = new SearchOverlayPropertyModel();
    public static final CosmeticTierPropertyModel CosmeticTierProperty = new CosmeticTierPropertyModel();
    public static final ServerCountPropertyModel ServerCountProperty = new ServerCountPropertyModel();
    public static final EmeraldPouchTierPropertyModel EmeraldPouchTierProperty = new EmeraldPouchTierPropertyModel();
    public static final DungeonKeyPropertyModel DungeonKeyProperty = new DungeonKeyPropertyModel();
    public static final EmeraldPouchItemStackModel EmeraldPouchItemStack = new EmeraldPouchItemStackModel();
    public static final ServerItemStackModel ServerItemStack = new ServerItemStackModel();
    public static final IngredientItemStackModel IngredientItemStack = new IngredientItemStackModel();
    public static final IntelligenceSkillPointsItemStackModel IntelligenceSkillPointsItemStack =
            new IntelligenceSkillPointsItemStackModel();
    public static final GearItemStackModel GearItemStack = new GearItemStackModel();
    public static final UnidentifiedItemStackModel UnidentifiedItemStack = new UnidentifiedItemStackModel();
    public static final SoulPointItemStackModel SoulPointItemStack = new SoulPointItemStackModel();
    public static final PowderItemStackModel PowderItemStack = new PowderItemStackModel();
    public static final SkillPointPropertyModel SkillPointProperty = new SkillPointPropertyModel();
    public static final AmplifierTierPropertyModel AmplifierTierProperty = new AmplifierTierPropertyModel();
    public static final PlayerInventoryModel PlayerInventory = new PlayerInventoryModel();
    public static final DurabilityPropertyModel DurabilityProperty = new DurabilityPropertyModel();
    public static final PlayerRelationsModel PlayerRelations = new PlayerRelationsModel();
    public static final TeleportScrollPropertyModel TeleportScrollProperty = new TeleportScrollPropertyModel();
    public static final ServerListModel ServerList = new ServerListModel();
}
