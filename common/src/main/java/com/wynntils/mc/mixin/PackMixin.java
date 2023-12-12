/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.PackGetFixedPositionEvent;
import net.minecraft.server.packs.repository.Pack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Pack.class)
public abstract class PackMixin {
    @Shadow
    public abstract String getId();

    @Shadow
    @Final
    private boolean fixedPosition;

    @Redirect(
            method = "*",
            at =
                    @At(
                            target = "Lnet/minecraft/server/packs/repository/Pack;fixedPosition:Z",
                            value = "FIELD",
                            opcode = Opcodes.GETFIELD))
    private boolean onGetFixedPosition(Pack pack) {
        PackGetFixedPositionEvent event = new PackGetFixedPositionEvent(pack, this.fixedPosition);
        MixinHelper.postAlways(event);
        return event.isFixedPosition();
    }
}
