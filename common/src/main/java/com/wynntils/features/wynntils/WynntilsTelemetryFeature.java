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
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynntilsKeybindsFont;
import com.wynntils.mc.event.KeyInputEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.OptionalBoolean;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.WYNNTILS)
public class WynntilsTelemetryFeature extends Feature {
    private static final int TELEMETRY_PROMPT_DELAY_LAUNCHES = 3;
    private static final long TELEMETRY_PROMPT_DISPLAY_TIME = 15000L;
    private static final long TELEMETRY_CONFIRMATION_DISPLAY_TIME = 1000L;
    private static final int TELEMETRY_ENABLE_KEY = GLFW.GLFW_KEY_LEFT_BRACKET;
    private static final int TELEMETRY_DISABLE_KEY = GLFW.GLFW_KEY_RIGHT_BRACKET;

    @Persisted
    private final Config<OptionalBoolean> crashReports = new Config<>(OptionalBoolean.NULL);

    @Persisted
    private final Storage<Integer> launchCount = new Storage<>(0);

    private long promptExpire = 0L;
    private SystemToast telemetryPromptToast = null;

    public WynntilsTelemetryFeature() {
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
        if (event.getNewState() == WorldState.NOT_CONNECTED) {
            clearPromptToast();
            return;
        }

        if (event.getNewState() != WorldState.WORLD) return;
        if (crashReports.get() != OptionalBoolean.NULL) return;
        if (launchCount.get() <= TELEMETRY_PROMPT_DELAY_LAUNCHES) return;

        MutableComponent toastMessage = Component.empty()
                .append(Component.translatable("feature.wynntils.wynntilsTelemetry.toastMessage1"))
                .append(WynnFont.asFont("key_left_bracket", WynntilsKeybindsFont.class))
                .append(Component.translatable("feature.wynntils.wynntilsTelemetry.toastMessage2"))
                .append(WynnFont.asFont("key_right_bracket", WynntilsKeybindsFont.class))
                .append(Component.translatable("feature.wynntils.wynntilsTelemetry.toastMessage3"));

        clearPromptToast();
        telemetryPromptToast = new SystemToast(
                new SystemToast.SystemToastId(TELEMETRY_PROMPT_DISPLAY_TIME),
                Component.literal(this.getTranslatedName()),
                toastMessage);
        McUtils.mc().getToastManager().addToast(telemetryPromptToast);
        promptExpire = System.currentTimeMillis() + TELEMETRY_PROMPT_DISPLAY_TIME;
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (McUtils.screen() != null) return;
        if (crashReports.get() != OptionalBoolean.NULL) return;
        if (!isPromptActive()) return;

        if (event.getKey() == TELEMETRY_ENABLE_KEY) {
            setCrashReportsPreference(OptionalBoolean.TRUE);
        } else if (event.getKey() == TELEMETRY_DISABLE_KEY) {
            setCrashReportsPreference(OptionalBoolean.FALSE);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (telemetryPromptToast == null) return;
        if (System.currentTimeMillis() <= promptExpire) return;

        clearPromptToast();
    }

    private boolean isPromptActive() {
        if (telemetryPromptToast == null) return false;
        if (System.currentTimeMillis() <= promptExpire) return true;

        clearPromptToast();
        return false;
    }

    private void setCrashReportsPreference(OptionalBoolean value) {
        crashReports.setValue(value);
        crashReports.touched();

        if (telemetryPromptToast != null) {
            String translationKey = value == OptionalBoolean.TRUE
                    ? "feature.wynntils.wynntilsTelemetry.toastEnabled"
                    : "feature.wynntils.wynntilsTelemetry.toastDisabled";
            telemetryPromptToast.reset(
                    Component.literal(this.getTranslatedName()), Component.translatable(translationKey));
            promptExpire = System.currentTimeMillis() + TELEMETRY_CONFIRMATION_DISPLAY_TIME;
        } else {
            clearPromptToast();
        }
    }

    private void clearPromptToast() {
        if (telemetryPromptToast != null) {
            telemetryPromptToast.forceHide();
            telemetryPromptToast = null;
        }

        promptExpire = 0L;
    }
}
