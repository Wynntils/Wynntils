/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.models.containers.containers.AbilityTreeContainer;
import com.wynntils.models.containers.containers.AbilityTreeResetContainer;
import com.wynntils.models.containers.containers.AspectsContainer;
import com.wynntils.models.containers.containers.CharacterInfoContainer;
import com.wynntils.models.containers.containers.ContentBookContainer;
import com.wynntils.models.containers.containers.CraftingStationContainer;
import com.wynntils.models.containers.containers.GuildBadgesContainer;
import com.wynntils.models.containers.containers.GuildBankContainer;
import com.wynntils.models.containers.containers.GuildManagementContainer;
import com.wynntils.models.containers.containers.GuildMemberListContainer;
import com.wynntils.models.containers.containers.GuildTerritoriesContainer;
import com.wynntils.models.containers.containers.HousingJukeboxContainer;
import com.wynntils.models.containers.containers.HousingListContainer;
import com.wynntils.models.containers.containers.IngredientPouchContainer;
import com.wynntils.models.containers.containers.InventoryContainer;
import com.wynntils.models.containers.containers.JukeboxContainer;
import com.wynntils.models.containers.containers.LeaderboardRewardsContainer;
import com.wynntils.models.containers.containers.LobbyContainer;
import com.wynntils.models.containers.containers.RatingRewardsContainer;
import com.wynntils.models.containers.containers.ScrapMenuContainer;
import com.wynntils.models.containers.containers.SeaskipperContainer;
import com.wynntils.models.containers.containers.TradeMarketContainer;
import com.wynntils.models.containers.containers.TradeMarketFiltersContainer;
import com.wynntils.models.containers.containers.TradeMarketSellContainer;
import com.wynntils.models.containers.containers.cosmetics.HelmetCosmeticsMenuContainer;
import com.wynntils.models.containers.containers.cosmetics.PetMenuContainer;
import com.wynntils.models.containers.containers.cosmetics.PlayerEffectsMenuContainer;
import com.wynntils.models.containers.containers.cosmetics.WeaponCosmeticsMenuContainer;
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
import com.wynntils.models.containers.containers.reward.LootChestContainer;
import com.wynntils.models.containers.containers.reward.ObjectiveRewardContainer;
import com.wynntils.models.profession.type.ProfessionType;
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
    public static final String COSMETICS_MENU_NAME = "Crates, Bombs & Cosmetics";
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
    public void onScreenClose(ScreenClosedEvent e) {
        currentContainer = null;
    }

    public Container getCurrentContainer() {
        return currentContainer;
    }

    private void registerContainers() {
        // Order does not matter here so just keep it alphabetical
        registerContainer(new AbilityTreeContainer());
        registerContainer(new AbilityTreeResetContainer());
        registerContainer(new AspectsContainer());
        registerContainer(new AccountBankContainer());
        registerContainer(new BookshelfContainer());
        registerContainer(new ChallengeRewardContainer());
        registerContainer(new CharacterBankContainer());
        registerContainer(new CharacterInfoContainer());
        registerContainer(new ContentBookContainer());
        registerContainer(new DailyRewardContainer());
        registerContainer(new EventContainer());
        registerContainer(new FlyingChestContainer());
        registerContainer(new GuildBankContainer());
        registerContainer(new GuildBadgesContainer());
        registerContainer(new GuildManagementContainer());
        registerContainer(new GuildMemberListContainer());
        registerContainer(new GuildTerritoriesContainer());
        registerContainer(new HelmetCosmeticsMenuContainer());
        registerContainer(new HousingJukeboxContainer());
        registerContainer(new HousingListContainer());
        registerContainer(new IngredientPouchContainer());
        registerContainer(new IslandBlockBankContainer());
        registerContainer(new InventoryContainer());
        registerContainer(new JukeboxContainer());
        registerContainer(new LeaderboardRewardsContainer());
        registerContainer(new LobbyContainer());
        registerContainer(new LootChestContainer());
        registerContainer(new MiscBucketContainer());
        registerContainer(new ObjectiveRewardContainer());
        registerContainer(new PlayerEffectsMenuContainer());
        registerContainer(new PersonalBlockBankContainer());
        registerContainer(new PetMenuContainer());
        registerContainer(new RatingRewardsContainer());
        registerContainer(new ScrapMenuContainer());
        registerContainer(new SeaskipperContainer());
        registerContainer(new TradeMarketFiltersContainer());
        registerContainer(new TradeMarketContainer());
        registerContainer(new TradeMarketSellContainer());
        registerContainer(new WeaponCosmeticsMenuContainer());

        for (ProfessionType type : ProfessionType.craftingProfessionTypes()) {
            registerContainer(new CraftingStationContainer(Pattern.compile(type.getDisplayName()), type));
        }
    }

    private void registerContainer(Container container) {
        containerTypes.add(container);
    }
}
