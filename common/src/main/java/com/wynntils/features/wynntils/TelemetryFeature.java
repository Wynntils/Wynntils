/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.mod.event.WynntilsCrashEvent;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.OptionalBoolean;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;

@ConfigCategory(Category.WYNNTILS)
public class TelemetryFeature extends Feature {
    private static final int TELEMETRY_PROMPT_DELAY_LAUNCHES = 3;

    @Persisted
    private final Config<OptionalBoolean> crashReports = new Config<>(OptionalBoolean.NULL);

    @Persisted
    private final Storage<Integer> launchCount = new Storage<>(0);

    public TelemetryFeature() {
        super(ProfileDefault.onlyDefault());
    }

    @Override
    public void onStorageLoad(Storage<?> storage) {
        if (storage != launchCount) return;

        launchCount.store(launchCount.get() + 1);
    }

    @SubscribeEvent
    public void onCrash(WynntilsCrashEvent event) {
        if (crashReports.get() != OptionalBoolean.TRUE) return;
        // Only send telemetry for released versions
        if (WynntilsMod.isDevelopmentEnvironment()) return;
        if (WynntilsMod.getVersion().contains("SNAPSHOT")) return;

        String title = "Crashed " + event.getType().toString().toLowerCase(Locale.ROOT) + ": " + event.getName() + "\n";
        String trace = ExceptionUtils.getStackTrace(event.getThrowable());

        ApiResponse apiResponse =
                Managers.Net.callApi(UrlId.API_ATHENA_TELEMETRY_CRASH, Map.of("trace", title + trace));

        apiResponse.handleJsonObject(json -> {
            String response = JsonUtils.getNullableJsonString(json, "message");
            String hash = JsonUtils.getNullableJsonString(json, "hash");
            if (response != null && response.equals("Crash report logged successfully.")) {
                WynntilsMod.info("Crash reported to Athena as " + hash);
            } else {
                WynntilsMod.warn("Failed to report crash reported to Athena: " + response);
            }
        });
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent event) {
        System.out.println("toast evt)");
        if (event.getNewState() != WorldState.WORLD) return;
        if (crashReports.get() != OptionalBoolean.NULL) return;
        if (launchCount.get() <= TELEMETRY_PROMPT_DELAY_LAUNCHES) return;

        System.out.println("trying to make toast");
        displayToast(
                Component.literal(getTranslatedName()),
                Component.literal(
                        "Anonymous crash reports help fix bugs. No personal info is sent. Set Telemetry > Crash Reports in Wynntils settings."));
    }

    private void displayToast(Component title, Component message) {
        McUtils.mc().getToastManager().addToast(new SystemToast(new SystemToast.SystemToastId(10000L), title, message));
    }
}
