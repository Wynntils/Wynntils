/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @ModifyVariable(method = "chat(Ljava/lang/String;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private String modifyChatMessage(String message) {
        return EventFactory.onChatSent(message).getMessage();
    }
}
