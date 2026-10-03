/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;

public final class MouseUtils {
    public static void sendLeftClickInput(boolean weaponSwing) {
        if (weaponSwing) {
            McUtils.player().swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
        } else {
            McUtils.sendPacket(new ServerboundPunchPacket());
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
