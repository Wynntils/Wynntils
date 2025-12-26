/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ContainerLabelRenderEvent;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.InventoryMouseClickedEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow
    public Slot hoveredSlot;

    @Inject(method = "renderContents(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("RETURN"))
    private void renderContentsPost(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
        MixinHelper.post(new ContainerRenderEvent(
                (AbstractContainerScreen<?>) (Object) this,
                guiGraphics,
                mouseX,
                mouseY,
                partialTicks,
                this.hoveredSlot));
    }

    @WrapOperation(
            method = "renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",
                            ordinal = 0))
    private void renderContainerLabel(
            GuiGraphics instance,
            Font font,
            Component text,
            int x,
            int y,
            int color,
            boolean dropShadow,
            Operation<Void> original) {
        ContainerLabelRenderEvent.ContainerLabel event = new ContainerLabelRenderEvent.ContainerLabel(
                (AbstractContainerScreen<?>) (Object) this, instance, color, x, y, text);
        MixinHelper.post(event);

        if (event.isCanceled()) return;

        original.call(instance, font, event.getContainerLabel(), x, y, event.getColor(), dropShadow);
    }

    @WrapOperation(
            method = "renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",
                            ordinal = 1))
    private void renderInventoryLabel(
            GuiGraphics instance,
            Font font,
            Component text,
            int x,
            int y,
            int color,
            boolean dropShadow,
            Operation<Void> original) {
        ContainerLabelRenderEvent.InventoryLabel event = new ContainerLabelRenderEvent.InventoryLabel(
                (AbstractContainerScreen<?>) (Object) this, instance, color, x, y, text);
        MixinHelper.post(event);

        if (event.isCanceled()) return;

        original.call(instance, font, event.getInventoryLabel(), x, y, event.getColor(), dropShadow);
    }

    @Inject(
            method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;II)V",
            at = @At("HEAD"))
    private void renderSlotPre(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, CallbackInfo info) {
        MixinHelper.post(new SlotRenderEvent.Pre(guiGraphics, (Screen) (Object) this, slot));
    }

    @Inject(
            method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;II)V",
            at = @At("RETURN"))
    private void renderSlotPost(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, CallbackInfo info) {
        MixinHelper.post(new SlotRenderEvent.Post(guiGraphics, (Screen) (Object) this, slot));
    }

    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void keyPressedPre(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        InventoryKeyPressEvent event = new InventoryKeyPressEvent(keyEvent, this.hoveredSlot);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(
            method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void mousePressedPre(
            MouseButtonEvent mouseButtonEvent, boolean isDoubleClick, CallbackInfoReturnable<Boolean> cir) {
        InventoryMouseClickedEvent event =
                new InventoryMouseClickedEvent(mouseButtonEvent, isDoubleClick, this.hoveredSlot);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "mouseDragged(Lnet/minecraft/client/input/MouseButtonEvent;DD)Z", at = @At("RETURN"))
    private void mouseDraggedPre(
            MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        TextInputBoxWidget focusedTextInput = ((TextboxScreen) this).getFocusedTextInput();

        if (focusedTextInput != null) {
            focusedTextInput.mouseDragged(mouseButtonEvent, deltaX, deltaY);
        }
    }

    @Inject(method = "mouseReleased(Lnet/minecraft/client/input/MouseButtonEvent;)Z", at = @At("RETURN"))
    private void mouseReleasedPre(MouseButtonEvent mouseButtonEvent, CallbackInfoReturnable<Boolean> cir) {
        TextInputBoxWidget focusedTextInput = ((TextboxScreen) this).getFocusedTextInput();

        if (focusedTextInput != null) {
            focusedTextInput.mouseReleased(mouseButtonEvent);
        }
    }

    @Inject(method = "onClose()V", at = @At("HEAD"), cancellable = true)
    private void onCloseContainerPre(CallbackInfo ci) {
        ContainerCloseEvent.Pre event = new ContainerCloseEvent.Pre();
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onClose()V", at = @At("RETURN"))
    private void onCloseContainerPost(CallbackInfo ci) {
        MixinHelper.post(new ContainerCloseEvent.Post());
    }
}
