/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import static com.wynntils.utils.render.type.HorizontalAlignment.CENTER;
import static com.wynntils.utils.render.type.HorizontalAlignment.LEFT;
import static com.wynntils.utils.render.type.VerticalAlignment.MIDDLE;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
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
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Math;

public class PlayerInfoFeature extends Feature {
    private static final Comparator<PlayerInfo> PLAYER_INFO_COMPARATOR =
            Comparator.comparing(playerInfo -> playerInfo.getProfile().getName(), String::compareToIgnoreCase);

    public static final int DISTANCE_BETWEEN_CATEGORIES = 87;

    public static final int ROLL_WIDTH = 27;

    public static final int HALF_WIDTH = 178;

    public static final int WIDTH = HALF_WIDTH * 2;

    public static final int TOTAL_WIDTH = WIDTH + ROLL_WIDTH * 2;

    public static final int MAX_LENGTH = 73;

    @RegisterConfig
    public final Config<Integer> openingDuration = new Config<>(125);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final PlayerInfoOverlay arrowShieldTrackerOverlay = new PlayerInfoOverlay();

    private class PlayerInfoOverlay extends Overlay {

        private static final double OPENING_DURATION_MILLIS = 125;
        private double animationProgress = 0;
        private long lastTime = -1;

        protected PlayerInfoOverlay() {
            super(
                    new OverlayPosition(0, 0, VerticalAlignment.TOP, LEFT, OverlayPosition.AnchorSection.TOP_MIDDLE),
                    TOTAL_WIDTH,
                    Texture.PLAYER_INFO_OVERLAY.height());
        }

        private List<StyledText> lastPlayers = new ArrayList<>();
        private long nextExecution = 0;

        private List<StyledText> getAvailablePlayers() {
            if (System.currentTimeMillis() < nextExecution && !lastPlayers.isEmpty()) return lastPlayers;

            nextExecution = System.currentTimeMillis() + 250;

            PlayerTabOverlay defaultTabList = McUtils.mc().gui.getTabList();

            lastPlayers = McUtils.player().connection.getListedOnlinePlayers().stream()
                    .sorted(PLAYER_INFO_COMPARATOR)
                    .limit(80)
                    .map(defaultTabList::getNameForDisplay)
                    .map(StyledText::fromComponent)
                    .filter(styledText -> !styledText.contains("§l"))
                    .map(StyledText::getString)
                    .map(styledText -> wrapText(styledText, MAX_LENGTH))
                    .map(styledText -> styledText.replace("§7", "§0"))
                    .map(StyledText::fromString)
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
            if (!McUtils.options().keyPlayerList.isDown() && animationProgress <= 0.0) return;
            double animation = 1;
            if (openingDuration.get() > 0) { // Animation Detection
                animation = getAnimation();
                if (openingDuration.get() == 0 && animationProgress <= 0.0) return;
            }

            //            animation = 0.5;
            RenderUtils.enableScissor(
                    (int) (getRenderX() + ROLL_WIDTH + HALF_WIDTH - HALF_WIDTH * animation),
                    0,
                    (int) (WIDTH * animation),
                    McUtils.mc().getWindow().getScreenHeight());
            FontRenderer fontRenderer = FontRenderer.getInstance();

            RenderUtils.drawTexturedRect(
                    poseStack,
                    Texture.PLAYER_INFO_OVERLAY.resource(),
                    getRenderX() + ROLL_WIDTH,
                    getRenderY(),
                    0,
                    Texture.PLAYER_INFO_OVERLAY.width(),
                    Texture.PLAYER_INFO_OVERLAY.height(),
                    ROLL_WIDTH,
                    0,
                    Texture.PLAYER_INFO_OVERLAY.width(),
                    Texture.PLAYER_INFO_OVERLAY.height(),
                    Texture.PLAYER_INFO_OVERLAY.width(),
                    Texture.PLAYER_INFO_OVERLAY.height());

            CustomColor customColor = CustomColor.fromChatFormatting(ChatFormatting.BLACK);
            float currentDist = getRenderX() + DISTANCE_BETWEEN_CATEGORIES - 5;
            float categoryStart = getRenderY() + 18;
            fontRenderer.renderText(
                    poseStack,
                    StyledText.fromString("Friends"),
                    currentDist,
                    categoryStart,
                    customColor,
                    CENTER,
                    MIDDLE,
                    TextShadow.NONE);
            currentDist += DISTANCE_BETWEEN_CATEGORIES;
            fontRenderer.renderText(
                    poseStack,
                    StyledText.fromString(Models.WorldState.getCurrentWorldName()),
                    currentDist,
                    categoryStart,
                    customColor,
                    CENTER,
                    MIDDLE,
                    TextShadow.NONE);
            currentDist += DISTANCE_BETWEEN_CATEGORIES;
            fontRenderer.renderText(
                    poseStack,
                    StyledText.fromString("Party"),
                    currentDist,
                    categoryStart,
                    customColor,
                    CENTER,
                    MIDDLE,
                    TextShadow.NONE);
            currentDist += DISTANCE_BETWEEN_CATEGORIES;
            fontRenderer.renderText(
                    poseStack,
                    StyledText.fromString("Guild"),
                    currentDist,
                    categoryStart,
                    customColor,
                    CENTER,
                    MIDDLE,
                    TextShadow.NONE);
            List<StyledText> players = getAvailablePlayers();

            for (int i = 0; i < players.size(); i++) {
                int x = i / 19;
                int y = i % 19;

                float xPos = getRenderX() + ROLL_WIDTH + 12 + (DISTANCE_BETWEEN_CATEGORIES * x);
                float yPos = (float) (categoryStart + 15 + (10.35 * y));
                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                players.get(i),
                                xPos,
                                yPos,
                                CustomColor.fromChatFormatting(ChatFormatting.BLACK),
                                LEFT,
                                MIDDLE,
                                TextShadow.NONE);
            }

            RenderUtils.disableScissor();

            float middle = getRenderX() + HALF_WIDTH + ROLL_WIDTH;
            RenderUtils.drawTexturedRect(
                    poseStack,
                    Texture.PLAYER_INFO_OVERLAY.resource(),
                    (float) (middle - ROLL_WIDTH + 2 - HALF_WIDTH * animation),
                    getRenderY(),
                    0,
                    ROLL_WIDTH,
                    Texture.PLAYER_INFO_OVERLAY.height(),
                    0,
                    0,
                    27,
                    Texture.PLAYER_INFO_OVERLAY.height(),
                    Texture.PLAYER_INFO_OVERLAY.width(),
                    Texture.PLAYER_INFO_OVERLAY.height());
            RenderUtils.drawTexturedRect(
                    poseStack,
                    Texture.PLAYER_INFO_OVERLAY.resource(),
                    (float) (middle + HALF_WIDTH * animation),
                    getRenderY(),
                    0,
                    ROLL_WIDTH,
                    Texture.PLAYER_INFO_OVERLAY.height(),
                    0,
                    0,
                    27,
                    Texture.PLAYER_INFO_OVERLAY.height(),
                    Texture.PLAYER_INFO_OVERLAY.width(),
                    Texture.PLAYER_INFO_OVERLAY.height());
        }

        private double getAnimation() {
            double animation;
            if (lastTime == -1) lastTime += System.currentTimeMillis();

            if (McUtils.options().keyPlayerList.isDown()) {
                animationProgress += (System.currentTimeMillis() - lastTime) / (double) openingDuration.get();
                animationProgress = Math.min(1, animationProgress);
            } else if (animationProgress > 0.0) {
                animationProgress -= (System.currentTimeMillis() - lastTime) / (double) openingDuration.get();
                animationProgress = Math.max(0, animationProgress);
            }
            animation = Math.sin((float) (animationProgress * 1f / 2f * Math.PI));

            lastTime = animationProgress <= 0.0 ? -1 : System.currentTimeMillis();
            return animation;
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }
}
