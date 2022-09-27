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
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.FunctionManager;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.objects.EmeraldSymbols;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;

@FeatureInfo(category = FeatureCategory.OVERLAYS)
public class InfoBoxFeature extends UserFeature {

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox1Overlay = new InfoBoxOverlay(1);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox2Overlay = new InfoBoxOverlay(2);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox3Overlay = new InfoBoxOverlay(3);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox4Overlay = new InfoBoxOverlay(4);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox5Overlay = new InfoBoxOverlay(5);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay infoBox6Overlay = new InfoBoxOverlay(6);

    private static final Pattern INFO_VARIABLE_PATTERN =
            Pattern.compile("%([a-zA-Z_]+|%)%|\\\\([\\\\n%§EBLMH]|x[\\dA-Fa-f]{2}|u[\\dA-Fa-f]{4}|U[\\dA-Fa-f]{8})");

    public static class InfoBoxOverlay extends Overlay {
        @Config
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config
        public String content = "";

        private final int id;

        private TextRenderTask toRender = recalculateFunctions(content);
        private List<Function<?>> functionDependencies = new ArrayList<>();

        public InfoBoxOverlay(int id) {
            super(
                    new OverlayPosition(
                            -60 + (15 * id),
                            5,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Left,
                            OverlayPosition.AnchorSection.MiddleLeft),
                    new GuiScaledOverlaySize(120, 10),
                    HorizontalAlignment.Left,
                    VerticalAlignment.Middle);
            this.id = id;
        }

        private TextRenderTask recalculateFunctions(String renderableText) {
            if (functionDependencies == null) {
                functionDependencies = new ArrayList<>();
            }

            for (Function<?> oldDependency : functionDependencies) {
                FunctionManager.disableFunction(oldDependency);
            }

            functionDependencies.clear();

            StringBuilder builder = new StringBuilder(renderableText.length() + 10);
            Matcher m = INFO_VARIABLE_PATTERN.matcher(renderableText);
            while (m.find()) {
                String replacement = null;
                if (m.group(1) != null && FunctionManager.forName(m.group(1)).isPresent()) {
                    // %variable%
                    Function<?> function = FunctionManager.forName(m.group(1)).get();

                    FunctionManager.enableFunction(function);
                    functionDependencies.add(function);

                    replacement = FunctionManager.getRawValueString(function, "");
                } else if (m.group(2) != null) {
                    // \escape
                    replacement = doEscapeFormat(m.group(2));
                }
                if (replacement == null) {
                    replacement = m.group(0);
                }

                m.appendReplacement(builder, replacement);
            }
            m.appendTail(builder);

            return new TextRenderTask(
                    parseColorCodes(builder.toString()),
                    TextRenderSetting.DEFAULT
                            .withHorizontalAlignment(this.getRenderHorizontalAlignment())
                            .withMaxWidth(this.getWidth())
                            .withCustomColor(CustomColor.fromChatFormatting(ChatFormatting.WHITE))
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
            return sb.toString();
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
            toRender = recalculateFunctions(content);
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

            // FIXME: We do re-calculate this on render, but this is preview only, and fixing this would need a lot of
            // architectural changes at the moment
            TextRenderTask toRenderPreview = recalculateFunctions("&cX: %x%, &9Y: %y%, &aZ: %z%");

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

        @Override
        public String getTranslatedName() {
            return I18n.get(
                    "feature.wynntils." + getDeclaringFeatureNameCamelCase() + ".overlay." + getNameCamelCase()
                            + ".name",
                    id);
        }

        @Override
        public String getConfigJsonName() {
            return super.getConfigJsonName() + id;
        }
    }
}
