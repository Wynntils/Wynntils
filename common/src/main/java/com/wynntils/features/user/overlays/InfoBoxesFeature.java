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
import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.FunctionManager;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.TextRenderSetting;
import com.wynntils.mc.render.TextRenderTask;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.wynn.objects.EmeraldSymbols;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraft.ChatFormatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern INFO_VARIABLE_PATTERN = Pattern.compile("%([a-zA-Z_]+|%)%|\\\\([\\\\n%§EBLMH]|x[\\dA-Fa-f]{2}|u[\\dA-Fa-f]{4}|U[\\dA-Fa-f]{8})");

    public abstract static class InfoBoxOverlay extends Overlay {
        @Config
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config
        public String content = "";

        private TextRenderTask toRender = getRenderTask(content);
        private final TextRenderTask toRenderPreview = getRenderTask("&cX: %x%, &9Y: %y%, &aZ: %z%");

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

        private TextRenderTask getRenderTask(String renderableText) {
            StringBuffer buffer = new StringBuffer(renderableText.length() + 10);
            Matcher m = INFO_VARIABLE_PATTERN.matcher(renderableText);
            while (m.find()) {
                String replacement = null;
                if (m.group(1) != null && FunctionManager.forName(m.group(1)).isPresent()) {
                    // %variable%
                    Function<?> function = FunctionManager.forName(m.group(1)).get();
                    replacement = FunctionManager.getRawValueString(function, "");
                } else if (m.group(2) != null) {
                    // \escape
                    replacement = doEscapeFormat(m.group(2));
                }
                if (replacement == null) {
                    replacement = m.group(0);
                }
                m.appendReplacement(buffer, replacement);
            }
            m.appendTail(buffer);

            return new TextRenderTask(
                    parseColorCodes(buffer.toString()),
                    TextRenderSetting.getWithHorizontalAlignment(
                                    this.getWidth(),
                                    CustomColor.fromChatFormatting(ChatFormatting.WHITE),
                                    this.getRenderHorizontalAlignment())
                            .withTextShadow(textShadow));
        }

        private String parseColorCodes(String toProcess) {
            // For every & symbol, check if the next symbol is a color code and if so, replace it with §
            // But don't do it if a \ precedes the &
            String validColors = "0123456789abcdefklmnor";
            StringBuilder sb = new StringBuilder(toProcess);
            for (int i = 0; i < sb.length(); i++) {
                if (sb.charAt(i) == '&') { // char == &
                    if (i + 1 < sb.length()
                            && validColors.contains(String.valueOf(sb.charAt(i + 1)))) { // char after is valid color
                        if (i - 1 < 0 || sb.charAt(i - 1) != '\\') { // & is first char || char before is not \
                            sb.setCharAt(i, '§');
                        } else if (sb.charAt(i - 1) == '\\') { // & is preceded by \, just remove the \
                            sb.deleteCharAt(i - 1);
                        }
                    }
                }
            }
            return toProcess;
        }

        private String doEscapeFormat(String escaped) {
            return switch (escaped) {
                case "\\" -> "\\\\";
                case "n" -> "\n";
                case "%" -> "%";
                case "§" -> "&";
                case "E" -> EmeraldSymbols.E_STRING;
                case "B" -> EmeraldSymbols.B_STRING;
                case "L" -> EmeraldSymbols.L_STRING;
                case "M" -> "✺";
                case "H" -> "❤";
                default -> null;
            };
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            toRender = getRenderTask(content);
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;
            FontRenderer.getInstance()
                    .renderTextWithAlignment(
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
            if (!WynnUtils.onWorld()) return;
            FontRenderer.getInstance()
                    .renderTextWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            toRenderPreview,
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
