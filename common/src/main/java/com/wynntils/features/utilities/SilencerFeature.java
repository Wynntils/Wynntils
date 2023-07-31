/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.mod.event.WynncraftConnectionEvent;
import com.wynntils.core.storage.RegisterStorage;
import com.wynntils.core.storage.Storage;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/**
 * "Silencer" modifies the game's masterVolume setting when toggled.
 * This is intended as a "session-only" feature, so the modification persists until "Silencer" -
 * is either toggled again or the "session ends" (i.e. disconnect or game exit).
 *
 * <p>The volume is restored to the original state on "game boot".
 * Note that the initial value of isSilencerEnabled is 'false' and is not explicitly set on "game boot".
 */
@ConfigCategory(Category.UTILITIES)
public class SilencerFeature extends Feature {
    @RegisterConfig
    public final Config<Double> silencerVolume = new Config<>(0.01);

    @RegisterKeyBind
    private final KeyBind silencerKeyBind =
            new KeyBind("Toggle Silencer", GLFW.GLFW_KEY_UNKNOWN, true, this::toggleSilencer);

    @RegisterStorage
    private final Storage<Double> originalVolume = new Storage<>(1.0);

    private boolean isSilencerEnabled = false;

    private boolean firstTitleScreenInit = true;

    @SubscribeEvent
    public void onDisconnect(WynncraftConnectionEvent.Disconnected event) {
        if (isSilencerEnabled) {
            restoreOriginalVolume();
            isSilencerEnabled = false;
        }
    }

    @SubscribeEvent
    public void onTitleScreenInit(TitleScreenInitEvent.Pre event) {
        if (!firstTitleScreenInit) return;
        if (!masterVolume().get().equals(silencerVolume.get())) return;
        // If the previous game crashed with the silencer enabled, restore normal volume now
        restoreOriginalVolume();
        firstTitleScreenInit = false;
    }

    private void toggleSilencer() {
        if (!Models.WorldState.onWorld()) return;
        isSilencerEnabled = !isSilencerEnabled;

        if (isSilencerEnabled) {
            enableSilencer();
            Managers.Notification.queueMessage(
                    Component.translatable("feature.wynntils.silencer.enabled").withStyle(ChatFormatting.GREEN));
        } else {
            restoreOriginalVolume();
            Managers.Notification.queueMessage(
                    Component.translatable("feature.wynntils.silencer.disabled").withStyle(ChatFormatting.RED));
        }
    }

    private void restoreOriginalVolume() {
        masterVolume().set(originalVolume.get());
    }

    private void enableSilencer() {
        originalVolume.store(masterVolume().get());
        masterVolume().set(silencerVolume.get());
    }

    private OptionInstance<Double> masterVolume() {
        return McUtils.options().getSoundSourceOptionInstance(SoundSource.MASTER);
    }
}
