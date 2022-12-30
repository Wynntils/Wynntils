/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.wynntils.core.features.DebugFeature;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.model.item.ItemModel;
import com.wynntils.model.item.WynnItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemDebugTooltipFeature extends DebugFeature {
    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        Optional<WynnItem> wynnItemOpt = ItemModel.getWynnItem(event.getItemStack());
        if (wynnItemOpt.isEmpty()) return;
        WynnItem wynnItem = wynnItemOpt.get();

        List<Component> tooltips = new ArrayList<>(event.getTooltips());
        tooltips.addAll(getTooltipAddon(wynnItem));
        event.setTooltips(tooltips);
    }

    private List<Component> getTooltipAddon(WynnItem wynnItem) {
        List<Component> addon = new ArrayList<>();

        addon.add(Component.literal("Wynn Item Type: ")
                .withStyle(ChatFormatting.DARK_GREEN)
                .append(Component.literal(wynnItem.toString()).withStyle(ChatFormatting.GREEN)));

        return addon;
    }
}
