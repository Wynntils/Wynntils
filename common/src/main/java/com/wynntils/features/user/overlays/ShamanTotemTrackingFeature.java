/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Model;
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
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.gui.render.TextShadow;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.event.TotemEvent;
import com.wynntils.wynn.objects.ShamanTotem;
import java.util.List;
import net.minecraft.ChatFormatting;
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
    public static ChatFormatting firstTotemColor = ChatFormatting.WHITE;

    @Config
    public static ChatFormatting secondTotemColor = ChatFormatting.AQUA;

    @Config
    public static ChatFormatting thirdTotemColor = ChatFormatting.RED;

    private static final String TOTEM_HIGHLIGHT_TEAM_BASE = "wynntilsTH";
    private static final int ENTITY_GLOWING_FLAG = 6;

    @SubscribeEvent
    public void onTotemSummoned(TotemEvent.Summoned e) {
        if (!highlightShamanTotems) return;

        int totemNumber = e.getTotemNumber();
        ArmorStand totemAS = e.getTotemEntity();

        ChatFormatting color =
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
        team.setColor(color);

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

    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.Spell, Models.ShamanTotem);
    }

    public static class ShamanTotemTimerOverlay extends Overlay {
        @Config
        public static TotemTrackingDetail totemTrackingDetail = TotemTrackingDetail.COORDS;

        @Config
        public TextShadow textShadow = TextShadow.OUTLINE;

        private TextRenderSetting textRenderSetting;

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

        private String getFormattedTotemText(String prefix, String suffix, String detail) {
            String maxFitting = StringUtils.getMaxFittingText(
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
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            FontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            Models.ShamanTotem.getActiveTotems().stream()
                                    .map(shamanTotem -> {
                                        String prefix =
                                                switch (shamanTotem.getTotemNumber()) {
                                                    case 1 -> firstTotemColor + "Totem 1";
                                                    case 2 -> secondTotemColor + "Totem 2";
                                                    case 3 -> thirdTotemColor + "Totem 3";
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
                                                case NONE -> suffix = " (" + shamanTotem.getTime() + " s";
                                                case COORDS -> {
                                                    suffix = " (" + shamanTotem.getTime() + " s";
                                                    detail = shamanTotem
                                                            .getLocation()
                                                            .toString();
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
                                        return new TextRenderTask(
                                                getFormattedTotemText(prefix, suffix, detail), textRenderSetting);
                                    })
                                    .toList(),
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
                            List.of(
                                    new TextRenderTask(
                                            getFormattedTotemText(firstTotemColor + "Totem 1", " Summoned", ""),
                                            textRenderSetting),
                                    new TextRenderTask(
                                            getFormattedTotemText(
                                                    secondTotemColor + "Totem 2", " (01s)", " [-1434, 104, -5823]"),
                                            textRenderSetting),
                                    new TextRenderTask(
                                            getFormattedTotemText(
                                                    thirdTotemColor + "Totem 3", " (14s)", " [1, 8, -41]"),
                                            textRenderSetting)),
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
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
}
