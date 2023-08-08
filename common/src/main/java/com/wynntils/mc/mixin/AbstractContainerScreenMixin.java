/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.InventoryMouseClickedEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", at = @At("RETURN"))
    private void renderPost(PoseStack client, int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
        MixinHelper.post(new ContainerRenderEvent(
                (AbstractContainerScreen<?>) (Object) this, client, mouseX, mouseY, partialTicks, this.hoveredSlot));
    }

    @Inject(
            method = "renderSlot(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/inventory/Slot;)V",
            at = @At("HEAD"))
    private void renderSlotPre(PoseStack poseStack, Slot slot, CallbackInfo info) {
        MixinHelper.post(new SlotRenderEvent.Pre(poseStack, (Screen) (Object) this, slot));
    }

    @Inject(
            method = "renderSlot(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/inventory/Slot;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void renderSlotPreCount(PoseStack poseStack, Slot slot, CallbackInfo info) {
        MixinHelper.post(new SlotRenderEvent.CountPre(poseStack, (Screen) (Object) this, slot));
    }

    @Inject(
            method = "renderSlot(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/inventory/Slot;)V",
            at = @At("RETURN"))
    private void renderSlotPost(PoseStack poseStack, Slot slot, CallbackInfo info) {
        MixinHelper.post(new SlotRenderEvent.Post(poseStack, (Screen) (Object) this, slot));
    }

    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void keyPressedPre(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        InventoryKeyPressEvent event = new InventoryKeyPressEvent(keyCode, scanCode, modifiers, this.hoveredSlot);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"), cancellable = true)
    private void mousePressedPre(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        InventoryMouseClickedEvent event = new InventoryMouseClickedEvent(mouseX, mouseY, button, this.hoveredSlot);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "mouseDragged(DDIDD)Z", at = @At("RETURN"))
    private void mouseDraggedPre(
            double mouseX,
            double mouseY,
            int button,
            double deltaX,
            double deltaY,
            CallbackInfoReturnable<Boolean> cir) {
        TextInputBoxWidget focusedTextInput = ((TextboxScreen) this).getFocusedTextInput();

        if (focusedTextInput != null) {
            focusedTextInput.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Inject(method = "mouseReleased(DDI)Z", at = @At("RETURN"))
    private void mouseReleasedPre(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        TextInputBoxWidget focusedTextInput = ((TextboxScreen) this).getFocusedTextInput();

        if (focusedTextInput != null) {
            focusedTextInput.mouseReleased(mouseX, mouseY, button);
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
