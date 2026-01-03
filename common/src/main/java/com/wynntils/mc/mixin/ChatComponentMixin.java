/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.AddGuiMessageLineEvent;
import com.wynntils.mc.event.ChatComponentRenderEvent;
import com.wynntils.mc.event.ChatScreenCreateEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
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

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        allMessages = new CopyOnWriteArrayList<>();
        trimmedMessages = new CopyOnWriteArrayList<>();
    }

    @WrapOperation(
            method = "addMessageToDisplayQueue(Lnet/minecraft/client/GuiMessage;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;addFirst(Ljava/lang/Object;)V"))
    private void addMessageToDisplayQueue(
            List<GuiMessage.Line> instance,
            Object line,
            Operation<Void> original,
            @Local(argsOnly = true) GuiMessage message) {
        MixinHelper.post(new AddGuiMessageLineEvent(message, (GuiMessage.Line) line));

        original.call(trimmedMessages, line);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IIZ)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V"))
    private void setupRender(
            ChatComponent.ChatGraphicsAccess chatGraphicsAccess,
            int mouseX,
            int mouseY,
            boolean focused,
            CallbackInfo ci) {
        MixinHelper.post(new ChatComponentRenderEvent.Pre((ChatComponent) (Object) this));
    }

    @ModifyArg(
            method = "method_75801", // updatePose lambda in render
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lorg/joml/Matrix3x2f;translate(FF)Lorg/joml/Matrix3x2f;",
                            remap = false),
            index = 0)
    private static float offsetChatBox(float x) {
        ChatComponentRenderEvent.Translate event = new ChatComponentRenderEvent.Translate(x);

        MixinHelper.post(event);

        return event.getX();
    }

    @WrapOperation(
            method = "method_75802",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;fill(IIIII)V"))
    private static void renderTimestampBackground(
            ChatComponent.ChatGraphicsAccess access,
            int x1,
            int y1,
            int x2,
            int y2,
            int color,
            Operation<Void> original) {
        GuiGraphics guiGraphics = null;
        if (access instanceof ChatComponent.DrawingBackgroundGraphicsAccess bg) {
            guiGraphics = bg.graphics;
        } else if (access instanceof ChatComponent.DrawingFocusedGraphicsAccess fg) {
            guiGraphics = fg.graphics;
        }

        if (guiGraphics != null) {
            int lineHeight = y2 - y1;
            float opacity = ((color >>> 24) & 0xFF) / 255.0f;

            MixinHelper.post(new ChatComponentRenderEvent.Background(guiGraphics, y1, lineHeight, opacity));
        }

        original.call(access, x1, y1, x2, y2, color);
    }

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
