/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ChatScreenCreateEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    //    @WrapOperation(
    //            method = "addMessageToDisplayQueue(Lnet/minecraft/client/GuiMessage;)V",
    //            at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"))
    //    private void addMessageToDisplayQueue(
    //            List<GuiMessage.Line> instance,
    //            int i,
    //            Object line,
    //            Operation<Void> original,
    //            @Local(ordinal = 1) int index,
    //            @Local(argsOnly = true) GuiMessage message) {
    //        MixinHelper.post(new AddGuiMessageLineEvent(message, (GuiMessage.Line) line, index));
    //
    //        original.call(trimmedMessages, i, line);
    //    }

    //    @Inject(
    //            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
    //            at =
    //                    @At(
    //                            value = "INVOKE",
    //                            target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V"))
    //    private void setupRender(
    //            GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
    //        MixinHelper.post(new ChatComponentRenderEvent.Pre((ChatComponent) (Object) this, guiGraphics));
    //    }

    //    @ModifyArg(
    //            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
    //            at =
    //                    @At(
    //                            value = "INVOKE",
    //                            target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;",
    //                            ordinal = 0,
    //                            remap = false),
    //            index = 0)
    //    private float offsetChatBox(float x) {
    //        ChatComponentRenderEvent.Translate event =
    //                new ChatComponentRenderEvent.Translate((ChatComponent) (Object) this, x);
    //
    //        MixinHelper.post(event);
    //
    //        return event.getX();
    //    }

    //    @WrapMethod(method = "screenToChatX")
    //    private double screenToChatX(double x, Operation<Double> original) {
    //        ChatComponentRenderEvent.MapMouseX event =
    //                new ChatComponentRenderEvent.MapMouseX((ChatComponent) (Object) this, x);
    //
    //        MixinHelper.post(event);
    //
    //        return original.call(event.getX());
    //    }

    //    @Inject(
    //            method = "method_71992", // 1st forEachLine lambda in render
    //            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal =
    // 0))
    //    private void renderTimestampBackground(
    //            GuiGraphics guiGraphics,
    //            int messageIndex,
    //            float backgroundOpacity,
    //            float chatOpacity,
    //            int screenHeight,
    //            int wrappedLineCount,
    //            int leftX,
    //            int topY,
    //            int bottomY,
    //            GuiMessage.Line line,
    //            int lineIndex,
    //            float lineFadeFactor,
    //            CallbackInfo ci) {
    //        MixinHelper.post(new ChatComponentRenderEvent.Background(
    //                (ChatComponent) (Object) this, guiGraphics, topY, bottomY - topY, backgroundOpacity *
    // lineFadeFactor));
    //    }

    //    @Inject(
    //            method = "method_71991", // 2nd forEachLine lambda in render
    //            at =
    //                    @At(
    //                            value = "INVOKE",
    //                            target =
    //
    // "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V"))
    //    private void renderTimestamp(
    //            int lineYOffset,
    //            GuiGraphics guiGraphics,
    //            float chatOpacity,
    //            int leftX,
    //            int i,
    //            int lineTop,
    //            GuiMessage.Line line,
    //            int lineIndex,
    //            float lineFadeFactor,
    //            CallbackInfo ci) {
    //        float finalAlpha = lineFadeFactor * chatOpacity;
    //        int color = ARGB.color(finalAlpha, -1);
    //
    //        MixinHelper.post(new ChatComponentRenderEvent.Text(
    //                (ChatComponent) (Object) this, guiGraphics, line, this.minecraft.font, lineTop + lineYOffset,
    // color));
    //    }

    @WrapOperation(
            method =
                    "createScreen(Lnet/minecraft/client/gui/components/ChatComponent$ChatMethod;Lnet/minecraft/client/gui/screens/ChatScreen$ChatConstructor;)Lnet/minecraft/client/gui/screens/ChatScreen;",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/screens/ChatScreen$ChatConstructor;create(Ljava/lang/String;Z)Lnet/minecraft/client/gui/screens/ChatScreen;"))
    private <T extends ChatScreen> T wrapCreateScreen(
            ChatScreen.ChatConstructor<T> constructor, String text, boolean draft, Operation<T> original) {
        T screen = original.call(constructor, text, draft);

        ChatScreenCreateEvent event = new ChatScreenCreateEvent(screen, text, draft);
        MixinHelper.post(event);

        return (T) event.getScreen();
    }
}
