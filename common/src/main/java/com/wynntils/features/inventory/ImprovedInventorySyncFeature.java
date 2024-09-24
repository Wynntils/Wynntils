/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.inventory.event.DesyncableContainerClickEvent;
import com.wynntils.handlers.inventory.resync.ClickPlaceholderSlotResyncStrategy;
import com.wynntils.handlers.inventory.resync.EmptySwapContentBookResyncStrategy;
import com.wynntils.handlers.inventory.resync.IngredientPouchSwapContentBookResyncStrategy;
import com.wynntils.handlers.inventory.resync.InventoryResyncStrategy;
import com.wynntils.handlers.inventory.resync.SwapNonEmptySlotResyncStrategy;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.containers.AbilityTreeContainer;
import com.wynntils.models.containers.containers.AbilityTreeResetContainer;
import com.wynntils.models.containers.containers.AspectsContainer;
import com.wynntils.models.containers.containers.CharacterInfoContainer;
import com.wynntils.models.containers.containers.ContentBookContainer;
import com.wynntils.models.containers.containers.CraftingStationContainer;
import com.wynntils.models.containers.containers.GuildBankContainer;
import com.wynntils.models.containers.containers.GuildManagementContainer;
import com.wynntils.models.containers.containers.GuildMemberListContainer;
import com.wynntils.models.containers.containers.GuildTerritoriesContainer;
import com.wynntils.models.containers.containers.HousingJukeboxContainer;
import com.wynntils.models.containers.containers.HousingListContainer;
import com.wynntils.models.containers.containers.IngredientPouchContainer;
import com.wynntils.models.containers.containers.InventoryContainer;
import com.wynntils.models.containers.containers.JukeboxContainer;
import com.wynntils.models.containers.containers.LobbyContainer;
import com.wynntils.models.containers.containers.PetMenuContainer;
import com.wynntils.models.containers.containers.SeaskipperContainer;
import com.wynntils.models.containers.containers.TradeMarketContainer;
import com.wynntils.models.containers.containers.TradeMarketFiltersContainer;
import com.wynntils.models.containers.containers.TradeMarketSellContainer;
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
import com.wynntils.utils.mc.McUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Map;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.INVENTORY)
public class ImprovedInventorySyncFeature extends Feature {
    // Generic strategy safe for anything
    private static final InventoryResyncStrategy GENERIC_RESYNC_STRATEGY = InventoryResyncStrategy.chain(
            ClickPlaceholderSlotResyncStrategy.INSTANCE, EmptySwapContentBookResyncStrategy.INSTANCE);
    // For the player inventory
    private static final InventoryResyncStrategy PLAYER_INV_RESYNC_STRATEGY = InventoryResyncStrategy.chain(
            EmptySwapContentBookResyncStrategy.INSTANCE,
            IngredientPouchSwapContentBookResyncStrategy.INSTANCE,
            SwapNonEmptySlotResyncStrategy.INSTANCE);
    // For reward containers like loot chests and daily rewards
    private static final InventoryResyncStrategy REWARD_RESYNC_STRATEGY = InventoryResyncStrategy.chain(
            EmptySwapContentBookResyncStrategy.INSTANCE, SwapNonEmptySlotResyncStrategy.INSTANCE);
    // For "click-to-insert" UIs like blacksmiths and item identifiers
    private static final InventoryResyncStrategy CLICK_TO_INSERT_RESYNC_STRATEGY = GENERIC_RESYNC_STRATEGY;
    // For bank-like UIs like player banks and housing containers
    private static final InventoryResyncStrategy BANK_RESYNC_STRATEGY = GENERIC_RESYNC_STRATEGY;
    // For UIs where the player inventory is not visible or not interactible
    private static final InventoryResyncStrategy UI_RESYNC_STRATEGY = GENERIC_RESYNC_STRATEGY;

    private static final Map<Class<? extends Container>, InventoryResyncStrategy> RESYNC_STRATEGY_TABLE = Map.ofEntries(
            Map.entry(AbilityTreeContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(AbilityTreeResetContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(AccountBankContainer.class, BANK_RESYNC_STRATEGY),
            Map.entry(AspectsContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(BookshelfContainer.class, BANK_RESYNC_STRATEGY),
            Map.entry(ChallengeRewardContainer.class, REWARD_RESYNC_STRATEGY),
            Map.entry(CharacterBankContainer.class, BANK_RESYNC_STRATEGY),
            Map.entry(CharacterInfoContainer.class, GENERIC_RESYNC_STRATEGY),
            Map.entry(ContentBookContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(CraftingStationContainer.class, CLICK_TO_INSERT_RESYNC_STRATEGY),
            Map.entry(DailyRewardContainer.class, REWARD_RESYNC_STRATEGY),
            Map.entry(EventContainer.class, REWARD_RESYNC_STRATEGY),
            Map.entry(FlyingChestContainer.class, REWARD_RESYNC_STRATEGY),
            Map.entry(GuildBankContainer.class, BANK_RESYNC_STRATEGY),
            Map.entry(GuildManagementContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(GuildMemberListContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(GuildTerritoriesContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(HousingJukeboxContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(HousingListContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(IngredientPouchContainer.class, PLAYER_INV_RESYNC_STRATEGY),
            Map.entry(InventoryContainer.class, PLAYER_INV_RESYNC_STRATEGY),
            Map.entry(IslandBlockBankContainer.class, BANK_RESYNC_STRATEGY),
            Map.entry(JukeboxContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(LobbyContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(LootChestContainer.class, REWARD_RESYNC_STRATEGY),
            Map.entry(MiscBucketContainer.class, BANK_RESYNC_STRATEGY),
            Map.entry(ObjectiveRewardContainer.class, REWARD_RESYNC_STRATEGY),
            Map.entry(PersonalBlockBankContainer.class, BANK_RESYNC_STRATEGY),
            Map.entry(PetMenuContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(SeaskipperContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(TradeMarketContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(TradeMarketFiltersContainer.class, UI_RESYNC_STRATEGY),
            Map.entry(TradeMarketSellContainer.class, UI_RESYNC_STRATEGY));

    @Persisted
    public Config<Boolean> forceSync = new Config<>(false);

    @SubscribeEvent
    public void onDesyncableContainerClick(DesyncableContainerClickEvent event) {
        if (event.getResyncStrategy() != null) {
            event.setShouldResync(true);
            return;
        }

        InventoryResyncStrategy strategy = getResyncStrategy(event.getContainerMenu());
        if (strategy == null) return;

        event.setShouldResync(true);
        event.setResyncStrategy(strategy);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onContainerClick(ContainerClickEvent event) {
        if (!forceSync.get() || getResyncStrategy(event.getContainerMenu()) == null) return;
        event.setCanceled(true);
        AbstractContainerMenu menu = event.getContainerMenu();

        // Store old inventory state
        ItemStack oldHeld = menu.getCarried().copy();
        ItemStack[] oldItems = new ItemStack[menu.slots.size()];
        for (int i = 0; i < oldItems.length; i++) {
            oldItems[i] = menu.slots.get(i).getItem().copy();
        }

        // Perform click
        menu.clicked(event.getSlotNum(), event.getMouseButton(), event.getClickType(), McUtils.player());

        // Detect changes
        Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < oldItems.length; i++) {
            ItemStack newItem = menu.slots.get(i).getItem();
            if (!ItemStack.matches(oldItems[i], newItem)) {
                changedSlots.put(i, newItem.copy());
            }
        }

        // Send click packet
        McUtils.sendPacket(new ServerboundContainerClickPacket(
                menu.containerId,
                menu.getStateId(),
                event.getSlotNum(),
                event.getMouseButton(),
                event.getClickType(),
                menu.getCarried().copy(),
                changedSlots));

        // Restore previous inventory state in expectation of an update from the server (see InventoryHandler)
        menu.initializeContents(menu.getStateId(), Arrays.asList(oldItems), oldHeld);
    }

    private InventoryResyncStrategy getResyncStrategy(AbstractContainerMenu menu) {
        Container container = Models.Container.getCurrentContainer();
        if (container == null || container.getContainerId() != menu.containerId) return null;

        return RESYNC_STRATEGY_TABLE.get(container.getClass());
    }
}
