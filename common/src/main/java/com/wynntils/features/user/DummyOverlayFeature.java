/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.OverlayBase;
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.Overlay;
import com.wynntils.core.features.overlays.annotations.OverlayFeature;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.utils.objects.CustomColor;

@OverlayFeature
public class DummyOverlayFeature extends UserFeature {

    @Config(displayName = "Blue overlay position", visible = false)
    OverlayPosition bluePosition = new OverlayPosition(150, 150);

    @Config(displayName = "Red overlay position", visible = false)
    OverlayPosition redPosition = new OverlayPosition(50, 50);

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        super.onInit(conditions);

        OverlayManager.registerOverlay(this, new DummyBlueBoxOverlay(), bluePosition);
        OverlayManager.registerOverlay(this, new DummyRedBoxOverlay(), redPosition);
    }

    @Overlay(renderType = RenderEvent.ElementType.GUI)
    public static class DummyRedBoxOverlay extends OverlayBase {
        public DummyRedBoxOverlay() {
            this.width = 100;
            this.height = 100;
        }

        @Override
        public void render(OverlayPosition overlayPosition, PoseStack poseStack, float partialTicks, Window window) {
            RenderUtils.drawRect(
                    new CustomColor(190, 40, 40).withAlpha(255),
                    overlayPosition.getX(),
                    overlayPosition.getY(),
                    0,
                    width,
                    height);
        }
    }

    @Overlay(renderType = RenderEvent.ElementType.GUI, renderAt = Overlay.RenderState.Post)
    public static class DummyBlueBoxOverlay extends OverlayBase {
        public DummyBlueBoxOverlay() {
            this.width = 50;
            this.height = 50;
        }

        public DummyBlueBoxOverlay(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void render(OverlayPosition overlayPosition, PoseStack poseStack, float partialTicks, Window window) {
            RenderUtils.drawRect(
                    new CustomColor(40, 40, 190).withAlpha(150),
                    overlayPosition.getX(),
                    overlayPosition.getY(),
                    1,
                    width,
                    height);
        }
    }
}
