/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.screens.changelog.ChangelogScreen;
import com.wynntils.utils.mc.McUtils;
import java.util.Map;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChangelogFeature extends UserFeature {
    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        // if (event.isFirstJoinWorld()) {
        //            ApiResponse response = Managers.Net.callApi(UrlId.API_ATHENA_UPDATE_CHANGELOG, Map.of("version",
        // WynntilsMod.getVersion()));
        ApiResponse response =
                Managers.Net.callApi(UrlId.API_ATHENA_UPDATE_CHANGELOG, Map.of("version", "v0.0.2-alpha.152"));

        response.handleJsonObject(
                jsonObject -> {
                    String changelog = jsonObject.get("changelog").getAsString();

                    Managers.TickScheduler.scheduleNextTick(
                            () -> McUtils.mc().setScreen(ChangelogScreen.create(changelog)));
                },
                throwable -> {
                    WynntilsMod.warn("Could not get update changelog: ", throwable);
                });

        // }
    }
}
