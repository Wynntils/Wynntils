/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.webapi.request.Request;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class UpdateReminderFeature extends UserFeature {
    private static final String CI_LINK =
            "https://ci.wynntils.com/job/Artemis/lastSuccessfulBuild/api/json?tree=number";
    private static final String DOWNLOAD_LINK = "https://ci.wynntils.com/job/Artemis";

    private boolean firstJoin = true;

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldStateManager.State.NOT_CONNECTED) {
            firstJoin = false;
            return;
        }

        if (event.getNewState() != WorldStateManager.State.WORLD || !firstJoin) return;

        firstJoin = false;

        Request versionFetch = new RequestBuilder(CI_LINK, "latest_build")
                .handleJsonObject((jsonObject) -> {
                    int buildNumber = jsonObject.getAsJsonPrimitive("number").getAsInt();

                    if (buildNumber != WynntilsMod.getBuildNumber()) {
                        TextComponent clickable = new TextComponent("here.");
                        clickable.setStyle(clickable
                                .getStyle()
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DOWNLOAD_LINK))
                                .withUnderlined(true)
                                .withBold(true));
                        McUtils.sendMessageToClient(new TextComponent("[Wynntils/Artemis]: Build " + buildNumber
                                        + " is the latest version, but you are using build "
                                        + WynntilsMod.getBuildNumber() + ". Please consider updating ")
                                .append(clickable)
                                .append(
                                        new TextComponent(
                                                "\nPlease note that Artemis is in alpha, and newer builds might introduce bugs."))
                                .withStyle(ChatFormatting.GREEN));
                    }

                    return true;
                })
                .build();

        RequestHandler handler = new RequestHandler();

        handler.addAndDispatch(versionFetch, true);
    }
}
