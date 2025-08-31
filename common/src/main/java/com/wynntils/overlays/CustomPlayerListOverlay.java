/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.AnimationPercentage;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.ThrottledSupplier;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.bus.api.SubscribeEvent;

public class CustomPlayerListOverlay extends Overlay {
    private static final Comparator<PlayerInfo> PLAYER_INFO_COMPARATOR =
            Comparator.comparing(playerInfo -> playerInfo.getProfile().getName(), String::compareToIgnoreCase);
    private static final int DISTANCE_BETWEEN_CATEGORIES = 114;
    private static final int ROLL_WIDTH = 32;
    private static final int HALF_WIDTH = 233;
    private static final int WIDTH = HALF_WIDTH * 2;
    private static final int TOTAL_WIDTH = WIDTH + ROLL_WIDTH * 2;
    private static final int MAX_WIDTH = 100;

    @Persisted
    private final Config<Integer> openingDuration = new Config<>(125);

    @Persisted
    private final Config<TextShadow> textShadow = new Config<>(TextShadow.NORMAL);

    private final AnimationPercentage animationPercentage = new AnimationPercentage(
            McUtils.options().keyPlayerList::isDown, Duration.of(openingDuration.get(), ChronoUnit.MILLIS));
    private final ThrottledSupplier<List<StyledText>> availablePlayers =
            new ThrottledSupplier<>(CustomPlayerListOverlay::getAvailablePlayers, Duration.ofMillis(250));

    public CustomPlayerListOverlay() {
        super(
                new OverlayPosition(
                        0,
                        0,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.TOP_MIDDLE),
                TOTAL_WIDTH,
                Texture.PLAYER_LIST_OVERLAY.height());
    }

    @SubscribeEvent
    public void onRender(RenderEvent.Pre event) {
        if (event.getType() == RenderEvent.ElementType.PLAYER_TAB_LIST) {
            event.setCanceled(true);
        }
    }

    @Override
    protected boolean hideWhenNoGui() {
        return false;
    }

    @Override
    protected boolean isVisible() {
        return McUtils.options().keyPlayerList.isDown() || !animationPercentage.finishedClosingAnimation();
    }

    @Override
    public void render(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        renderPlayerList(guiGraphics, animationPercentage.getAnimation());
    }

    @Override
    public void renderPreview(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        renderPlayerList(guiGraphics, 1);
    }

    private static List<StyledText> getAvailablePlayers() {
        PlayerTabOverlay defaultTabList = McUtils.mc().gui.getTabList();

        return McUtils.player().connection.getListedOnlinePlayers().stream()
                .sorted(PLAYER_INFO_COMPARATOR)
                .limit(80)
                .map(defaultTabList::getNameForDisplay)
                .map(StyledText::fromComponent)
                .map(StyledText::trim)
                .map(StyledText::getString)
                .map(styledText -> RenderedStringUtils.substringMaxWidth(styledText, MAX_WIDTH))
                .map(StyledText::fromString)
                .toList();
    }

    private void renderPlayerList(GuiGraphics guiGraphics, double animation) {
        RenderSystem.disableDepthTest();
        PoseStack poseStack = guiGraphics.pose();

        if (animation < 1) {
            RenderUtils.enableScissor(
                    guiGraphics,
                    (int) (getRenderX() + ROLL_WIDTH + HALF_WIDTH - HALF_WIDTH * animation),
                    0,
                    (int) (WIDTH * animation),
                    McUtils.mc().getWindow().getScreenHeight());
        }

        renderBackground(poseStack);

        renderPlayerNames(poseStack, availablePlayers.get());

        if (animation < 1) {
            RenderUtils.disableScissor(guiGraphics);
        }

        float middle = getRenderX() + HALF_WIDTH + ROLL_WIDTH;
        renderRoll(poseStack, (float) (middle - ROLL_WIDTH + 11 - HALF_WIDTH * animation), 0);
        renderRoll(
                poseStack,
                (float) (middle - 11 + HALF_WIDTH * animation),
                Texture.PLAYER_LIST_OVERLAY.width() - ROLL_WIDTH);

        RenderSystem.enableDepthTest();
    }

    private void renderRoll(PoseStack poseStack, float xPos, int uOffset) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.PLAYER_LIST_OVERLAY.resource(),
                xPos,
                getRenderY(),
                0,
                ROLL_WIDTH,
                Texture.PLAYER_LIST_OVERLAY.height(),
                uOffset,
                0,
                ROLL_WIDTH,
                Texture.PLAYER_LIST_OVERLAY.height(),
                Texture.PLAYER_LIST_OVERLAY.width(),
                Texture.PLAYER_LIST_OVERLAY.height());
    }

    private void renderPlayerNames(PoseStack poseStack, List<StyledText> players) {
        for (int i = 0; i < players.size(); i++) {
            int x = i / 20;
            int y = i % 20;

            float xPos = getRenderX() + ROLL_WIDTH + (i % 20 == 0 ? 30 : 12) + (DISTANCE_BETWEEN_CATEGORIES * x);
            float yPos;

            if (i % 20 == 0) {
                yPos = getRenderY() + 16;
            } else {
                yPos = getRenderY() + 18 + 14 + (11 * (y - 1));
            }

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            players.get(i),
                            xPos,
                            yPos,
                            CustomColor.fromChatFormatting(ChatFormatting.BLACK),
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            textShadow.get());
        }
    }

    private void renderBackground(PoseStack poseStack) {
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.PLAYER_LIST_OVERLAY.resource(),
                getRenderX() + ROLL_WIDTH,
                getRenderY(),
                0,
                Texture.PLAYER_LIST_OVERLAY.width() - ROLL_WIDTH,
                Texture.PLAYER_LIST_OVERLAY.height(),
                ROLL_WIDTH,
                0,
                Texture.PLAYER_LIST_OVERLAY.width() - ROLL_WIDTH,
                Texture.PLAYER_LIST_OVERLAY.height(),
                Texture.PLAYER_LIST_OVERLAY.width(),
                Texture.PLAYER_LIST_OVERLAY.height());
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        animationPercentage.setOpeningDuration(Duration.of(openingDuration.get(), ChronoUnit.MILLIS));
    }
}
