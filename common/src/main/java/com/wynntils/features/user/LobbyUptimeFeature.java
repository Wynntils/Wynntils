/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.utils.LoreUtils;
import com.wynntils.wynn.handleditems.items.gui.ServerItem;
import com.wynntils.wynn.objects.profiles.ServerProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        Optional<ServerItem> serverItemOpt = Models.Item.asWynnItem(event.getItemStack(), ServerItem.class);
        if (serverItemOpt.isEmpty()) return;

        List<Component> tooltips = LoreUtils.appendTooltip(
                event.getItemStack(), event.getTooltips(), getTooltipAddon(serverItemOpt.get()));
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
