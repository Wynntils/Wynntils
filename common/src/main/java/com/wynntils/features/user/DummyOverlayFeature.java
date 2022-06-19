/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.annotations.OverlayHolder;
import com.wynntils.core.features.overlays.annotations.RegisteredOverlay;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.utils.objects.CustomColor;

@OverlayHolder
public class DummyOverlayFeature extends UserFeature {
    @RegisteredOverlay
    public static class DummyRedBoxOverlay extends Overlay {
        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            RenderUtils.drawRect(new CustomColor(190, 40, 40).withAlpha(255), 50, 50, 0, 100, 100);
        }
    }

    @RegisteredOverlay(renderAt = RegisteredOverlay.RenderState.Post)
    public static class DummyBlueBoxOverlay extends Overlay {
        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            RenderUtils.drawRect(new CustomColor(40, 40, 190).withAlpha(150), 75, 75, 0, 50, 50);
        }
    }
}
