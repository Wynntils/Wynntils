/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.event.ScoreboardUpdatedEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.bus.api.SubscribeEvent;

public class ScoreboardOverlay extends Overlay {
    private static final MutableComponent SCOREBOARD_TITLE_COMPONENT = Component.literal("play.wynncraft.com")
            .withStyle(ChatFormatting.BOLD)
            .withStyle(ChatFormatting.GOLD);
    private static final String EMPTY_LINE = "À";

    @Persisted
    private final Config<TextShadow> textShadow = new Config<>(TextShadow.NONE);

    @Persisted
    private final Config<Float> fontScale = new Config<>(1.0f);

    @Persisted
    private final Config<Boolean> renderHeader = new Config<>(true);

    @Persisted
    private final Config<CustomColor> headerBackgroundColor = new Config<>(CustomColor.fromHexString("#00000066"));

    @Persisted
    private final Config<CustomColor> contentBackgroundColor = new Config<>(CustomColor.fromHexString("#00000033"));

    @Persisted
    private final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    private List<StyledText> scoreboardLines = new ArrayList<>();
    private List<StyledText> linesToRender = new ArrayList<>();

    private float longestLineWidth = 0f;
    private float contentHeight = 0f;

    public ScoreboardOverlay() {
        super(
                new OverlayPosition(
                        -2,
                        -1,
                        VerticalAlignment.MIDDLE,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.MIDDLE_RIGHT),
                new OverlaySize(150, 100));
    }

    @Override
    public void render(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        if (linesToRender.isEmpty()) return;

        PoseStack poseStack = guiGraphics.pose();
        FontRenderer fontRenderer = FontRenderer.getInstance();

        float totalUnscaledHeight = fontRenderer.getFont().lineHeight + contentHeight;
        float scaledContentWidth = longestLineWidth * fontScale.get();
        float scaledTotalHeight = totalUnscaledHeight * fontScale.get();

        float x1 = getRenderX();
        float x2 = x1 + getWidth();
        float y1 = getRenderY();
        float y2 = y1 + getHeight();

        float bgX =
                switch (getRenderHorizontalAlignment()) {
                    case RIGHT -> x2 - scaledContentWidth;
                    case CENTER -> (x1 + x2) * 0.5f - scaledContentWidth * 0.5f;
                    case LEFT -> x1;
                };

        float bgY =
                switch (getRenderVerticalAlignment()) {
                    case TOP -> y1;
                    case MIDDLE -> (y1 + y2) * 0.5f + (scaledTotalHeight / 3f) - scaledTotalHeight;
                    case BOTTOM -> y2 - scaledTotalHeight;
                };

        float headerBoxHeight = fontRenderer.getFont().lineHeight * fontScale.get();
        if (renderHeader.get()) {
            RenderUtils.drawRect(
                    poseStack,
                    headerBackgroundColor.get(),
                    bgX - 2f,
                    bgY - 2f,
                    0f,
                    scaledContentWidth + 4f,
                    headerBoxHeight);

            fontRenderer.renderAlignedTextInBox(
                    poseStack,
                    StyledText.fromComponent(SCOREBOARD_TITLE_COMPONENT),
                    bgX - 2f,
                    bgX - 2f + scaledContentWidth + 4f,
                    bgY - 1f,
                    bgY - 1f + headerBoxHeight,
                    scaledContentWidth + 4f,
                    CommonColors.WHITE,
                    HorizontalAlignment.CENTER,
                    VerticalAlignment.BOTTOM,
                    textShadow.get(),
                    fontScale.get());
        }

        RenderUtils.drawRect(
                poseStack,
                contentBackgroundColor.get(),
                bgX - 2f,
                bgY - 2f + headerBoxHeight,
                0f,
                scaledContentWidth + 4f,
                (contentHeight + 1f) * fontScale.get());

        float textX = bgX - 1f;
        float currentY = bgY - 2f + headerBoxHeight + fontScale.get();

        for (StyledText line : linesToRender) {
            fontRenderer.renderText(
                    poseStack,
                    line,
                    textX,
                    currentY,
                    CommonColors.WHITE,
                    HorizontalAlignment.LEFT,
                    VerticalAlignment.TOP,
                    textShadow.get(),
                    fontScale.get());
            currentY += fontRenderer.getFont().lineHeight * fontScale.get();
        }
    }

    @SubscribeEvent
    public void onScoreboardContentSet(ScoreboardUpdatedEvent event) {
        scoreboardLines = new ArrayList<>();

        if (event.getScoreboardSegments().isEmpty()) {
            linesToRender = scoreboardLines;
            return;
        }

        if (renderHeader.get()) {
            scoreboardLines.add(StyledText.fromString("À"));
        }

        for (int i = 0; i < event.getScoreboardSegments().size(); i++) {
            Pair<ScoreboardPart, ScoreboardSegment> pair =
                    event.getScoreboardSegments().get(i);
            ScoreboardSegment segment = pair.value();
            if (!segment.isVisible()) continue;

            scoreboardLines.add(segment.getHeader());
            scoreboardLines.addAll(segment.getContent());

            if (i < event.getScoreboardSegments().size() - 1) {
                scoreboardLines.add(StyledText.fromString("À"));
            }
        }

        updateLines();
    }

    @SubscribeEvent
    public void onScoreboardRender(RenderEvent.Pre event) {
        if (event.getType() != RenderEvent.ElementType.SCOREBOARD) return;
        if (shouldDisplayOriginal.get()) return;

        event.setCanceled(true);
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        updateLines();
    }

    private void updateLines() {
        linesToRender = new ArrayList<>(scoreboardLines);

        Font font = FontRenderer.getInstance().getFont();

        if (!renderHeader.get() && !linesToRender.isEmpty()) {
            String firstLine = linesToRender.getFirst().getString();
            if (firstLine.equals(EMPTY_LINE)) {
                linesToRender.removeFirst();
            }
        }

        longestLineWidth = linesToRender.stream()
                .map(line -> (float) font.width(line.getString()))
                .reduce(0f, Math::max);
        longestLineWidth = Math.max(longestLineWidth, font.width(SCOREBOARD_TITLE_COMPONENT));

        contentHeight = linesToRender.size() * font.lineHeight;
    }
}
