/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ContainerLabelRenderEvent;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.InventoryMouseClickedEvent;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
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

    @Shadow
    public abstract void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("RETURN"))
    private void renderPost(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
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
                                    "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I",
                            ordinal = 0))
    private int renderContainerLabel(
            GuiGraphics instance,
            Font font,
            Component text,
            int x,
            int y,
            int color,
            boolean dropShadow,
            Operation<Integer> original) {
        ContainerLabelRenderEvent.ContainerLabel event = new ContainerLabelRenderEvent.ContainerLabel(
                (AbstractContainerScreen<?>) (Object) this, instance, color, x, y, text);
        MixinHelper.post(event);

        if (event.isCanceled()) return 0;

        return original.call(instance, font, event.getContainerLabel(), x, y, event.getColor(), dropShadow);
    }

    @WrapOperation(
            method = "renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I",
                            ordinal = 1))
    private int renderInventoryLabel(
            GuiGraphics instance,
            Font font,
            Component text,
            int x,
            int y,
            int color,
            boolean dropShadow,
            Operation<Integer> original) {
        ContainerLabelRenderEvent.InventoryLabel event = new ContainerLabelRenderEvent.InventoryLabel(
                (AbstractContainerScreen<?>) (Object) this, instance, color, x, y, text);
        MixinHelper.post(event);

        if (event.isCanceled()) return 0;

        return original.call(instance, font, event.getInventoryLabel(), x, y, event.getColor(), dropShadow);
    }

    @Inject(
            method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V",
            at = @At("HEAD"))
    private void renderSlotPre(GuiGraphics guiGraphics, Slot slot, CallbackInfo info) {
        MixinHelper.post(new SlotRenderEvent.Pre(guiGraphics, (Screen) (Object) this, slot));
    }

    @Inject(
            method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void renderSlotPreCount(GuiGraphics guiGraphics, Slot slot, CallbackInfo info) {
        MixinHelper.post(new SlotRenderEvent.CountPre(guiGraphics, (Screen) (Object) this, slot));
    }

    @Inject(
            method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V",
            at = @At("RETURN"))
    private void renderSlotPost(GuiGraphics guiGraphics, Slot slot, CallbackInfo info) {
        MixinHelper.post(new SlotRenderEvent.Post(guiGraphics, (Screen) (Object) this, slot));
    }

    // Note: Call site 2 of 3 of ItemTooltipRenderEvent. Check the event class for more info.
    //       Additionally, we do not require these mixins, since forge will not match the method signatures,
    //       and this way we don't need to make this mixin fabric only.
    //       See ForgeGuiGraphicsMixin for the Forge mixin.
    @WrapOperation(
            method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"),
            require = 0)
    private void renderTooltipPre(
            GuiGraphics instance,
            Font font,
            List<Component> tooltipLines,
            Optional<TooltipComponent> visualTooltipComponent,
            int mouseX,
            int mouseY,
            Operation<Void> operation,
            @Local ItemStack itemStack) {
        ItemTooltipRenderEvent.Pre event =
                new ItemTooltipRenderEvent.Pre(instance, itemStack, tooltipLines, mouseX, mouseY);
        MixinHelper.post(event);
        if (event.isCanceled()) return;

        operation.call(
                instance,
                font,
                event.getTooltips(),
                event.getItemStack().getTooltipImage(),
                event.getMouseX(),
                event.getMouseY());
    }

    @Inject(
            method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V",
                            shift = At.Shift.AFTER),
            require = 0)
    private void renderTooltipPost(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci, @Local ItemStack itemStack) {
        MixinHelper.post(new ItemTooltipRenderEvent.Post(guiGraphics, itemStack, x, y));
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
