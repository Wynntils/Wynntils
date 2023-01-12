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
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.TotemRemovedEvent;
import com.wynntils.wynn.event.TotemSummonedEvent;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShamanTotemTrackingFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final ShamanTotemTimerOverlay shamanTotemTimerOverlay = new ShamanTotemTimerOverlay();

    @Config
    public boolean highlightShamanTotems = true;

    @Config
    public static ChatFormatting totem1Color = ChatFormatting.WHITE;

    @Config
    public static ChatFormatting totem2Color = ChatFormatting.AQUA;

    @Config
    public static ChatFormatting totem3Color = ChatFormatting.RED;

    private static final String TOTEM_HIGHLIGHT_TEAM_BASE = "wynntilsTH";

    @SubscribeEvent
    public void onTotemSummoned(TotemSummonedEvent e) {
        if (!highlightShamanTotems) return;

        int totemNumber = e.getTotemNumber();
        ArmorStand totemAS = e.getTotemEntity();

        ChatFormatting color =
                switch (totemNumber) {
                    case 1 -> totem1Color;
                    case 2 -> totem2Color;
                    case 3 -> totem3Color;
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
        totemAS.setSharedFlag(6, true); // Makes the totem glow
    }

    @SubscribeEvent
    public void onTotemDestroy(TotemRemovedEvent e) {
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
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        private TextRenderSetting textRenderSetting;

        protected ShamanTotemTimerOverlay() {
            super(
                    new OverlayPosition(
                            200,
                            -5,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.TopRight),
                    new GuiScaledOverlaySize(100, 35));

            updateTextRenderSetting();
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            FontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            Models.ShamanTotem.getActiveTotems().stream()
                                    .sorted()
                                    .map(shamanTotem -> {
                                        String prefix =
                                                switch (shamanTotem.getTotemNumber()) {
                                                    case 1 -> totem1Color + "Totem 1";
                                                    case 2 -> totem2Color + "Totem 2";
                                                    case 3 -> totem3Color + "Totem 3";
                                                    default -> throw new IllegalArgumentException(
                                                            "totemNumber should be 1, 2, or 3! (color switch in #render in ShamanTotemTrackingFeature");
                                                };
                                        return new TextRenderTask(
                                                prefix + " (00:" + shamanTotem.getTime() + ")", textRenderSetting);
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
                    .renderTextWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            new TextRenderTask(ChatFormatting.WHITE + "Totem 1" + " (00:28)", textRenderSetting),
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
    }
}
