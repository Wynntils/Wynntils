/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Managers;
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
import com.wynntils.mc.event.SetLocalPlayerVehicleEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.type.MountChoice;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.overlays.MountEnergyOverlay;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.MouseUtils;
import com.wynntils.utils.type.RenderElementType;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class MountKeybindFeature extends Feature {
    private static final Identifier MOUNT_WHISTLE_ID = Identifier.fromNamespaceAndPath("wynntils", "mount.whistle");
    private static final SoundEvent MOUNT_WHISTLE_SOUND = SoundEvent.createVariableRangeEvent(MOUNT_WHISTLE_ID);

    // How long we wait before assuming mount failure
    private static final int MOUNT_TIME_TICKS = 10;

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

    private int summonTick = -1;

    public MountKeybindFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER, ConfigProfile.LITE)
                .build());
    }

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        if (!Models.WorldState.onWorld()) return;

        ItemStack itemStack = McUtils.inventory().getSelectedItem();
        Optional<MountItem> mountItemOpt = Models.Item.asWynnItem(itemStack, MountItem.class);
        if (mountItemOpt.isEmpty()) return;
        if (!mountItemOpt.get().isSummonItem()) return;

        playSoundIfEnabled();

        summonTick = McUtils.player().tickCount;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (summonTick == -1) return;

        int currentTick = McUtils.player().tickCount;
        if (currentTick - summonTick > MOUNT_TIME_TICKS) {
            // Assume failed to mount
            summonTick = -1;
        }
    }

    @SubscribeEvent
    public void onVehicleChange(SetLocalPlayerVehicleEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (!switchToThirdPersonOnMount.get()) return;

        if (event.getVehicle() == null && prevCameraType != null) {
            restoreCamera();
        } else {
            prevCameraType = McUtils.options().getCameraType();
            McUtils.options().setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (switchToThirdPersonOnMount.get() && prevCameraType != null) {
            restoreCamera();
        }
    }

    private void tryRideMount() {
        if (!Models.WorldState.onWorld()) return;

        LocalPlayer player = McUtils.player();
        if (player.getVehicle() != null) {
            postMountErrorMessage(RideMountStatus.ALREADY_RIDING);
            return;
        }

        int mountInventorySlot = Models.Mount.findMountSlotNum(mountChoice.get());
        if (mountInventorySlot == -1) {
            postMountErrorMessage(RideMountStatus.NO_MOUNT);
            return;
        }
        if (mountInventorySlot > 8) {
            postMountErrorMessage(RideMountStatus.CONFLICTING_SLOTS);
            return;
        }

        playSoundIfEnabled();
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(mountInventorySlot));
        Managers.TickScheduler.scheduleNextTick(() -> {
            MouseUtils.sendRightClickInput();
            McUtils.sendPacket(new ServerboundSetCarriedItemPacket(McUtils.inventory().selected));
            summonTick = McUtils.player().tickCount;
        });
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

    private void postMountErrorMessage(RideMountStatus status) {
        Managers.Notification.queueMessage(
                Component.translatable(status.getTranslationKey()).withStyle(ChatFormatting.DARK_RED));
    }

    private enum RideMountStatus {
        NO_MOUNT("feature.wynntils.mountKeybind.noMount"),
        ALREADY_RIDING("feature.wynntils.mountKeybind.alreadyRiding"),
        CONFLICTING_SLOTS("feature.wynntils.mountKeybind.conflictingSlots");

        private final String translationKey;

        RideMountStatus(String tcString) {
            this.translationKey = tcString;
        }

        private String getTranslationKey() {
            return this.translationKey;
        }
    }
}
