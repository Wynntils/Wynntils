/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.DynamicOverlay;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerInfoFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final PlayerInfoOverlay arrowShieldTrackerOverlay = new PlayerInfoOverlay();

    private static class PlayerInfoOverlay extends Overlay {

        private static final double OPENING_DURATION_MILLIS = 125;
        private double animationProgress = 0;
        private long lastTime = -1;

        protected PlayerInfoOverlay() {
            super(
                    new OverlayPosition(
                            10,
                            0,
                            VerticalAlignment.MIDDLE,
                            HorizontalAlignment.CENTER,
                            OverlayPosition.AnchorSection.TOP_MIDDLE),
                    512,
                    256
                    );
        }

        private List<StyledText> lastPlayers = new ArrayList<>();
        private long nextExecution = 0;

        private List<StyledText> getAvailablePlayers() {
            if (System.currentTimeMillis() < nextExecution && !lastPlayers.isEmpty()) return lastPlayers;

            nextExecution = System.currentTimeMillis() + 250;

            lastPlayers = McUtils.player().connection.getListedOnlinePlayers().stream()
                    .map(playerInfo -> Optional.ofNullable(playerInfo.getTabListDisplayName())
                            .map(StyledText::fromComponent)
                            .orElse(StyledText.fromString(
                                    playerInfo.getProfile().getName())))
                    .toList();

            return lastPlayers;
        }

        private static String wrapText(String input, int maxLength) {
            if (McUtils.mc().font.width(input) <= maxLength) return input;

            StringBuilder builder = new StringBuilder();
            for (char c : input.toCharArray()) {
                if (McUtils.mc().font.width(builder.toString() + c) > maxLength) break;

                builder.append(c);
            }

            return builder.toString();
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString("test"),
                            0,
                            0,
                            CustomColor.fromChatFormatting(ChatFormatting.BLACK),
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NONE);
            if (!McUtils.options().keyPlayerList.isDown() && animationProgress <= 0.0) return;
            if (lastTime == -1) lastTime = System.currentTimeMillis();

            if (McUtils.options().keyPlayerList.isDown()) {
                animationProgress += (System.currentTimeMillis() - lastTime) / OPENING_DURATION_MILLIS;
                animationProgress = Math.min(1, animationProgress);
            } else if (animationProgress > 0.0) {
                animationProgress -= (System.currentTimeMillis() - lastTime) / OPENING_DURATION_MILLIS;
                animationProgress = Math.min(0, animationProgress);
            }
            double animation = Math.sin((float) (animationProgress * 1f / 2f * Math.PI));

            lastTime = animationProgress <= 0.0 ? -1 : System.currentTimeMillis();

            //            if (animationProgress <= 0.0) return;
            System.out.println(animationProgress);
            RenderUtils.drawTexturedRect(poseStack, Texture.PLAYER_INFO_OVERLAY, 0, 0);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString("Friends"),
                            -124,
                            7,
                            CustomColor.fromChatFormatting(ChatFormatting.BLACK),
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NONE);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString("Party"),
                            47,
                            7,
                            CustomColor.fromChatFormatting(ChatFormatting.BLACK),
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NONE);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString("Guild"),
                            133,
                            7,
                            CustomColor.fromChatFormatting(ChatFormatting.BLACK),
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NONE);
            List<StyledText> players = getAvailablePlayers();
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 20; y++) {
                    int position = (x * 20) + (y + 1);

                    if (players.size() < position) break; // not enough players

                    StyledText entry = players.get(position - 1);
                    if (entry.contains("§l")) continue; // avoid the titles

                    int xPos = -166 + (87 * x);
                    int yPos = 11 + (10 * y);
                    FontRenderer.getInstance()
                            .renderText(
                                    poseStack,
                                    entry,
                                    xPos,
                                    yPos,
                                    CustomColor.fromChatFormatting(ChatFormatting.BLACK),
                                    HorizontalAlignment.LEFT,
                                    VerticalAlignment.MIDDLE,
                                    TextShadow.NONE);
                }
            }

            int x = (int) (177 * animation);
            int y = (int) (27 + (177 * animation));
            RenderUtils.drawTexturedRect(
                    poseStack,
                    Texture.PLAYER_INFO_OVERLAY.resource(),
                    x,
                    -5,
                    0,
                    y - x,
                    234,
                    0,
                    0,
                    27,
                    229,
                    Texture.PLAYER_INFO_OVERLAY.width(),
                    Texture.PLAYER_INFO_OVERLAY.height());
            RenderUtils.drawTexturedRect(
                    poseStack,
                    Texture.PLAYER_INFO_OVERLAY.resource(),
                    -y,
                    -5,
                    0,
                    -x + y,
                    234,
                    0,
                    0,
                    27,
                    229,
                    Texture.PLAYER_INFO_OVERLAY.width(),
                    Texture.PLAYER_INFO_OVERLAY.height());
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }
}
