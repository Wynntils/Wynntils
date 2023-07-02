/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.mod.event.WynncraftConnectionEvent;
import com.wynntils.core.storage.Storage;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.UNCATEGORIZED)
public class SilencerFeature extends Feature {
    @RegisterConfig
    public final Config<Double> silencerVolume = new Config<>(0.01);

    @RegisterKeyBind
    private final KeyBind silencerKeyBind =
            new KeyBind("Toggle Silencer", GLFW.GLFW_KEY_UNKNOWN, true, this::toggleSilencer);

    private final Storage<Double> lastVolume = new Storage<>(1.0);

    private boolean isSilencerEnabled = false;

    @SubscribeEvent
    public void onDisconnect(WynncraftConnectionEvent.Disconnected event) {
        if (isSilencerEnabled) {
            resetVolume();
            isSilencerEnabled = false;
        }
    }

    @SubscribeEvent
    public void onTitleScreenInit(TitleScreenInitEvent.Pre event) {
        resetVolume();
    }

    private void toggleSilencer() {
        if (!Models.WorldState.onWorld()) return;
        isSilencerEnabled = !isSilencerEnabled;

        if (isSilencerEnabled) {
            enableSilencer();
            Managers.Notification.queueMessage(
                    Component.translatable("feature.wynntils.silencer.enabled").withStyle(ChatFormatting.GREEN));
        } else {
            resetVolume();
            Managers.Notification.queueMessage(
                    Component.translatable("feature.wynntils.silencer.disabled").withStyle(ChatFormatting.RED));
        }
    }

    private void resetVolume() {
        McUtils.options().getSoundSourceOptionInstance(SoundSource.MASTER).set(lastVolume.get());
    }

    private void enableSilencer() {
        lastVolume.store(McUtils.options()
                .getSoundSourceOptionInstance(SoundSource.MASTER)
                .get());
        McUtils.options().getSoundSourceOptionInstance(SoundSource.MASTER).set(silencerVolume.get());
    }
}
