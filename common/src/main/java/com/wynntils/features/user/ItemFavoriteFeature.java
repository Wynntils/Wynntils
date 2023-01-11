/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.TypeOverride;
import com.wynntils.core.features.UserFeature;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.handleditems.WynnItem;
import com.wynntils.wynn.handleditems.WynnItemCache;
import com.wynntils.wynn.utils.ContainerUtils;
import com.wynntils.wynn.utils.WynnUtils;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemFavoriteFeature extends UserFeature {
    public static ItemFavoriteFeature INSTANCE;

    // This should really move to FavoritesModel, but for now, models cannot have configs
    @Config(visible = false)
    public Set<String> favoriteItems = new HashSet<>();

    @TypeOverride
    private final Type favoriteItemsType = new TypeToken<Set<String>>() {}.getType();

    @SubscribeEvent
    public void onChestCloseAttempt(ContainerCloseEvent.Pre e) {
        if (!WynnUtils.onWorld()) return;
        if (!Managers.Container.isLootOrRewardChest(McUtils.mc().screen)) return;

        NonNullList<ItemStack> items = ContainerUtils.getItems(McUtils.mc().screen);
        for (int i = 0; i < 27; i++) {
            ItemStack stack = items.get(i);

            if (isFavorited(stack)) {
                McUtils.sendMessageToClient(Component.translatable("feature.wynntils.itemFavorite.closingBlocked")
                        .withStyle(ChatFormatting.RED));
                e.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Post event) {
        ItemStack itemStack = event.getSlot().getItem();

        if (isFavorited(itemStack)) {
            renderFavoriteItem(event);
        }
    }

    private boolean isFavorited(ItemStack itemStack) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return false;

        WynnItem wynnItem = wynnItemOpt.get();
        Boolean result = wynnItem.getCache()
                .getOrCalculate(
                        WynnItemCache.FAVORITE_KEY, () -> Managers.Favorites.calculateFavorite(itemStack, wynnItem));
        return result;
    }

    private static void renderFavoriteItem(SlotRenderEvent.Post event) {
        RenderUtils.drawScalingTexturedRect(
                new PoseStack(),
                Texture.FAVORITE.resource(),
                event.getSlot().x + 10,
                event.getSlot().y + 8,
                400,
                9,
                9,
                Texture.FAVORITE.width(),
                Texture.FAVORITE.height());
    }
}
