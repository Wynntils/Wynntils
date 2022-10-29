/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.UpdateManager;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class UpdateReminderFeature extends UserFeature {

    private boolean firstJoin = true;

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldStateManager.State.NOT_CONNECTED) {
            firstJoin = false;
            return;
        }

        if (event.getNewState() != WorldStateManager.State.WORLD || !firstJoin) return;

        firstJoin = false;

        UpdateManager.getLatestBuild().whenCompleteAsync((buildNumber, throwable) -> {
            if (buildNumber != WynntilsMod.getBuildNumber()) {
                TextComponent clickable = new TextComponent("here.");
                clickable.setStyle(clickable
                        .getStyle()
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/update"))
                        .withUnderlined(true)
                        .withBold(true));
                McUtils.sendMessageToClient(new TextComponent("[Wynntils/Artemis]: Build " + buildNumber
                                + " is the latest version, but you are using build "
                                + WynntilsMod.getBuildNumber() + ". Please consider updating by clicking ")
                        .append(clickable)
                        .append(new TextComponent(
                                "\nPlease note that Artemis is in alpha, and newer builds might introduce bugs."))
                        .withStyle(ChatFormatting.GREEN));
            }
        });
    }
}
