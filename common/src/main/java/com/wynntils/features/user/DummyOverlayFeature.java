/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.DebugFeature;
import com.wynntils.core.features.overlays.BasicOverlay;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.SectionCoordinates;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.objects.CustomColor;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DummyOverlayFeature extends DebugFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay ComplexRedOverlay = new DummyRedComplexOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    private final Overlay BasicBlueOverlay = new BasicOverlay(
            new OverlayPosition(
                    15,
                    15,
                    OverlayPosition.VerticalAlignment.Top,
                    OverlayPosition.HorizontalAlignment.Left,
                    OverlayPosition.AnchorSection.TopLeft),
            new GuiScaledOverlaySize(75, 75),
            DummyOverlayFeature::renderBasicBlueBox);

    @OverlayInfo(renderType = RenderEvent.ElementType.Crosshair, renderAt = OverlayInfo.RenderState.Replace)
    private final Overlay CrosshairReplacerOverlay = new BasicOverlay(
            new OverlayPosition(
                    0,
                    0,
                    OverlayPosition.VerticalAlignment.Middle,
                    OverlayPosition.HorizontalAlignment.Center,
                    OverlayPosition.AnchorSection.Middle),
            new GuiScaledOverlaySize(2, 2),
            DummyOverlayFeature::renderBasicGreenBox);

    public static class DummyRedComplexOverlay extends Overlay {
        public DummyRedComplexOverlay() {
            super(
                    new OverlayPosition(
                            15,
                            30,
                            OverlayPosition.VerticalAlignment.Bottom,
                            OverlayPosition.HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.Middle),
                    100,
                    100);
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            RenderUtils.drawRect(
                    new CustomColor(190, 40, 40).withAlpha(255),
                    this.getRenderX(),
                    this.getRenderY(),
                    0,
                    (int) this.getWidth(),
                    (int) this.getHeight());
        }
    }

    public static void renderBasicBlueBox(Overlay overlay, PoseStack poseStack, float partialTicks, Window window) {
        RenderUtils.drawRect(
                new CustomColor(40, 40, 190).withAlpha(150),
                overlay.getRenderX(),
                overlay.getRenderY(),
                1,
                (int) overlay.getWidth(),
                (int) overlay.getHeight());
    }

    private static void renderBasicGreenBox(Overlay overlay, PoseStack poseStack, float partialTicks, Window window) {
        RenderUtils.drawRect(
                new CustomColor(70, 190, 70).withAlpha(255),
                overlay.getRenderX(),
                overlay.getRenderY(),
                1,
                (int) overlay.getWidth(),
                (int) overlay.getHeight());
    }

    @SubscribeEvent
    public void renderNinths(RenderEvent.Post event) {
        Window window = McUtils.mc().getWindow();
        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();

        int wT = width / 3;
        int hT = height / 3;

        for (SectionCoordinates section : OverlayManager.getSections()) {
            RenderUtils.drawRect(
                    CustomColor.fromInt(section.hashCode()).withAlpha(75), section.x1(), section.y1(), 0, wT, hT);
        }
    }
}
