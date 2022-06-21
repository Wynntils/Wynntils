/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayFeature;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.utils.objects.CustomColor;

@OverlayFeature
public class DummyOverlayFeature extends UserFeature {

    @SuppressWarnings("UnusedDeclaration")
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay DummyRedBoxOverlay = new DummyRedBoxOverlay();

    @SuppressWarnings("UnusedDeclaration")
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay DummyBlueBoxOverlay = new DummyBlueBoxOverlay();

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        super.onInit(conditions);
    }

    public static class DummyRedBoxOverlay extends Overlay {
        public DummyRedBoxOverlay() {
            super(new OverlayPosition(100, 100), 100, 100);
        }

        @Override
        public void render(OverlayPosition overlayPosition, PoseStack poseStack, float partialTicks, Window window) {
            RenderUtils.drawRect(
                    new CustomColor(190, 40, 40).withAlpha(255),
                    overlayPosition.getX(),
                    overlayPosition.getY(),
                    0,
                    (int) width,
                    (int) height);
        }
    }

    public static class DummyBlueBoxOverlay extends Overlay {
        public DummyBlueBoxOverlay() {
            super(new OverlayPosition(10, 10), 50, 50);
        }

        @Override
        public void render(OverlayPosition overlayPosition, PoseStack poseStack, float partialTicks, Window window) {
            RenderUtils.drawRect(
                    new CustomColor(40, 40, 190).withAlpha(150),
                    overlayPosition.getX(),
                    overlayPosition.getY(),
                    1,
                    (int) width,
                    (int) height);
        }
    }
}
