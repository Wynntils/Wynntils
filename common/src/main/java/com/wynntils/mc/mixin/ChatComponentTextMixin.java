/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ChatComponentRenderEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The ChatComponent.LineConsumer inside ChatComponent#render
@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$1")
public abstract class ChatComponentTextMixin {
    @Unique
    private GuiMessage.Line currentLine;

    @Inject(method = "accept(Lnet/minecraft/client/GuiMessage$Line;IF)V", at = @At("HEAD"))
    private void captureLine(GuiMessage.Line line, int ix, float fx, CallbackInfo ci) {
        this.currentLine = line;
    }

    @WrapOperation(
            method = "accept(Lnet/minecraft/client/GuiMessage$Line;IF)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;handleMessage(IFLnet/minecraft/util/FormattedCharSequence;)Z"))
    private boolean wrapHandleMessage(
            ChatComponent.ChatGraphicsAccess access,
            int y,
            float opacity,
            FormattedCharSequence text,
            Operation<Boolean> original) {
        GuiGraphics graphics = null;

        if (access instanceof ChatComponent.DrawingBackgroundGraphicsAccess bg) {
            graphics = bg.graphics;
        } else if (access instanceof ChatComponent.DrawingFocusedGraphicsAccess fg) {
            graphics = fg.graphics;
        }

        if (graphics != null && this.currentLine != null) {
            MixinHelper.post(new ChatComponentRenderEvent.Text(
                    graphics, this.currentLine, McUtils.mc().font, y, ARGB.white(opacity)));
        }

        return original.call(access, y, opacity, text);
    }
}
