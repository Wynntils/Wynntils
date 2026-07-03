/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.consumers.features.ExternalConfigurationScreen;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.screens.soundtriggers.SoundTriggerManagmentScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.soundtriggers.SoundTrigger;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class SoundTriggersFeature extends Feature implements ExternalConfigurationScreen {
    @Persisted
    private final Config<Float> globalVolumeModifier = new Config<>(100f);

    @Persisted
    private final Storage<List<SoundTrigger>> registeredTriggers = new Storage<>(new ArrayList<>());

    public SoundTriggersFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent
    public void playTriggers(TickEvent event) {
        for (SoundTrigger trigger : registeredTriggers.get()) {
            if (trigger.isEnabled() && trigger.shouldPlay()) {
                ErrorOr<Boolean> shouldPlay = trigger.getControllerFunctionResult();
                ErrorOr<String> identifierRaw = trigger.getIdentifierFunctionResult();
                if (!shouldPlay.hasError() && !identifierRaw.hasError()) {
                    Identifier sound = Identifier.parse(identifierRaw.getValue());
                    McUtils.playSoundMaster(
                            SoundEvent.createVariableRangeEvent(sound),
                            trigger.getVolume() / 100f * Math.max(globalVolumeModifier.get(), 0f) / 100f,
                            trigger.getPitch() / 100f);
                }
            }
        }
    }

    @Override
    public Screen getExternalConfigurationScreen(Screen previousScreen) {
        return SoundTriggerManagmentScreen.screen(previousScreen);
    }

    public Storage<List<SoundTrigger>> getRegisteredTriggers() {
        return registeredTriggers;
    }
}
