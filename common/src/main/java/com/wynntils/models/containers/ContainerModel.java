/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.models.containers.containers.AbilityTreeContainer;
import com.wynntils.models.containers.containers.AbilityTreeResetContainer;
import com.wynntils.models.containers.containers.AspectsContainer;
import com.wynntils.models.containers.containers.BlacksmithContainer;
import com.wynntils.models.containers.containers.CharacterInfoContainer;
import com.wynntils.models.containers.containers.CharacterSelectionContainer;
import com.wynntils.models.containers.containers.ContentBookContainer;
import com.wynntils.models.containers.containers.CosmeticContainer;
import com.wynntils.models.containers.containers.CraftingStationContainer;
import com.wynntils.models.containers.containers.GuildBadgesContainer;
import com.wynntils.models.containers.containers.GuildBankContainer;
import com.wynntils.models.containers.containers.GuildLogContainer;
import com.wynntils.models.containers.containers.GuildManagementContainer;
import com.wynntils.models.containers.containers.GuildMemberListContainer;
import com.wynntils.models.containers.containers.GuildTerritoriesContainer;
import com.wynntils.models.containers.containers.HousingJukeboxContainer;
import com.wynntils.models.containers.containers.HousingListContainer;
import com.wynntils.models.containers.containers.IngredientPouchContainer;
import com.wynntils.models.containers.containers.InventoryContainer;
import com.wynntils.models.containers.containers.ItemIdentifierAugmentsContainer;
import com.wynntils.models.containers.containers.ItemIdentifierContainer;
import com.wynntils.models.containers.containers.JukeboxContainer;
import com.wynntils.models.containers.containers.LeaderboardRewardsContainer;
import com.wynntils.models.containers.containers.LobbyContainer;
import com.wynntils.models.containers.containers.LootrunRewardChestContainer;
import com.wynntils.models.containers.containers.PartyFinderMatchFoundContainer;
import com.wynntils.models.containers.containers.RaidRewardChestContainer;
import com.wynntils.models.containers.containers.RaidRewardPreviewContainer;
import com.wynntils.models.containers.containers.RaidStartContainer;
import com.wynntils.models.containers.containers.RatingRewardsContainer;
import com.wynntils.models.containers.containers.ScrapMenuContainer;
import com.wynntils.models.containers.containers.SeaskipperContainer;
import com.wynntils.models.containers.containers.StoreContainer;
import com.wynntils.models.containers.containers.personal.AccountBankContainer;
import com.wynntils.models.containers.containers.personal.BookshelfContainer;
import com.wynntils.models.containers.containers.personal.CharacterBankContainer;
import com.wynntils.models.containers.containers.personal.IslandBlockBankContainer;
import com.wynntils.models.containers.containers.personal.MiscBucketContainer;
import com.wynntils.models.containers.containers.personal.PersonalBlockBankContainer;
import com.wynntils.models.containers.containers.reward.ChallengeRewardContainer;
import com.wynntils.models.containers.containers.reward.DailyRewardContainer;
import com.wynntils.models.containers.containers.reward.EventContainer;
import com.wynntils.models.containers.containers.reward.FlyingChestContainer;
import com.wynntils.models.containers.containers.reward.IngredientBombRewardContainer;
import com.wynntils.models.containers.containers.reward.ItemBombRewardContainer;
import com.wynntils.models.containers.containers.reward.LootChestContainer;
import com.wynntils.models.containers.containers.reward.ObjectiveRewardContainer;
import com.wynntils.models.containers.containers.trademarket.TradeMarketBuyContainer;
import com.wynntils.models.containers.containers.trademarket.TradeMarketContainer;
import com.wynntils.models.containers.containers.trademarket.TradeMarketFiltersContainer;
import com.wynntils.models.containers.containers.trademarket.TradeMarketOrderContainer;
import com.wynntils.models.containers.containers.trademarket.TradeMarketSellContainer;
import com.wynntils.models.containers.containers.trademarket.TradeMarketTradesContainer;
import com.wynntils.models.guild.type.GuildLogType;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.store.type.CosmeticItemType;
import com.wynntils.models.store.type.StoreItemType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class ContainerModel extends Model {
    // Test in ContainerModel_ABILITY_TREE_PATTERN
    public static final Pattern ABILITY_TREE_PATTERN = Pattern.compile("\uDAFF\uDFEA\uE000");

    public static final String CHARACTER_INFO_NAME = "\uDAFF\uDFDC\uE003";
    public static final String STORE_MENU_NAME =
            "\uDAFF\uDFF4\uE02C\uDAFF\uDF7C\uF027\uDAFF\uDF52\uDB00\uDC3D.\uDAFF\uDF22\uDB00\uDC40.\uDAFF\uDF2F";
    public static final String GUILD_MENU_NAME = "[a-zA-Z\\s]+: Manage";
    public static final String GUILD_DIPLOMACY_MENU_NAME = "[a-zA-Z\\s]+: Diplomacy";
    public static final String MASTERY_TOMES_NAME = "\uDAFF\uDFDB\uE005";

    private static final List<Container> containerTypes = new ArrayList<>();
    private Container currentContainer = null;

    public ContainerModel() {
        super(List.of());

        registerContainers();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenInit(ScreenInitEvent.Pre e) {
        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        currentContainer = null;

        for (Container container : containerTypes) {
            if (container.isScreen(screen)) {
                currentContainer = container;
                currentContainer.setContainerId(screen.getMenu().containerId);
                break;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenClose(ScreenClosedEvent.Post e) {
        currentContainer = null;
    }

    public Container getCurrentContainer() {
        return currentContainer;
    }

    private void registerContainers() {
        // Order does not matter here so just keep it alphabetical
        registerContainer(new AbilityTreeContainer());
        registerContainer(new AbilityTreeResetContainer());
        registerContainer(new AccountBankContainer());
        registerContainer(new AspectsContainer());
        registerContainer(new BlacksmithContainer());
        registerContainer(new BookshelfContainer());
        registerContainer(new ChallengeRewardContainer());
        registerContainer(new CharacterBankContainer());
        registerContainer(new CharacterInfoContainer());
        registerContainer(new CharacterSelectionContainer());
        registerContainer(new ContentBookContainer());
        registerContainer(new CosmeticContainer());
        registerContainer(new DailyRewardContainer());
        registerContainer(new EventContainer());
        registerContainer(new FlyingChestContainer());
        registerContainer(new GuildBadgesContainer());
        registerContainer(new GuildBankContainer());
        registerContainer(new GuildManagementContainer());
        registerContainer(new GuildMemberListContainer());
        registerContainer(new GuildTerritoriesContainer());
        registerContainer(new HousingJukeboxContainer());
        registerContainer(new HousingListContainer());
        registerContainer(new IngredientBombRewardContainer());
        registerContainer(new IngredientPouchContainer());
        registerContainer(new InventoryContainer());
        registerContainer(new IslandBlockBankContainer());
        registerContainer(new ItemBombRewardContainer());
        registerContainer(new ItemIdentifierAugmentsContainer());
        registerContainer(new ItemIdentifierContainer());
        registerContainer(new JukeboxContainer());
        registerContainer(new LeaderboardRewardsContainer());
        registerContainer(new LobbyContainer());
        registerContainer(new LootChestContainer());
        registerContainer(new LootrunRewardChestContainer());
        registerContainer(new MiscBucketContainer());
        registerContainer(new ObjectiveRewardContainer());
        registerContainer(new PartyFinderMatchFoundContainer());
        registerContainer(new PersonalBlockBankContainer());
        registerContainer(new RaidRewardChestContainer());
        registerContainer(new RaidRewardPreviewContainer());
        registerContainer(new RaidStartContainer());
        registerContainer(new RatingRewardsContainer());
        registerContainer(new ScrapMenuContainer());
        registerContainer(new SeaskipperContainer());
        registerContainer(new TradeMarketBuyContainer());
        registerContainer(new TradeMarketContainer());
        registerContainer(new TradeMarketFiltersContainer());
        registerContainer(new TradeMarketOrderContainer());
        registerContainer(new TradeMarketSellContainer());
        registerContainer(new TradeMarketTradesContainer());

        for (CosmeticItemType type : CosmeticItemType.values()) {
            registerContainer(new CosmeticContainer(type));
        }

        for (GuildLogType type : GuildLogType.values()) {
            registerContainer(new GuildLogContainer(type));
        }

        for (ProfessionType type : ProfessionType.craftingProfessionTypes()) {
            registerContainer(new CraftingStationContainer(Pattern.compile(type.getDisplayName()), type));
        }

        for (StoreItemType type : StoreItemType.values()) {
            registerContainer(new StoreContainer(type));
        }
    }

    private void registerContainer(Container container) {
        containerTypes.add(container);
    }
}
