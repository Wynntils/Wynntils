/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ClientsideMessageEvent;
import com.wynntils.mc.event.DropHeldItemEvent;
import com.wynntils.mc.event.LocalSoundEvent;
import com.wynntils.mc.event.PlayerMoveEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
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

    @Inject(method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(Component component, CallbackInfo ci) {
        if ((Object) this != McUtils.player()) return;

        ClientsideMessageEvent event = new ClientsideMessageEvent(component);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
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
            method = "Lnet/minecraft/client/player/LocalPlayer;sendPosition()V",
            at = {
                @At(
                        value = "NEW",
                        target = "(DDDFFZ)Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket$PosRot;"),
                @At(
                        value = "NEW",
                        target = "(DDDFF)Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket$Pos;"),
            },
            cancellable = true)
    private void sendPositionPre(CallbackInfo ci) {
        PlayerMoveEvent event = new PlayerMoveEvent();
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
