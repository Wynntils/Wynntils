/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.abilities.ShamanTotem;
import com.wynntils.models.abilities.event.TotemEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.OVERLAYS)
public class ShamanTotemTrackingFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final ShamanTotemTimerOverlay shamanTotemTimerOverlay = new ShamanTotemTimerOverlay();

    @Config
    public boolean highlightShamanTotems = true;

    @Config
    public static TotemTrackingColors firstTotemColor = TotemTrackingColors.WHITE;

    @Config
    public static TotemTrackingColors secondTotemColor = TotemTrackingColors.AQUA;

    @Config
    public static TotemTrackingColors thirdTotemColor = TotemTrackingColors.RED;

    private static final String TOTEM_HIGHLIGHT_TEAM_BASE = "wynntilsTH";
    private static final int ENTITY_GLOWING_FLAG = 6;

    @SubscribeEvent
    public void onTotemSummoned(TotemEvent.Summoned e) {
        if (!highlightShamanTotems) return;

        int totemNumber = e.getTotemNumber();
        ArmorStand totemAS = e.getTotemEntity();

        TotemTrackingColors color =
                switch (totemNumber) {
                    case 1 -> firstTotemColor;
                    case 2 -> secondTotemColor;
                    case 3 -> thirdTotemColor;
                    default -> throw new IllegalArgumentException(
                            "totemNumber should be 1, 2, or 3! (color switch in #onTotemSummoned in ShamanTotemTrackingFeature");
                };

        // Make or get scoreboard to set highlight colors
        Scoreboard scoreboard = McUtils.mc().level.getScoreboard();
        if (!scoreboard.getTeamNames().contains(TOTEM_HIGHLIGHT_TEAM_BASE + totemNumber)) {
            scoreboard.addPlayerTeam(TOTEM_HIGHLIGHT_TEAM_BASE + totemNumber);
        }
        PlayerTeam team = scoreboard.getPlayerTeam(TOTEM_HIGHLIGHT_TEAM_BASE + totemNumber);
        team.setColor(ChatFormatting.getByCode(color.getColorCode()));

        scoreboard.addPlayerToTeam(totemAS.getStringUUID(), team);
        totemAS.setSharedFlag(ENTITY_GLOWING_FLAG, true); // Makes the totem glow
    }

    @SubscribeEvent
    public void onTotemDestroy(TotemEvent.Removed e) {
        if (!highlightShamanTotems) return;

        // Teams should be destroyed and recreated every cast to allow the user to change totem highlight colors without
        // having to reload the feature
        Scoreboard scoreboard = McUtils.mc().level.getScoreboard();
        if (scoreboard.getTeamNames().contains(TOTEM_HIGHLIGHT_TEAM_BASE + e.getTotemNumber())) {
            scoreboard.removePlayerTeam(scoreboard.getPlayerTeam(TOTEM_HIGHLIGHT_TEAM_BASE + e.getTotemNumber()));
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        shamanTotemTimerOverlay.ticksUntilUpdate--;

        if (shamanTotemTimerOverlay.ticksUntilUpdate <= 0) {
            shamanTotemTimerOverlay.ticksUntilUpdate = ShamanTotemTimerOverlay.TICKS_PER_UPDATE;
            shamanTotemTimerOverlay.updateRenderTaskCache();
        }
    }

    public static class ShamanTotemTimerOverlay extends Overlay {
        static final int TICKS_PER_UPDATE = 5;

        @Config
        public static TotemTrackingDetail totemTrackingDetail = TotemTrackingDetail.COORDS;

        @Config
        public TextShadow textShadow = TextShadow.OUTLINE;

        private TextRenderSetting textRenderSetting;

        int ticksUntilUpdate = 0;
        List<TextRenderTask> renderTaskCache;

        protected ShamanTotemTimerOverlay() {
            super(
                    new OverlayPosition(
                            275,
                            -5,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.TopRight),
                    new GuiScaledOverlaySize(120, 35));

            updateTextRenderSetting();
        }

        @Override
        public void render(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            if (renderTaskCache == null) {
                updateRenderTaskCache();
            }

            BufferedFontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            bufferSource,
                            this.getRenderX(),
                            this.getRenderY(),
                            renderTaskCache,
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        @Override
        public void renderPreview(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            BufferedFontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            bufferSource,
                            this.getRenderX(),
                            this.getRenderY(),
                            List.of(
                                    new TextRenderTask(
                                            getFormattedTotemText(firstTotemColor.getFormatCode() + "Totem 1", " Summoned", ""),
                                            textRenderSetting),
                                    new TextRenderTask(
                                            getFormattedTotemText(
                                                    secondTotemColor.getFormatCode() + "Totem 2", " (01s)", " [-1434, 104, -5823]"),
                                            textRenderSetting),
                                    new TextRenderTask(
                                            getFormattedTotemText(
                                                    thirdTotemColor.getFormatCode() + "Totem 3", " (14s)", " [1, 8, -41]"),
                                            textRenderSetting)),
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        void updateRenderTaskCache() {
            renderTaskCache = Models.ShamanTotem.getActiveTotems().stream()
                    .map(shamanTotem -> {
                        String prefix =
                                switch (shamanTotem.getTotemNumber()) {
                                    case 1 -> firstTotemColor.getFormatCode() + "Totem 1";
                                    case 2 -> secondTotemColor.getFormatCode() + "Totem 2";
                                    case 3 -> thirdTotemColor.getFormatCode() + "Totem 3";
                                    default -> throw new IllegalArgumentException(
                                            "totemNumber should be 1, 2, or 3! (switch in #render in ShamanTotemTrackingFeature");
                                };

                        String suffix = "";
                        String detail = "";
                        // Check if we should be saying "Summoned"
                        if (shamanTotem.getState() == ShamanTotem.TotemState.SUMMONED) {
                            suffix = " Summoned";
                        } else {
                            switch (totemTrackingDetail) {
                                case NONE -> suffix = " (" + shamanTotem.getTime() + " s)";
                                case COORDS -> {
                                    suffix = " (" + shamanTotem.getTime() + " s)";
                                    detail = " " + shamanTotem.getLocation().toString();
                                }
                                case DISTANCE -> suffix = " (" + shamanTotem.getTime() + " s, "
                                        + Math.round(McUtils.player()
                                                .position()
                                                .distanceTo(shamanTotem
                                                        .getLocation()
                                                        .toVec3()))
                                        + " m)";
                            }
                        }
                        return new TextRenderTask(getFormattedTotemText(prefix, suffix, detail), textRenderSetting);
                    })
                    .toList();
        }

        private String getFormattedTotemText(String prefix, String suffix, String detail) {
            String maxFitting = RenderedStringUtils.getMaxFittingText(
                    prefix + suffix + detail,
                    this.getWidth(),
                    FontRenderer.getInstance().getFont());
            if (maxFitting.contains("[")
                    && !maxFitting.contains("]")) { // Detail line did not appear to fit, force break
                return prefix + suffix + "\n" + detail;
            } else { // Fits fine, give normal lines
                return prefix + suffix + detail;
            }
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            updateTextRenderSetting();
        }

        private void updateTextRenderSetting() {
            textRenderSetting = TextRenderSetting.DEFAULT
                    .withMaxWidth(this.getWidth())
                    .withHorizontalAlignment(this.getRenderHorizontalAlignment())
                    .withTextShadow(textShadow);
        }

        public enum TotemTrackingDetail {
            NONE,
            COORDS,
            DISTANCE
        }
    }

    /**
     * This enum exists for two reasons:
     * <p>
     * 1. The previously used ChatFormatting enum does not work as a config. ChatFormatting contains options such as
     *   OBFUSCATED, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC, and RESET. These are not valid colors for a totem highlight.
     * <p>
     * 2. When the config system tries to render the names of ChatFormatting enums, it pulls the formatting code instead
     *   of a proper name. For example, instead of displaying WHITE for white, it will display §f, which is actually just
     *   parsed to an invisible string.
     * <p>
     * This is otherwise a trimmed down version of ChatFormatting with those issues fixed.
     * Oh, I also ordered the colors to a rainbow-ish pattern.
     */
    private enum TotemTrackingColors {
        WHITE('f'),
        DARK_RED('4'),
        RED('c'),
        GOLD('6'),
        YELLOW('e'),
        DARK_GREEN('2'),
        GREEN('a'),
        AQUA('b'),
        DARK_AQUA('3'),
        BLUE('9'),
        DARK_BLUE('1'),
        LIGHT_PURPLE('d'),
        DARK_PURPLE('5'),
        GRAY('7'),
        DARK_GRAY('8'),
        BLACK('0');


        private final char colorCode;

        TotemTrackingColors(char colorCode) {
            this.colorCode = colorCode;
        }

        public char getColorCode() {
            return colorCode;
        }

        public String getFormatCode() {
            return "§" + colorCode;
        }
    }
}
