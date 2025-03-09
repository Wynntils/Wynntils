/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.DropHeldItemEvent;
import com.wynntils.mc.event.LocalSoundEvent;
import com.wynntils.mc.event.ShiftKeyStateChangeEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true)
    private void onDropPre(boolean fullStack, CallbackInfoReturnable<Boolean> cir) {
        DropHeldItemEvent event = new DropHeldItemEvent(fullStack);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", at = @At("HEAD"), cancellable = true)
    private void playSoundPre(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        LocalSoundEvent.Player event = new LocalSoundEvent.Player(sound);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "sendShiftKeyState()V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void sendShiftKeyState(CallbackInfo ci, @Local(ordinal = 0) ServerboundPlayerCommandPacket.Action action) {
        MixinHelper.post(new ShiftKeyStateChangeEvent(action));
    }
}
