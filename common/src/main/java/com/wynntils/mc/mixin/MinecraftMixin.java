/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.wynntils.core.components.Services;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.ChatScreenCreateEvent;
import com.wynntils.mc.event.DisplayResizeEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.event.ServerResourcePackEvent;
import com.wynntils.mc.event.TickAlwaysEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.extension.MinecraftExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftExtension {
    @Unique
    private RenderTarget wynntils_overridenRenderTarget;

    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("RETURN"))
    private void setScreenPost(Screen screen, CallbackInfo ci, @Share("oldScreen") LocalRef<Screen> oldScreen) {
        Event event = (screen == null)
                ? new ScreenClosedEvent.Post(oldScreen.get())
                : new ScreenOpenedEvent.Post(screen, oldScreen.get());
        MixinHelper.post(event);
    }

    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"), cancellable = true)
    private void setScreenPre(Screen screen, CallbackInfo ci, @Share("oldScreen") LocalRef<Screen> oldScreen) {
        oldScreen.set(((Minecraft) (Object) this).screen);

        // "var" is needed since there is no specific enough common supertype between ScreenOpenedEvent.Pre and
        // ScreenClosedEvent.Pre
        var event = (screen == null)
                ? new ScreenClosedEvent.Pre(oldScreen.get())
                : new ScreenOpenedEvent.Pre(screen, oldScreen.get());
        MixinHelper.postAlways(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void tickPost(CallbackInfo ci) {
        // TickAlwaysEvent is posted before TickEvent to ensure
        // that the tasks in TickSchedulerManager are run before
        // any other tick event listeners could schedule new tasks
        // making it run in the same tick
        MixinHelper.postAlways(new TickAlwaysEvent());
        MixinHelper.post(new TickEvent());
    }

    @Inject(method = "resizeDisplay()V", at = @At("RETURN"))
    private void resizeDisplayPost(CallbackInfo ci) {
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

    @WrapMethod(method = "getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;")
    private RenderTarget getMainRenderTarget(Operation<RenderTarget> operation) {
        if (this.wynntils_overridenRenderTarget != null) {
            return this.wynntils_overridenRenderTarget;
        }

        return operation.call();
    }

    @WrapOperation(
            method = "openChatScreen(Ljava/lang/String;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void wrapOpenChatScreenSetScreen(
            Minecraft instance, Screen screen, Operation<Void> original, String defaultText) {
        if (screen == null) {
            original.call(instance, screen);
            return;
        }

        ChatScreenCreateEvent event = new ChatScreenCreateEvent(screen, defaultText);
        MixinHelper.post(event);
        original.call(instance, event.getScreen());
    }

    @Override
    public void setOverridenRenderTarget(RenderTarget renderTarget) {
        this.wynntils_overridenRenderTarget = renderTarget;
    }
}
