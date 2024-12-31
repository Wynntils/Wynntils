/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;

public final class MouseUtils {
    public static void sendAttackInput(boolean reversed) {
        if (reversed) {
            McUtils.sendSequencedPacket(id -> new ServerboundUseItemPacket(
                    InteractionHand.MAIN_HAND,
                    id,
                    McUtils.player().getYRot(),
                    McUtils.player().getXRot()));
        } else {
            // ServerboundSwingPacket does not do the swing animation
            McUtils.player().swing(InteractionHand.MAIN_HAND);
        }
    }

    public static void sendLeftClickInput() {
        McUtils.sendPacket(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
    }

    public static void sendRightClickInput() {
        McUtils.sendSequencedPacket(id -> new ServerboundUseItemPacket(
                InteractionHand.MAIN_HAND,
                id,
                McUtils.player().getYRot(),
                McUtils.player().getXRot()));
    }
}
