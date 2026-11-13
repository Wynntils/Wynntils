/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;

public final class MouseUtils {
    public static void sendLeftClickInput(boolean weaponSwing) {
        if (weaponSwing) {
            McUtils.player().swing(InteractionHand.MAIN_HAND);
        } else {
            McUtils.sendPacket(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
    }

    public static void sendRightClickInput() {
        McUtils.sendSequencedPacket(id -> new ServerboundUseItemPacket(
                InteractionHand.MAIN_HAND,
                id,
                McUtils.player().getYRot(),
                McUtils.player().getXRot()));
    }

    public static void sendDirectRightClickInput() {
        McUtils.sendPacket(new ServerboundUseItemPacket(
                InteractionHand.MAIN_HAND,
                0,
                McUtils.player().getYRot(),
                McUtils.player().getXRot()));
    }
}
