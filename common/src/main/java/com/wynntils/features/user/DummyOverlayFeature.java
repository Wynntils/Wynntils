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
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.core.features.overlays.annotations.OverlayFeature;
import com.wynntils.core.features.overlays.annotations.RegisteredOverlay;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.utils.objects.CustomColor;

@OverlayFeature
public class DummyOverlayFeature extends UserFeature {
    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        super.onInit(conditions);

        DummyRedBoxOverlay newInstanceRed =
                (DummyRedBoxOverlay) OverlayManager.createNewInstance(DummyRedBoxOverlay.class);
        newInstanceRed.setPos(200, 200);

        DummyBlueBoxOverlay newInstanceBlue = new DummyBlueBoxOverlay(225, 225, 50, 50);
        OverlayManager.addNewInstance(newInstanceBlue);
    }

    @RegisteredOverlay
    public static class DummyRedBoxOverlay extends Overlay {
        public DummyRedBoxOverlay() {
            this.x = 50;
            this.y = 50;
            this.width = 100;
            this.height = 100;
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            RenderUtils.drawRect(new CustomColor(190, 40, 40).withAlpha(255), x, y, 0, width, height);
        }

        public void setPos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @RegisteredOverlay(renderAt = RegisteredOverlay.RenderState.Post)
    public static class DummyBlueBoxOverlay extends Overlay {
        public DummyBlueBoxOverlay() {
            this.x = 75;
            this.y = 75;
            this.width = 50;
            this.height = 50;
        }

        public DummyBlueBoxOverlay(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            RenderUtils.drawRect(new CustomColor(40, 40, 190).withAlpha(150), x, y, 1, width, height);
        }
    }
}
