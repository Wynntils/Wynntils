/*
 * Copyright © Wynntils 2021-2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Managers;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.model.item.ItemModel;
import com.wynntils.model.item.gui.SoulPointItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE)
public class SoulPointTimerFeature extends UserFeature {
    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        var wynnItemOpt = ItemModel.getWynnItem(event.getItemStack());
        if (wynnItemOpt.isEmpty()) return;
        if (!(wynnItemOpt.get() instanceof SoulPointItem soulPointItem)) return;

        List<Component> tooltips = new ArrayList<>(event.getTooltips());
        tooltips.addAll(getTooltipAddon());
        event.setTooltips(tooltips);
    }

    private List<Component> getTooltipAddon() {
        List<Component> addon = new ArrayList<>();

        addon.add(Component.literal(" "));

        int rawSecondsUntilSoulPoint = Managers.Character.getTicksToNextSoulPoint() / 20;
        int minutesUntilSoulPoint = rawSecondsUntilSoulPoint / 60;
        int secondsUntilSoulPoint = rawSecondsUntilSoulPoint % 60;

        addon.add(Component.translatable(
                        "feature.wynntils.soulPointTimer.lore",
                        ChatFormatting.WHITE + String.format("%d:%02d", minutesUntilSoulPoint, secondsUntilSoulPoint))
                .withStyle(ChatFormatting.AQUA));

        return addon;
    }
}
