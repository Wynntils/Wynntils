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
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.MouseUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class MountKeybindFeature extends Feature {
    private static final Identifier MOUNT_WHISTLE_ID = Identifier.fromNamespaceAndPath("wynntils", "horse.whistle");
    private static final SoundEvent MOUNT_WHISTLE_SOUND = SoundEvent.createVariableRangeEvent(MOUNT_WHISTLE_ID);
    private static final int RESTORE_TIMEOUT_TICKS = 40;

    // TODO: Check if there are new error messages, as of writing this Wyverns and Adasaurs haven't been released yet
    private static final List<String> MOUNT_ERROR_MESSAGES = List.of(
            "There is no room for a horse.",
            "You cannot interact with your horse at the moment.",
            "You cannot use your horse here!",
            "Your horse spawn was disabled (in vanish)!",
            "You can not use a horse while in war.",
            "You cannot use your vehicle here!");

    @RegisterKeyBind
    private final KeyBind mountHorseKeyBind = KeyBindDefinition.MOUNT_HORSE.create(this::mountHorse);

    private int prevItem = -1;
    private boolean alreadySetPrevItem = false;
    private boolean cancelMounting = false;

    @Persisted
    private final Config<Boolean> playWhistle = new Config<>(true);

    public MountKeybindFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER, ConfigProfile.LITE)
                .build());
    }

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        if (!Models.WorldState.onWorld()) return;

        int mountInventorySlot = Models.Horse.findMountSlotNum();
        if (mountInventorySlot == -1) return;
        if (McUtils.inventory().selected != mountInventorySlot) return;

        mountHorse();
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onChatReceived(ChatMessageEvent.Match e) {
        cancelMounting = MOUNT_ERROR_MESSAGES.stream()
                .anyMatch(msg -> e.getMessage().getString().contains(msg));
    }

    private void mountHorse() {
        if (!Models.WorldState.onWorld()) return;

        LocalPlayer player = McUtils.player();
        if (player.getVehicle() != null) {
            postHorseErrorMessage(MountHorseStatus.ALREADY_RIDING);
            return;
        }

        int mountInventorySlot = Models.Horse.findMountSlotNum();
        if (mountInventorySlot == -1) {
            postHorseErrorMessage(MountHorseStatus.NO_HORSE);
            return;
        }
        if (mountInventorySlot > 8) {
            postHorseErrorMessage(MountHorseStatus.CONFLICTING_SLOTS);
            return;
        }

        if (!alreadySetPrevItem) {
            prevItem = McUtils.inventory().selected;
            alreadySetPrevItem = true;
        }
        ItemStack previousSlotStack = McUtils.inventory().getItem(prevItem).copy();

        if (playWhistle.get()) {
            McUtils.playSoundAmbient(MOUNT_WHISTLE_SOUND);
        }
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(mountInventorySlot));
        Managers.TickScheduler.scheduleNextTick(() -> {
            // Re-assert selected slot packet before use to reduce desync.
            McUtils.sendPacket(new ServerboundSetCarriedItemPacket(mountInventorySlot));
            MouseUtils.sendRightClickInput();
            waitForMountAndRestore(prevItem, previousSlotStack, RESTORE_TIMEOUT_TICKS);
        });
    }

    private void waitForMountAndRestore(int previousSlot, ItemStack previousSlotStack, int ticksLeft) {
        LocalPlayer player = McUtils.player();
        if (player == null) return;

        if (cancelMounting || player.getVehicle() != null || ticksLeft <= 0) {
            // Restore original local slot contents to clear any client-side ghost stack.
            McUtils.inventory().setItem(previousSlot, previousSlotStack.copy());
            McUtils.sendPacket(new ServerboundSetCarriedItemPacket(previousSlot));
            alreadySetPrevItem = false;
            cancelMounting = false;
            return;
        }

        Managers.TickScheduler.scheduleNextTick(
                () -> waitForMountAndRestore(previousSlot, previousSlotStack, ticksLeft - 1));
    }

    private void postHorseErrorMessage(MountHorseStatus status) {
        Managers.Notification.queueMessage(
                Component.translatable(status.getTcString()).withStyle(ChatFormatting.DARK_RED));
    }

    private enum MountHorseStatus {
        NO_HORSE("feature.wynntils.mountKeybind.noHorse"),
        ALREADY_RIDING("feature.wynntils.mountKeybind.alreadyRiding"),
        CONFLICTING_SLOTS("feature.wynntils.mountKeybind.conflictingSlots");

        private final String tcString;

        MountHorseStatus(String tcString) {
            this.tcString = tcString;
        }

        private String getTcString() {
            return this.tcString;
        }
    }
}
