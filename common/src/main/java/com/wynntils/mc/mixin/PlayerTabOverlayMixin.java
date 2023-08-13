/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(PoseStack poseStack, int width, Scoreboard scoreboard, Objective objective, CallbackInfo ci) {
        RenderEvent.Pre renderEvent =
                new RenderEvent.Pre(poseStack, 0, McUtils.window(), RenderEvent.ElementType.PLAYER_TAB_LIST);
        MixinHelper.post(renderEvent);
        if (renderEvent.isCanceled()) {
            ci.cancel();
        }
    }
}
