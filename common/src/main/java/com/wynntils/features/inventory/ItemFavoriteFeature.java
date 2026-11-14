/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.InventoryMouseClickedEvent;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.containers.LootChestModel;
import com.wynntils.models.containers.containers.reward.RewardContainer;
import com.wynntils.models.containers.type.FullscreenContainerProperty;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.screens.favorites.FavoriteCategoryPopup;
import com.wynntils.services.favorites.FavoriteRenderer;
import com.wynntils.services.favorites.type.FavoriteCategory;
import com.wynntils.services.favorites.type.LootFilter;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class ItemFavoriteFeature extends Feature {
    @RegisterKeyBind
    private final KeyBind itemFavoriteKeyBind =
            KeyBindDefinition.TOGGLE_FAVORITE.create(this::tryChangeFavoriteStateOnHoveredSlot);

    @Persisted
    private final Config<Boolean> allowFavoritingAllItems = new Config<>(false);

    // These should really move to FavoritesService, but they have to stay configs to keep the existing favorites
    @Persisted
    public final HiddenConfig<List<LootFilter>> lootFilters =
            new HiddenConfig<>(new ArrayList<>(List.of(new LootFilter(LootFilter.DEFAULT_NAME))));

    @Persisted
    public final HiddenConfig<String> activeLootFilter = new HiddenConfig<>(LootFilter.DEFAULT_NAME);

    private int lootChestCloseOverrideCounter = 0;
    private FavoriteCategoryPopup categoryPopup = null;
    private int foundSoundIndex = Integer.MAX_VALUE;
    private int playedSoundIndex = Integer.MAX_VALUE;

    public ItemFavoriteFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent
    public void onChestCloseAttempt(ContainerCloseEvent.Pre e) {
        if (!Models.WorldState.onWorld()) return;
        if (!(Models.Container.getCurrentContainer() instanceof RewardContainer)) return;

        // The strictest category in the chest decides
        int closeOverride = 0;
        NonNullList<ItemStack> items = ContainerUtils.getItems(McUtils.screen());
        for (int i = 0; i < 27; i++) {
            FavoriteCategory category = getCategory(items.get(i));
            if (category == null) continue;

            if (category.getLootChestCloseOverride() == FavoriteCategory.NEVER_CLOSE) {
                closeOverride = FavoriteCategory.NEVER_CLOSE;
                break;
            }

            closeOverride = Math.max(closeOverride, category.getLootChestCloseOverride());
        }

        if (closeOverride == 0) return;

        if (closeOverride == FavoriteCategory.NEVER_CLOSE) {
            McUtils.sendErrorToClient(I18n.get("feature.wynntils.itemFavorite.closingBlocked"));
            e.setCanceled(true);
            return;
        }

        lootChestCloseOverrideCounter++;
        if (lootChestCloseOverrideCounter >= closeOverride) return;

        McUtils.sendErrorToClient(I18n.get(
                "feature.wynntils.itemFavorite.closingBlockedOverride", closeOverride - lootChestCloseOverrideCounter));
        e.setCanceled(true);
    }

    @SubscribeEvent
    public void onScreenOpen(ScreenOpenedEvent.Post event) {
        lootChestCloseOverrideCounter = 0;
        categoryPopup = null;
        foundSoundIndex = Integer.MAX_VALUE;
        playedSoundIndex = Integer.MAX_VALUE;
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent.Post event) {
        if (!(Models.Container.getCurrentContainer() instanceof RewardContainer rewardContainer)) return;
        if (event.getContainerId() != rewardContainer.getContainerId()) return;
        if (event.getSlot() >= LootChestModel.LOOT_CHEST_ITEM_COUNT) return;

        FavoriteCategory category = getCategory(event.getItemStack());
        if (category == null || category.getSound().isEmpty()) return;

        foundSoundIndex =
                Math.min(foundSoundIndex, Services.Favorites.getCategories().indexOf(category));
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (foundSoundIndex >= playedSoundIndex) return;

        playedSoundIndex = foundSoundIndex;
        Services.Favorites.playSound(Services.Favorites.getCategories().get(playedSoundIndex));
    }

    @SubscribeEvent
    public void onRenderSlotPre(SlotRenderEvent.Pre event) {
        if (Models.Container.getCurrentContainer() instanceof FullscreenContainerProperty) return;

        FavoriteCategory category = getCategory(event.getSlot().getItem());
        if (category == null) return;

        FavoriteRenderer.renderBackground(event.getGuiGraphics(), category, event.getSlot().x, event.getSlot().y);
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Post event) {
        if (Models.Container.getCurrentContainer() instanceof FullscreenContainerProperty) return;

        FavoriteCategory category = getCategory(event.getSlot().getItem());
        if (category == null) return;

        FavoriteRenderer.renderForeground(event.getGuiGraphics(), category, event.getSlot().x, event.getSlot().y);
    }

    @SubscribeEvent
    public void onContainerRender(ContainerRenderEvent event) {
        if (categoryPopup == null) return;

        categoryPopup.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
    }

    @SubscribeEvent
    public void onInventoryMouseClicked(InventoryMouseClickedEvent event) {
        if (categoryPopup == null) return;

        categoryPopup.mouseClicked(event.getMouseX(), event.getMouseY());
        categoryPopup = null;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        if (categoryPopup == null) return;

        event.setCanceled(true);
    }

    private FavoriteCategory getCategory(ItemStack itemStack) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return null;

        WynnItem wynnItem = wynnItemOpt.get();
        int currentRevision = Services.Favorites.getRevision();
        CachedCategory cached = wynnItem.getData().get(WynnItemData.FAVORITE_KEY);
        if (cached != null && cached.revision() == currentRevision) {
            // The cache is up to date
            return cached.category();
        }

        // Cache is missing or outdated
        FavoriteCategory category = Services.Favorites.calculateCategory(itemStack, wynnItem);
        wynnItem.getData().store(WynnItemData.FAVORITE_KEY, new CachedCategory(currentRevision, category));
        return category;
    }

    private void tryChangeFavoriteStateOnHoveredSlot(Slot hoveredSlot) {
        if (categoryPopup != null) {
            categoryPopup = null;
            return;
        }

        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        String itemName = null;

        Optional<NamedItemProperty> namedItemPropertyOpt =
                Models.Item.asWynnItemProperty(hoveredSlot.getItem(), NamedItemProperty.class);
        if (namedItemPropertyOpt.isPresent()) {
            itemName = namedItemPropertyOpt.get().getName();
        } else if (allowFavoritingAllItems.get()) {
            itemName = WynnUtils.stripItemNameMarkers(WynnUtils.normalizeBadString(
                    StyledText.fromComponent((hoveredSlot.getItem().getHoverName()))
                            .getStringWithoutFormatting()));
        }

        if (itemName == null || itemName.isBlank()) return;

        if (Services.Favorites.getCategories().size() > 1
                && McUtils.screen() instanceof AbstractContainerScreen<?> containerScreen) {
            categoryPopup = FavoriteCategoryPopup.forItem(
                    itemName, containerScreen.leftPos + hoveredSlot.x + 18, containerScreen.topPos + hoveredSlot.y);
            return;
        }

        Services.Favorites.toggleFavorite(itemName);
    }

    private record CachedCategory(int revision, FavoriteCategory category) {}
}
