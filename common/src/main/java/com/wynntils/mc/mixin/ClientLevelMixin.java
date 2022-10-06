/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "disconnect()V", at = @At("HEAD"))
    private void disconnectPre() {
        // User-triggered logoff
        EventFactory.onDisconnect();
    }
}
