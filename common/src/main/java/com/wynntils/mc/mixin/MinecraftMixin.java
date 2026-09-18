/*
 * Copyright © Wynntils 2021-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.wynntils.core.components.Services;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.DisplayResizeEvent;
import com.wynntils.mc.event.ServerResourcePackEvent;
import com.wynntils.mc.event.SetCameraEntityEvent;
import com.wynntils.mc.event.TickAlwaysEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.utils.TaskUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void tickPost(CallbackInfo ci) {
        // TickAlwaysEvent is posted before TickEvent to ensure
        // that the tasks in TickSchedulerManager are run before
        // any other tick event listeners could schedule new tasks
        // making it run in the same tick
        MixinHelper.postAlways(new TickAlwaysEvent());
        MixinHelper.post(new TickEvent());
    }

    @Inject(method = "resizeGui()V", at = @At("RETURN"))
    private void resizeGuiPost(CallbackInfo ci) {
        MixinHelper.postAlways(new DisplayResizeEvent());
    }

    @Inject(
            method = "<init>(Lnet/minecraft/client/main/GameConfig;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/Options;loadSelectedResourcePacks(Lnet/minecraft/server/packs/repository/PackRepository;)V",
                            shift = At.Shift.AFTER))
    private void onInitialResourcePackLoad(CallbackInfo ci) {
        // Too early to post events here, but Service components are initialized (and their storages loaded)
        // We add the resource pack to the selected list here
        Services.ResourcePack.preloadResourcePack();
        // Explicitly do not trigger a reload here, as it's too early, and the game will do it later
    }

    @Inject(
            method = "clearDownloadedResourcePacks()V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/resources/server/DownloadedPackSource;cleanupAfterDisconnect()V",
                            shift = At.Shift.AFTER))
    private void handleResourcePackPopPre(CallbackInfo ci) {
        ServerResourcePackEvent.Clear event = new ServerResourcePackEvent.Clear();
        MixinHelper.postAlways(event);
    }

    @Inject(method = "setCameraEntity(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    private void setCameraEntityPre(Entity viewingEntity, CallbackInfo ci) {
        SetCameraEntityEvent event = new SetCameraEntityEvent(viewingEntity);
        MixinHelper.post(event);
    }

    @WrapWithCondition(
            method = "startAttack()Z",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private boolean onAttack(LocalPlayer localPlayer, InteractionHand hand) {
        ArmSwingEvent event = new ArmSwingEvent(ArmSwingEvent.ArmSwingContext.ATTACK_OR_START_BREAKING_BLOCK, hand);
        MixinHelper.post(event);

        return !event.isCanceled();
    }

    @Inject(method = "close()V", at = @At("HEAD"))
    private void closePre(CallbackInfo callbackInfo) {
        TaskUtils.close();
    }
}
