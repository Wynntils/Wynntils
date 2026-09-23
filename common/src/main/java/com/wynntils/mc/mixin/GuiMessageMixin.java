/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.extension.GuiMessageExtension;
import java.time.LocalDateTime;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMessage.class)
public abstract class GuiMessageMixin implements GuiMessageExtension {
    @Unique
    private LocalDateTime createdAt;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(
            int addedTime,
            Component content,
            MessageSignature signature,
            GuiMessageSource source,
            GuiMessageTag tag,
            CallbackInfo ci) {
        createdAt = LocalDateTime.now();
    }

    @Unique
    @Override
    public LocalDateTime getCreated() {
        return createdAt;
    }
}
