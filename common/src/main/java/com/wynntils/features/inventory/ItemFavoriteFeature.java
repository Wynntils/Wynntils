/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.containers.containers.reward.RewardContainer;
import com.wynntils.models.containers.type.FullscreenContainerProperty;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.INVENTORY)
public class ItemFavoriteFeature extends Feature {
    // This should really move to FavoritesModel, but for now, models cannot have configs

    @RegisterKeyBind
    private final KeyBind itemFavoriteKeyBind = new KeyBind(
            "Favorite/Unfavorite Item", GLFW.GLFW_KEY_UNKNOWN, true, null, this::tryChangeFavoriteStateOnHoveredSlot);

    @Persisted
    private final Config<Boolean> allowFavoritingAllItems = new Config<>(false);

    @Persisted
    public final HiddenConfig<Set<String>> favoriteItems = new HiddenConfig<>(new TreeSet<>());

    @Persisted
    private final Config<Integer> lootChestCloseOverride = new Config<>(3);

    private int lootChestCloseOverrideCounter = 0;

    @SubscribeEvent
    public void onChestCloseAttempt(ContainerCloseEvent.Pre e) {
        if (!Models.WorldState.onWorld()) return;
        if (!(Models.Container.getCurrentContainer() instanceof RewardContainer)) return;

        boolean containsFavorite = false;
        NonNullList<ItemStack> items = ContainerUtils.getItems(McUtils.screen());
        for (int i = 0; i < 27; i++) {
            ItemStack itemStack = items.get(i);

            if (isFavorited(itemStack)) {
                containsFavorite = true;
                break;
            }
        }

        if (!containsFavorite) return;

        if (lootChestCloseOverride.get() == 0) { // never allow user to close
            McUtils.sendErrorToClient(I18n.get("feature.wynntils.itemFavorite.closingBlocked"));
            e.setCanceled(true);
            return;
        }

        lootChestCloseOverrideCounter++;
        if (lootChestCloseOverrideCounter >= lootChestCloseOverride.get()) return;

        McUtils.sendErrorToClient(I18n.get(
                "feature.wynntils.itemFavorite.closingBlockedOverride",
                lootChestCloseOverride.get() - lootChestCloseOverrideCounter));
        e.setCanceled(true);
    }

    @SubscribeEvent
    public void onScreenOpen(ScreenOpenedEvent.Post event) {
        lootChestCloseOverrideCounter = 0;
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.CountPre event) {
        if (Models.Container.getCurrentContainer() instanceof FullscreenContainerProperty) return;

        ItemStack itemStack = event.getSlot().getItem();

        if (isFavorited(itemStack)) {
            renderFavoriteItem(event);
        }
    }

    private boolean isFavorited(ItemStack itemStack) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return false;

        WynnItem wynnItem = wynnItemOpt.get();
        int currentRevision = Services.Favorites.getRevision();
        Integer revision = wynnItem.getData().get(WynnItemData.FAVORITE_KEY);
        if (revision != null && (revision == currentRevision || revision == -currentRevision)) {
            // The cache is up to date; positive value means it is a favorite
            return revision > 0;
        }

        // Cache is missing or outdated
        boolean isFavorite = Services.Favorites.calculateFavorite(itemStack, wynnItem);
        wynnItem.getData().store(WynnItemData.FAVORITE_KEY, isFavorite ? currentRevision : -currentRevision);
        return isFavorite;
    }

    private static void renderFavoriteItem(SlotRenderEvent.CountPre event) {
        BufferedRenderUtils.drawScalingTexturedRect(
                event.getPoseStack(),
                event.getGuiGraphics().bufferSource,
                Texture.FAVORITE_ICON.resource(),
                event.getSlot().x + 10,
                event.getSlot().y,
                200,
                9,
                9,
                Texture.FAVORITE_ICON.width(),
                Texture.FAVORITE_ICON.height());
    }

    private void tryChangeFavoriteStateOnHoveredSlot(Slot hoveredSlot) {
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        String itemName = null;

        Optional<NamedItemProperty> namedItemPropertyOpt =
                Models.Item.asWynnItemProperty(hoveredSlot.getItem(), NamedItemProperty.class);
        if (namedItemPropertyOpt.isPresent()) {
            itemName = namedItemPropertyOpt.get().getName();
        } else if (allowFavoritingAllItems.get()) {
            itemName = WynnUtils.normalizeBadString(
                    StyledText.fromComponent((hoveredSlot.getItem().getHoverName()))
                            .getStringWithoutFormatting());
        }

        if (itemName != null && !itemName.isBlank()) {
            Services.Favorites.toggleFavorite(itemName);
        }
    }
}
