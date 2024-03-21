/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.models.containers.type.WynncraftContainer;
import com.wynntils.models.containers.type.wynncontainers.AbilityTreeContainer;
import com.wynntils.models.containers.type.wynncontainers.AccountBankContainer;
import com.wynntils.models.containers.type.wynncontainers.BlockBankContainer;
import com.wynntils.models.containers.type.wynncontainers.BookshelfContainer;
import com.wynntils.models.containers.type.wynncontainers.CharacterBankContainer;
import com.wynntils.models.containers.type.wynncontainers.ContentBookContainer;
import com.wynntils.models.containers.type.wynncontainers.GuildBankContainer;
import com.wynntils.models.containers.type.wynncontainers.GuildMemberListContainer;
import com.wynntils.models.containers.type.wynncontainers.GuildTerritoriesContainer;
import com.wynntils.models.containers.type.wynncontainers.HousingJukeboxContainer;
import com.wynntils.models.containers.type.wynncontainers.HousingListContainer;
import com.wynntils.models.containers.type.wynncontainers.JukeboxContainer;
import com.wynntils.models.containers.type.wynncontainers.LobbyContainer;
import com.wynntils.models.containers.type.wynncontainers.MiscBucketContainer;
import com.wynntils.models.containers.type.wynncontainers.PetMenuContainer;
import com.wynntils.models.containers.type.wynncontainers.ScrapMenuContainer;
import com.wynntils.models.containers.type.wynncontainers.TradeMarketFiltersContainer;
import com.wynntils.models.containers.type.wynncontainers.TradeMarketPrimaryContainer;
import com.wynntils.models.containers.type.wynncontainers.TradeMarketSecondaryContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ContainerModel extends Model {
    // Test in ContainerModel_ABILITY_TREE_PATTERN
    public static final Pattern ABILITY_TREE_PATTERN =
            Pattern.compile("(?:Warrior|Shaman|Mage|Assassin|Archer) Abilities");

    // Test in ContainerModel_LOOT_CHEST_PATTERN
    private static final Pattern LOOT_CHEST_PATTERN = Pattern.compile("Loot Chest (§.)\\[.+\\]");

    public static final String CHARACTER_INFO_NAME = "Character Info";
    public static final String COSMETICS_MENU_NAME = "Crates, Bombs & Cosmetics";
    public static final String MASTERY_TOMES_NAME = "Mastery Tomes";

    private static final StyledText SEASKIPPER_TITLE = StyledText.fromString("V.S.S. Seaskipper");

    private static final List<WynncraftContainer> containerTypes = new ArrayList<>();
    private WynncraftContainer currentContainer = null;

    public ContainerModel() {
        super(List.of());

        registerWynncraftContainers();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenInit(ScreenInitEvent e) {
        currentContainer = null;

        if (!(e.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        for (WynncraftContainer container : containerTypes) {
            if (container.isScreen(screen)) {
                currentContainer = container;
                break;
            }
        }
    }

    // TODO: Port the remaining isXScreen to WynncraftContainers
    public boolean isCharacterInfoScreen(Screen screen) {
        return StyledText.fromComponent(screen.getTitle())
                .getStringWithoutFormatting()
                .equals(CHARACTER_INFO_NAME);
    }

    public boolean isLootChest(Screen screen) {
        return screen instanceof ContainerScreen && lootChestMatcher(screen).matches();
    }

    public boolean isLootChest(String title) {
        return title.startsWith("Loot Chest");
    }

    public boolean isRewardChest(String title) {
        return title.startsWith("Daily Rewards")
                || title.contains("Objective Rewards")
                || title.contains("Challenge Rewards")
                || title.contains("Flying Chest");
    }

    public boolean isLootOrRewardChest(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return false;

        String title = screen.getTitle().getString();
        return isLootOrRewardChest(title);
    }

    public boolean isLootOrRewardChest(String title) {
        return isLootChest(title) || isRewardChest(title);
    }

    public WynncraftContainer getCurrentContainer() {
        return currentContainer;
    }

    public Matcher lootChestMatcher(Screen screen) {
        return StyledText.fromComponent(screen.getTitle()).getNormalized().getMatcher(LOOT_CHEST_PATTERN);
    }

    public boolean isSeaskipper(Component component) {
        return StyledText.fromComponent(component).equals(SEASKIPPER_TITLE);
    }

    private void registerWynncraftContainers() {
        // Order does not matter here so just keep it alphabetical
        registerWynncraftContainer(new AbilityTreeContainer());
        registerWynncraftContainer(new AccountBankContainer());
        registerWynncraftContainer(new BlockBankContainer());
        registerWynncraftContainer(new BookshelfContainer());
        registerWynncraftContainer(new CharacterBankContainer());
        registerWynncraftContainer(new ContentBookContainer());
        registerWynncraftContainer(new GuildBankContainer());
        registerWynncraftContainer(new GuildMemberListContainer());
        registerWynncraftContainer(new GuildTerritoriesContainer());
        registerWynncraftContainer(new HousingJukeboxContainer());
        registerWynncraftContainer(new HousingListContainer());
        registerWynncraftContainer(new JukeboxContainer());
        registerWynncraftContainer(new LobbyContainer());
        registerWynncraftContainer(new MiscBucketContainer());
        registerWynncraftContainer(new PetMenuContainer());
        registerWynncraftContainer(new ScrapMenuContainer());
        registerWynncraftContainer(new TradeMarketFiltersContainer());
        registerWynncraftContainer(new TradeMarketPrimaryContainer());
        registerWynncraftContainer(new TradeMarketSecondaryContainer());
    }

    private void registerWynncraftContainer(WynncraftContainer container) {
        containerTypes.add(container);
    }
}
