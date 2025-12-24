/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.models.items.items.gui.ServerItem;
import com.wynntils.models.worlds.profile.ServerProfile;
import com.wynntils.utils.mc.LoreUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class LobbyUptimeFeature extends Feature {
    public LobbyUptimeFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.NEW_PLAYER, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
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

        String serverId = serverItem.getRegion().name() + serverItem.getServerId();
        ServerProfile serverProfile = Models.ServerList.getServer(serverId);
        String uptimeString = serverProfile == null ? "Unknown" : serverProfile.getUptime();

        addon.add(Component.literal("Uptime: ")
                .withStyle(ChatFormatting.DARK_GREEN)
                .append(Component.literal(uptimeString).withStyle(ChatFormatting.GREEN)));

        return addon;
    }
}
