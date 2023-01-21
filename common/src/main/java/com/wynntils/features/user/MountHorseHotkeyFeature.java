/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import com.wynntils.utils.wynn.WynnUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.STABLE)
public class MountHorseHotkeyFeature extends UserFeature {
    private static final int SEARCH_RADIUS = 6; // Furthest blocks away from which we can interact with a horse
    private static final int SUMMON_ATTEMPTS = 8;
    private static final int SUMMON_DELAY_TICKS = 6;

    private static int prevItem = -1;
    private static boolean alreadySetPrevItem = false;

    @RegisterKeyBind
    private final KeyBind mountHorseKeyBind =
            new KeyBind("Mount Horse", GLFW.GLFW_KEY_R, true, this::onMountHorseKeyPress);

    private void onMountHorseKeyPress() {
        if (!WynnUtils.onWorld()) return;

        if (McUtils.player().getVehicle() != null) {
            postHorseErrorMessage(MountHorseStatus.ALREADY_RIDING);
            return;
        }

        AbstractHorse horse = Models.Horse.searchForHorseNearby(SEARCH_RADIUS);
        if (horse == null) { // Horse has not spawned, we should do that
            int horseInventorySlot = Models.Horse.findHorseSlotNum();
            if (horseInventorySlot > 8 || horseInventorySlot == -1) {
                postHorseErrorMessage(MountHorseStatus.NO_HORSE);
                return;
            }
            trySummonAndMountHorse(horseInventorySlot, SUMMON_ATTEMPTS);
        } else { // Horse already exists, mount it
            mountHorse(horse);
        }
    }

    /** Horse should be nearby when this is called */
    private void mountHorse(Entity horse) {
        // swap to soul points to avoid right click problems
        int prevItem = McUtils.inventory().selected;
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(InventoryUtils.SOUL_POINTS_SLOT_NUM));
        McUtils.sendPacket(ServerboundInteractPacket.createInteractionPacket(horse, false, InteractionHand.MAIN_HAND));
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(prevItem));
    }

    private void trySummonAndMountHorse(int horseInventorySlot, int attempts) {
        if (attempts <= 0) {
            postHorseErrorMessage(MountHorseStatus.NO_HORSE);
            return;
        }

        if (!alreadySetPrevItem) {
            prevItem = McUtils.inventory().selected;
            alreadySetPrevItem = true;
        }

        Managers.TickScheduler.scheduleLater(
                () -> {
                    AbstractHorse horse = Models.Horse.searchForHorseNearby(SEARCH_RADIUS);
                    if (horse != null) { // Horse successfully summoned
                        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(prevItem));
                        alreadySetPrevItem = false;
                        mountHorse(horse);
                        return;
                    }
                    McUtils.sendPacket(new ServerboundSetCarriedItemPacket(horseInventorySlot));
                    McUtils.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id));

                    trySummonAndMountHorse(horseInventorySlot, attempts - 1);
                },
                SUMMON_DELAY_TICKS);
    }

    private void postHorseErrorMessage(MountHorseStatus status) {
        Managers.Notification.queueMessage(
                Component.translatable(status.getTcString()).withStyle(ChatFormatting.DARK_RED));
    }

    private enum MountHorseStatus {
        NO_HORSE("feature.wynntils.mountHorseHotkey.noHorse"),
        ALREADY_RIDING("feature.wynntils.mountHorseHotkey.alreadyRiding");

        private final String tcString;

        MountHorseStatus(String tcString) {
            this.tcString = tcString;
        }

        private String getTcString() {
            return this.tcString;
        }
    }
}
