/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item;

import com.wynntils.core.components.Models;
import com.wynntils.wynn.objects.profiles.ServerProfile;
import com.wynntils.wynn.utils.WynnItemMatchers;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ServerItemStack extends WynnItemStack {
    private List<Component> tooltip;

    public ServerItemStack(ItemStack stack) {
        super(stack);
    }

    @Override
    public void init() {
        Matcher matcher = WynnItemMatchers.serverItemMatcher(this.getHoverName());

        // Need to run matches to calculate group
        if (!matcher.matches()) {
            throw new IllegalStateException("ServerItem did not match regex.");
        }

        int serverId = Integer.parseInt(matcher.group(1));

        extendTooltip(serverId);
    }

    private void extendTooltip(int id) {
        List<Component> newTooltip = new ArrayList<>(getOriginalTooltip());

        String serverId = "WC" + id;
        ServerProfile serverProfile = Models.ServerList.getServer(serverId);
        String uptimeString = serverProfile == null ? "Unknown" : serverProfile.getUptime();

        newTooltip.add(Component.literal("Uptime: ")
                .withStyle(ChatFormatting.DARK_GREEN)
                .append(Component.literal(uptimeString).withStyle(ChatFormatting.GREEN)));
        tooltip = newTooltip;
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag isAdvanced) {
        if (tooltip == null) {
            return getOriginalTooltip();
        }

        return tooltip;
    }
}
