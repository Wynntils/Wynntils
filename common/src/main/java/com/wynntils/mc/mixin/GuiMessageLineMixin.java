/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.extension.GuiMessageLineExtension;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.type.Pair;
import java.time.LocalDateTime;
import java.util.Optional;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMessage.Line.class)
public abstract class GuiMessageLineMixin implements GuiMessageLineExtension {
    @Unique
    private LocalDateTime createdAt;

    @Unique
    private Optional<Pair<Component, Integer>> timestamp;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(
            int addedTime, FormattedCharSequence content, GuiMessageTag tag, boolean endOfEntry, CallbackInfo ci) {
        timestamp = Optional.empty();
    }

    @Unique
    @Override
    public LocalDateTime getCreated() {
        return createdAt;
    }

    @Unique
    @Override
    public void setCreated(LocalDateTime date) {
        createdAt = date;
    }

    @Unique
    @Override
    public Optional<Pair<Component, Integer>> getTimestamp() {
        return timestamp;
    }

    @Unique
    @Override
    public void setTimestamp(Component component) {
        timestamp = Optional.of(
                Pair.of(component, FontRenderer.getInstance().getFont().width(component)));
    }
}
