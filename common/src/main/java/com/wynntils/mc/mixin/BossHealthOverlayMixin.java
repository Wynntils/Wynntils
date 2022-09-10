/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.EventFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {
    @Mutable
    @Shadow
    public Map<UUID, LerpingBossEvent> events;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private static ResourceLocation GUI_BARS_LOCATION;

    @Shadow
    protected abstract void drawBar(PoseStack poseStack, int x, int y, BossEvent bossEvent);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onCtor(CallbackInfo ci) {
        events = Maps.newConcurrentMap();
    }

    @Inject(
            method = "update(Lnet/minecraft/network/protocol/game/ClientboundBossEventPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void updatePre(ClientboundBossEventPacket packet, CallbackInfo ci) {
        if (EventFactory.onBossHealthUpdate(packet, events).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderPre(PoseStack poseStack, CallbackInfo ci) {
        boolean anyCancelled = false;
        List<LerpingBossEvent> rendered = new ArrayList<>();
        for (LerpingBossEvent value : events.values()) {
            if (!EventFactory.onBossEventRenderPre(value).isCanceled()) {
                rendered.add(value);
            } else {
                anyCancelled = true;
            }
        }

        if (!anyCancelled) {
            return;
        }

        ci.cancel();
        renderBossBars(poseStack, rendered);
    }

    // This is a copy of BossHealthOverlay#render's render code
    private void renderBossBars(PoseStack poseStack, List<LerpingBossEvent> rendered) {
        int i = this.minecraft.getWindow().getGuiScaledWidth();
        int j = 12;

        for (LerpingBossEvent event : rendered) {
            int k = i / 2 - 91;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, GUI_BARS_LOCATION);
            this.drawBar(poseStack, k, j, event);
            Component component = event.getName();
            int m = this.minecraft.font.width(component);
            int n = i / 2 - m / 2;
            int o = j - 9;
            this.minecraft.font.drawShadow(poseStack, component, (float) n, (float) o, 16777215);
            Objects.requireNonNull(this.minecraft.font);
            j += 10 + 9;
            if (j >= this.minecraft.getWindow().getGuiScaledHeight() / 3) {
                break;
            }
        }
    }
}
