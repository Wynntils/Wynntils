/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.models.mount.event.MountEvent;
import com.wynntils.models.mount.type.MountChoice;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.overlays.MountEnergyOverlay;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.RenderElementType;
import net.minecraft.client.CameraType;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class MountKeybindFeature extends Feature {
    private static final Identifier MOUNT_WHISTLE_ID = Identifier.fromNamespaceAndPath("wynntils", "mount.whistle");
    private static final SoundEvent MOUNT_WHISTLE_SOUND = SoundEvent.createVariableRangeEvent(MOUNT_WHISTLE_ID);

    @RegisterKeyBind
    private final KeyBind rideMountKeybind = KeyBindDefinition.RIDE_MOUNT.create(this::tryRideMount);

    @RegisterOverlay(renderType = RenderElementType.ACTION_BAR)
    private final MountEnergyOverlay mountEnergyOverlay = new MountEnergyOverlay();

    @Persisted
    private final Config<Boolean> playWhistle = new Config<>(true);

    @Persisted
    private final Config<MountChoice> mountChoice = new Config<>(MountChoice.FIRST);

    @Persisted
    private final Config<Boolean> switchToThirdPersonOnMount = new Config<>(false);

    private CameraType prevCameraType = null;

    public MountKeybindFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER, ConfigProfile.LITE)
                .build());
    }

    @SubscribeEvent
    public void onMountSummon(MountEvent.Summon event) {
        playSoundIfEnabled();
    }

    @SubscribeEvent
    public void onMountMount(MountEvent.Mount event) {
        if (!switchToThirdPersonOnMount.get()) return;

        prevCameraType = McUtils.options().getCameraType();
        McUtils.options().setCameraType(CameraType.THIRD_PERSON_BACK);
    }

    @SubscribeEvent
    public void onMountDismount(MountEvent.Dismount event) {
        if (!switchToThirdPersonOnMount.get()) return;

        if (prevCameraType != null) {
            restoreCamera();
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (switchToThirdPersonOnMount.get() && prevCameraType != null) {
            restoreCamera();
        }
    }

    private void tryRideMount() {
        Models.Mount.tryRideMount(mountChoice.get());
    }

    private void restoreCamera() {
        McUtils.options().setCameraType(prevCameraType);
        prevCameraType = null;
    }

    private void playSoundIfEnabled() {
        if (playWhistle.get()) {
            // TODO: Add unique sounds for each mount type
            McUtils.playSoundAmbient(MOUNT_WHISTLE_SOUND);
        }
    }
}
