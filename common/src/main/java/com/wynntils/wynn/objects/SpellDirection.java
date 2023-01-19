package com.wynntils.wynn.objects;

import com.wynntils.mc.utils.McUtils;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;

import java.util.Arrays;

public enum SpellDirection {
    RIGHT(() -> McUtils.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id))),
    LEFT(() -> McUtils.sendPacket(new ServerboundSwingPacket(InteractionHand.MAIN_HAND)));

    private final Runnable sendPacketRunnable;

    SpellDirection(Runnable sendPacketRunnable) {
        this.sendPacketRunnable = sendPacketRunnable;
    }

    public Runnable getSendPacketRunnable() {
        return sendPacketRunnable;
    }

    public static SpellDirection[] invertArray(SpellDirection[] initial) {
        return Arrays.stream(initial).map((x) -> (x == RIGHT) ? LEFT : RIGHT).toArray(SpellDirection[]::new);
    }
}
