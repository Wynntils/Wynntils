/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.TextRenderSetting;
import com.wynntils.mc.render.TextRenderTask;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.utils.objects.CustomColor;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.ChatFormatting;

@FeatureInfo(category = "Overlays")
public class InfoBoxesFeature extends UserFeature {

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox1Overlay = new InfoBoxOverlay1();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox2Overlay = new InfoBoxOverlay2();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox3Overlay = new InfoBoxOverlay3();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox4Overlay = new InfoBoxOverlay4();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox5Overlay = new InfoBoxOverlay5();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox6Overlay = new InfoBoxOverlay6();

    public abstract static class InfoBoxOverlay extends Overlay {
        private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("((?=&))");

        @Config
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config
        public String content = "";

        private List<TextRenderTask> toRender = getRenderTasks();

        protected InfoBoxOverlay(int id) {
            super(
                    new OverlayPosition(
                            -80 + (15 * id),
                            5,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Left,
                            OverlayPosition.AnchorSection.MiddleLeft),
                    new GuiScaledOverlaySize(100, 12),
                    HorizontalAlignment.Left,
                    VerticalAlignment.Bottom);
        }

        private List<TextRenderTask> getRenderTasks() {
            List<TextRenderTask> renderTaskList = new ArrayList<>();
            String[] contentList = COLOR_CODE_PATTERN.split(content);
            CustomColor lastColor = CustomColor.fromChatFormatting(ChatFormatting.WHITE);
            for (String parsedContent : contentList) {
                if (parsedContent.isEmpty()) continue;

                char color = parsedContent.charAt(1);
                ChatFormatting cf = ChatFormatting.getByCode(color);
                int substringLength = 0;
                if (cf != null && parsedContent.charAt(0) == '&') { // Update lastColor if valid color code; else it will use last valid color code or
                    // white by default
                    lastColor = CustomColor.fromChatFormatting(cf);
                    substringLength = 2;
                }
                renderTaskList.add(new TextRenderTask(
                        parsedContent.substring(substringLength),
                        TextRenderSetting.getWithHorizontalAlignment(
                                        this.getWidth(), lastColor, this.getRenderHorizontalAlignment())
                                .withTextShadow(textShadow)));
            }
            return renderTaskList;
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            toRender = getRenderTasks();
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            FontRenderer.getInstance()
                    .renderMulticolorTextWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            toRender,
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        @Override
        public void renderPreview(PoseStack poseStack, float partialTicks, Window window) {
            FontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            toRender,
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }
    }

    public static class InfoBoxOverlay1 extends InfoBoxOverlay {
        public InfoBoxOverlay1() {
            super(1);
        }
    }

    public static class InfoBoxOverlay2 extends InfoBoxOverlay {
        public InfoBoxOverlay2() {
            super(2);
        }
    }

    public static class InfoBoxOverlay3 extends InfoBoxOverlay {
        public InfoBoxOverlay3() {
            super(3);
        }
    }

    public static class InfoBoxOverlay4 extends InfoBoxOverlay {
        public InfoBoxOverlay4() {
            super(4);
        }
    }

    public static class InfoBoxOverlay5 extends InfoBoxOverlay {
        public InfoBoxOverlay5() {
            super(5);
        }
    }

    public static class InfoBoxOverlay6 extends InfoBoxOverlay {
        public InfoBoxOverlay6() {
            super(6);
        }
    }
}
