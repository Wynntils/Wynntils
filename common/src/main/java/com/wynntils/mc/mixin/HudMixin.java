/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.RenderElementType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method =
                    "extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void extractSlotPre(
            GuiGraphicsExtractor guiGraphics,
            int x,
            int y,
            DeltaTracker deltaTracker,
            Player player,
            ItemStack itemStack,
            int i,
            CallbackInfo info) {
        HotbarSlotRenderEvent.Pre event = new HotbarSlotRenderEvent.Pre(guiGraphics, itemStack, x, y);
        MixinHelper.post(event);

        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(
            method =
                    "extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V"))
    private void extractSlotCountPre(
            GuiGraphicsExtractor guiGraphics,
            int x,
            int y,
            DeltaTracker deltaTracker,
            Player player,
            ItemStack itemStack,
            int i,
            CallbackInfo info) {
        MixinHelper.post(new HotbarSlotRenderEvent.CountPre(guiGraphics, itemStack, x, y));
    }

    @Inject(
            method =
                    "extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("RETURN"))
    private void extractSlotPost(
            GuiGraphicsExtractor guiGraphics,
            int x,
            int y,
            DeltaTracker deltaTracker,
            Player player,
            ItemStack itemStack,
            int i,
            CallbackInfo info) {
        MixinHelper.post(new HotbarSlotRenderEvent.Post(guiGraphics, itemStack, x, y));
    }

    @Inject(
            method =
                    "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"))
    private void onExtractRenderStatePre(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (this.minecraft.gui.screen() instanceof LevelLoadingScreen) return;
        if (McUtils.mc().gui.hud.isHidden()) return;
        MixinHelper.post(
                new RenderEvent.Pre(guiGraphics, deltaTracker, this.minecraft.getWindow(), RenderElementType.GUI_PRE));
    }

    @Inject(
            method =
                    "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("RETURN"))
    private void onExtractRenderStatePost(
            GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (this.minecraft.gui.screen() instanceof LevelLoadingScreen) return;
        if (McUtils.mc().gui.hud.isHidden()) return;
        MixinHelper.post(new RenderEvent.Post(
                guiGraphics, deltaTracker, this.minecraft.getWindow(), RenderElementType.GUI_POST));
    }

    @Inject(
            method =
                    "extractCameraOverlays(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"))
    private void onExtractCameraOverlaysPre(
            GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MixinHelper.post(new RenderEvent.Pre(
                guiGraphics, deltaTracker, this.minecraft.getWindow(), RenderElementType.CAMERA_OVERLAYS));
    }

    @Inject(
            method =
                    "extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onExtractCrosshairPre(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        RenderEvent.Pre event =
                new RenderEvent.Pre(guiGraphics, deltaTracker, this.minecraft.getWindow(), RenderElementType.CROSSHAIR);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onExtractSelectedItemNamePre(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        if (!MixinHelper.onWynncraft()) return;

        RenderEvent.Pre event = new RenderEvent.Pre(
                guiGraphics, DeltaTracker.ZERO, this.minecraft.getWindow(), RenderElementType.SELECTED_ITEM);
        MixinHelper.post(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method =
                    "extractBossOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"))
    private void onExtractBossOverlayPre(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MixinHelper.post(new RenderEvent.Pre(
                guiGraphics, deltaTracker, this.minecraft.getWindow(), RenderElementType.BOSS_BARS));
    }

    @Inject(
            method =
                    "extractScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onExtractScoreboardSidebarPre(
            GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!MixinHelper.onWynncraft()) return;

        RenderEvent.Pre event = new RenderEvent.Pre(
                guiGraphics, DeltaTracker.ZERO, this.minecraft.getWindow(), RenderElementType.SCOREBOARD);
        MixinHelper.post(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method =
                    "extractOverlayMessage(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"))
    private void onExtractOverlayMessagePre(
            GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MixinHelper.post(new RenderEvent.Pre(
                guiGraphics, deltaTracker, this.minecraft.getWindow(), RenderElementType.ACTION_BAR));
    }

    @Inject(
            method =
                    "extractOverlayMessage(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("TAIL"))
    private void onExtractOverlayMessagePost(
            GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MixinHelper.post(new RenderEvent.Post(
                guiGraphics, deltaTracker, this.minecraft.getWindow(), RenderElementType.ACTION_BAR));
    }

    @Inject(
            method =
                    "extractTitle(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"))
    private void onExtractTitlePre(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MixinHelper.post(
                new RenderEvent.Pre(guiGraphics, deltaTracker, this.minecraft.getWindow(), RenderElementType.TITLE));
    }

    @Inject(
            method = "extractChat(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"))
    private void onExtractChatPre(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MixinHelper.post(
                new RenderEvent.Pre(guiGraphics, deltaTracker, this.minecraft.getWindow(), RenderElementType.CHAT));
    }

    @Inject(
            method =
                    "extractTabList(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onExtractTabListPre(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        RenderEvent.Pre renderEvent = new RenderEvent.Pre(
                guiGraphics, DeltaTracker.ZERO, McUtils.window(), RenderElementType.PLAYER_TAB_LIST);
        MixinHelper.post(renderEvent);

        if (renderEvent.isCanceled()) {
            ci.cancel();
        }
    }
}
