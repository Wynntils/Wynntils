/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.wynn.handleditems.ItemModel;
import com.wynntils.wynn.handleditems.items.gui.ServerItem;
import com.wynntils.wynn.objects.profiles.ServerProfile;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LobbyUptimeFeature extends UserFeature {
    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.ServerList);
    }

    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        var wynnItemOpt = ItemModel.getWynnItem(event.getItemStack());
        if (wynnItemOpt.isEmpty()) return;
        if (!(wynnItemOpt.get() instanceof ServerItem serverItem)) return;

        List<Component> tooltips = new ArrayList<>(event.getTooltips());
        tooltips.addAll(getTooltipAddon(serverItem));
        event.setTooltips(tooltips);
    }

    private List<Component> getTooltipAddon(ServerItem serverItem) {
        List<Component> addon = new ArrayList<>();

        String serverId = "WC" + serverItem.getServerId();
        ServerProfile serverProfile = Models.ServerList.getServer(serverId);
        String uptimeString = serverProfile == null ? "Unknown" : serverProfile.getUptime();

        addon.add(Component.literal("Uptime: ")
                .withStyle(ChatFormatting.DARK_GREEN)
                .append(Component.literal(uptimeString).withStyle(ChatFormatting.GREEN)));

        return addon;
    }
}
