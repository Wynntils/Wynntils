/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.core.webapi.ServerListModel;
import com.wynntils.core.webapi.profiles.ServerProfile;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wynn.model.WorldStateManager;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LobbyUptimeFeature extends UserFeature {
    private static final Pattern SERVER_ITEM_PATTERN = Pattern.compile("§[baec]§lWorld (\\d+)(§3 \\(Recommended\\))?");

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent event) {
        if (WorldStateManager.getCurrentState() != WorldStateManager.State.HUB) return;

        ItemStack item = event.getItemStack();

        replaceLore(item);
    }

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent event) {
        if (WorldStateManager.getCurrentState() != WorldStateManager.State.HUB) return;

        for (ItemStack item : event.getItems()) {
            replaceLore(item);
        }
    }

    private static void replaceLore(ItemStack item) {
        String name = ComponentUtils.getCoded(item.getHoverName());

        Matcher matcher = SERVER_ITEM_PATTERN.matcher(name);

        if (matcher.matches()) {
            String serverId = "WC" + matcher.group(1);
            ServerProfile serverProfile = ServerListModel.getServer(serverId);
            String uptimeString = serverProfile == null ? "Unknown" : serverProfile.getUptime();

            ListTag loreTag = ItemUtils.getLoreTagElseEmpty(item);
            loreTag.add(ItemUtils.toLoreStringTag(
                    ChatFormatting.DARK_GREEN + "Uptime: " + ChatFormatting.GREEN + uptimeString));
            ItemUtils.replaceLore(item, loreTag);
        }
    }

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(ServerListModel.class);
    }
}
