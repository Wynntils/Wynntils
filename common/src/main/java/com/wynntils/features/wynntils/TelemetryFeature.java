/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.mod.event.WynntilsCrashEvent;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.utils.JsonUtils;
import java.util.Locale;
import java.util.Map;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;

@ConfigCategory(Category.WYNNTILS)
public class TelemetryFeature extends UserFeature {
    @Config
    public boolean crashReports = true;

    @SubscribeEvent
    public void onCrash(WynntilsCrashEvent event) {
        String title = "Crashed " + event.getType().toString().toLowerCase(Locale.ROOT) + ": " + event.getName() + "\n";
        String trace = ExceptionUtils.getStackTrace(event.getThrowable());

        ApiResponse apiResponse =
                Managers.Net.callApi(UrlId.API_ATHENA_TELEMETRY_CRASH, Map.of("trace", title + trace));

        apiResponse.handleJsonObject(json -> {
            String response = JsonUtils.getNullableJsonString(json, "message");
            String hash = JsonUtils.getNullableJsonString(json, "hash");
            if (response.equals("Crash report logged successfully.")) {
                WynntilsMod.info("Crash reported to Athena as " + hash);
            } else {
                WynntilsMod.warn("Failed to report crash reported to Athena: " + response);
            }
        });
    }
}
