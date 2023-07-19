/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
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
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.ThrottledSupplier;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Math;

@ConfigCategory(Category.OVERLAYS)
public class CustomPlayerListFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final CustomPlayerListOverlay customPlayerListOverlay = new CustomPlayerListOverlay();

    @SubscribeEvent
    public void onRender(RenderEvent.Pre event) {
        if (event.getType() == RenderEvent.ElementType.PLAYER_TAB_LIST) {
            event.setCanceled(true);
        }
    }

    private static class CustomPlayerListOverlay extends Overlay {
        private static final Comparator<PlayerInfo> PLAYER_INFO_COMPARATOR =
                Comparator.comparing(playerInfo -> playerInfo.getProfile().getName(), String::compareToIgnoreCase);
        private static final int DISTANCE_BETWEEN_CATEGORIES = 87;
        private static final int ROLL_WIDTH = 27;
        private static final int HALF_WIDTH = 178;
        private static final int WIDTH = HALF_WIDTH * 2;
        private static final int TOTAL_WIDTH = WIDTH + ROLL_WIDTH * 2;
        private static final int MAX_WIDTH = 73;

        @RegisterConfig
        public final Config<Integer> openingDuration = new Config<>(125);

        private final AnimationPercentage animationPercentage = new AnimationPercentage(
                McUtils.options().keyPlayerList::isDown, Duration.of(openingDuration.get(), ChronoUnit.MILLIS));
        private final ThrottledSupplier<List<StyledText>> availablePlayers =
                new ThrottledSupplier<>(CustomPlayerListOverlay::getAvailablePlayers, Duration.ofMillis(250));

        protected CustomPlayerListOverlay() {
            super(
                    new OverlayPosition(
                            0,
                            0,
                            VerticalAlignment.TOP,
                            HorizontalAlignment.CENTER,
                            OverlayPosition.AnchorSection.TOP_MIDDLE),
                    TOTAL_WIDTH,
                    Texture.PLAYER_INFO_OVERLAY.height());
        }

        private static List<StyledText> getAvailablePlayers() {
            PlayerTabOverlay defaultTabList = McUtils.mc().gui.getTabList();

            return McUtils.player().connection.getListedOnlinePlayers().stream()
                    .sorted(PLAYER_INFO_COMPARATOR)
                    .limit(80)
                    .map(defaultTabList::getNameForDisplay)
                    .map(StyledText::fromComponent)
                    .filter(styledText -> !styledText.contains(ChatFormatting.BOLD.toString()))
                    .map(StyledText::getString)
                    .map(styledText -> RenderedStringUtils.cut(styledText, MAX_WIDTH))
                    .map(styledText ->
                            styledText.replace(ChatFormatting.GRAY.toString(), ChatFormatting.BLACK.toString()))
                    .map(StyledText::fromString)
                    .toList();
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
            if (!McUtils.options().keyPlayerList.isDown() && animationPercentage.finishedClosingAnimation()) return;
            renderPlayerList(poseStack, animationPercentage.getAnimation());
        }

        @Override
        public void renderPreview(
                PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
            renderPlayerList(poseStack, 1);
        }

        private void renderPlayerList(PoseStack poseStack, double animation) {
            if (animation < 1) {
                RenderUtils.enableScissor(
                        (int) (getRenderX() + ROLL_WIDTH + HALF_WIDTH - HALF_WIDTH * animation),
                        0,
                        (int) (WIDTH * animation),
                        McUtils.mc().getWindow().getScreenHeight());
            }

            renderBackground(poseStack);

            float currentDist = getRenderX() + ROLL_WIDTH + 55;
            float categoryStart = getRenderY() + 18;
            renderCategoryTitle(poseStack, "Friends", currentDist, categoryStart);
            currentDist += DISTANCE_BETWEEN_CATEGORIES;
            renderCategoryTitle(poseStack, Models.WorldState.getCurrentWorldName(), currentDist, categoryStart);
            currentDist += DISTANCE_BETWEEN_CATEGORIES;
            renderCategoryTitle(poseStack, "Party", currentDist, categoryStart);
            currentDist += DISTANCE_BETWEEN_CATEGORIES;
            renderCategoryTitle(poseStack, "Guild", currentDist, categoryStart);

            renderPlayerNames(poseStack, categoryStart, availablePlayers.get());

            if (animation < 1) {
                RenderUtils.disableScissor();
            }

            float middle = getRenderX() + HALF_WIDTH + ROLL_WIDTH;
            renderRoll(poseStack, (float) (middle - ROLL_WIDTH + 2 - HALF_WIDTH * animation));
            renderRoll(poseStack, (float) (middle - 2 + HALF_WIDTH * animation));
        }

        private void renderRoll(PoseStack poseStack, float xPos) {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    Texture.PLAYER_INFO_OVERLAY.resource(),
                    xPos,
                    getRenderY(),
                    0,
                    ROLL_WIDTH,
                    Texture.PLAYER_INFO_OVERLAY.height(),
                    0,
                    0,
                    ROLL_WIDTH,
                    Texture.PLAYER_INFO_OVERLAY.height(),
                    Texture.PLAYER_INFO_OVERLAY.width(),
                    Texture.PLAYER_INFO_OVERLAY.height());
        }

        private void renderPlayerNames(PoseStack poseStack, float categoryStart, List<StyledText> players) {
            for (int i = 0; i < players.size(); i++) {
                int x = i / 19;
                int y = i % 19;

                float xPos = getRenderX() + ROLL_WIDTH + 12 + (DISTANCE_BETWEEN_CATEGORIES * x);
                float yPos = categoryStart + 14 + (10 * y);
                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                players.get(i),
                                xPos,
                                yPos,
                                CustomColor.fromChatFormatting(ChatFormatting.BLACK),
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.MIDDLE,
                                TextShadow.NONE);
            }
        }

        private void renderCategoryTitle(PoseStack poseStack, String name, float currentDist, float categoryStart) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(name),
                            currentDist,
                            categoryStart,
                            CustomColor.fromChatFormatting(ChatFormatting.BLACK),
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NONE);
        }

        private void renderBackground(PoseStack poseStack) {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    Texture.PLAYER_INFO_OVERLAY.resource(),
                    getRenderX() + ROLL_WIDTH,
                    getRenderY(),
                    0,
                    Texture.PLAYER_INFO_OVERLAY.width() - ROLL_WIDTH,
                    Texture.PLAYER_INFO_OVERLAY.height(),
                    ROLL_WIDTH,
                    0,
                    Texture.PLAYER_INFO_OVERLAY.width() - ROLL_WIDTH,
                    Texture.PLAYER_INFO_OVERLAY.height(),
                    Texture.PLAYER_INFO_OVERLAY.width(),
                    Texture.PLAYER_INFO_OVERLAY.height());
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            animationPercentage.setOpeningDuration(Duration.of(openingDuration.get(), ChronoUnit.MILLIS));
        }
    }

    private static final class AnimationPercentage {
        private final Supplier<Boolean> openingContitions;
        private Duration openingDuration;
        private Instant lastTime = Instant.EPOCH;

        private double openingProgress;

        private AnimationPercentage(Supplier<Boolean> openingContitions, Duration openingDuration) {
            this.openingContitions = openingContitions;
            this.openingDuration = openingDuration;
        }

        private double getAnimation() {
            if (openingDuration.isZero() || openingDuration.isNegative()) return 1;
            if (openingProgress == 0) {
                lastTime = Instant.now().minus(1, ChronoUnit.MILLIS);
            }

            if (openingContitions.get()) {
                addOpeningProgress(
                        Duration.between(lastTime, Instant.now()).toMillis() / ((double) openingDuration.toMillis()));
            } else if (openingProgress > 0) {
                addOpeningProgress(
                        -Duration.between(lastTime, Instant.now()).toMillis() / ((double) openingDuration.toMillis()));
            }
            lastTime = Instant.now();
            return applyTransformation(openingProgress);
        }

        public void addOpeningProgress(double openingProgress) {
            setOpeningProgress(this.openingProgress + openingProgress);
        }

        public void setOpeningProgress(double openingProgress) {
            this.openingProgress = Math.clamp(0, 1, openingProgress);
        }

        private static double applyTransformation(double openingProgress) {
            return java.lang.Math.sin((float) (openingProgress / 2f * java.lang.Math.PI));
        }

        public void setOpeningDuration(Duration openingDuration) {
            this.openingDuration = openingDuration;
        }

        public boolean finishedClosingAnimation() {
            return openingProgress == 0;
        }
    }
}
