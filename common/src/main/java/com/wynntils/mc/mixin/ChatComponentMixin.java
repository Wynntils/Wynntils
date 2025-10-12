/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.AddGuiMessageLineEvent;
import com.wynntils.mc.event.ChatComponentRenderEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Shadow
    private List<GuiMessage> allMessages;

    @Shadow
    private List<GuiMessage.Line> trimmedMessages;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        allMessages = new CopyOnWriteArrayList<>();
        trimmedMessages = new CopyOnWriteArrayList<>();
    }

    @WrapOperation(
            method = "addMessageToDisplayQueue(Lnet/minecraft/client/GuiMessage;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"))
    private void addMessageToDisplayQueue(
            List<GuiMessage.Line> instance,
            int i,
            Object line,
            Operation<Void> original,
            @Local(ordinal = 1) int index,
            @Local(argsOnly = true) GuiMessage message) {
        MixinHelper.post(new AddGuiMessageLineEvent(message, (GuiMessage.Line) line, index));

        original.call(trimmedMessages, i, line);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V"))
    private void setupRender(
            GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        MixinHelper.post(new ChatComponentRenderEvent.Pre((ChatComponent) (Object) this, guiGraphics));
    }

    @ModifyArg(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 0),
            index = 0)
    private float offsetChatBox(float x) {
        ChatComponentRenderEvent.Translate event =
                new ChatComponentRenderEvent.Translate((ChatComponent) (Object) this, x);

        MixinHelper.post(event);

        return event.getX();
    }

    @WrapMethod(method = "screenToChatX")
    private double screenToChatX(double x, Operation<Double> original) {
        ChatComponentRenderEvent.MapMouseX event =
                new ChatComponentRenderEvent.MapMouseX((ChatComponent) (Object) this, x);

        MixinHelper.post(event);

        return original.call(event.getX());
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0))
    private void renderTimestampBackground(
            GuiGraphics guiGraphics,
            int tickCount,
            int mouseX,
            int mouseY,
            boolean focused,
            CallbackInfo ci,
            @Local(ordinal = 18) int renderX,
            @Local(ordinal = 9) int lineHeight,
            @Local(ordinal = 16) int opacity) {
        MixinHelper.post(new ChatComponentRenderEvent.Background(
                (ChatComponent) (Object) this, guiGraphics, renderX, lineHeight, opacity));
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"))
    private void renderTimestamp(
            GuiGraphics guiGraphics,
            int tickCount,
            int mouseX,
            int mouseY,
            boolean focused,
            CallbackInfo ci,
            @Local GuiMessage.Line line,
            @Local(ordinal = 19) int y,
            @Local(ordinal = 15) int textOpacity) {
        MixinHelper.post(new ChatComponentRenderEvent.Text(
                (ChatComponent) (Object) this, guiGraphics, line, this.minecraft.font, y, textOpacity));
    }
}
