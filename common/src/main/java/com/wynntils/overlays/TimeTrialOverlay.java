/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynntilsTimeTrialsFont;
import com.wynntils.models.timetrials.type.TimeTrialInfo;
import com.wynntils.models.timetrials.type.TimeTrialState;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class TimeTrialOverlay extends Overlay {
    private static final float LINE_SPACING = 11f;

    @Persisted
    private final Config<TextShadow> textShadow = new Config<>(TextShadow.NORMAL);

    @Persisted
    private final Config<Float> fontScale = new Config<>(1.0f);

    @Persisted
    private final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    private List<StyledText> lines = new ArrayList<>();

    public TimeTrialOverlay() {
        super(
                new OverlayPosition(
                        153,
                        30,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.TOP_LEFT),
                new OverlaySize(50, 30),
                HorizontalAlignment.LEFT,
                VerticalAlignment.TOP);
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        Models.TimeTrial.setHideTrialStats(Managers.Overlay.isEnabled(this) && !this.shouldDisplayOriginal.get());
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, Window window) {
        if (lines.isEmpty()) return;

        float scale = fontScale.get();
        float lineSpacing = LINE_SPACING * scale;
        float totalHeight =
                (lines.size() - 1) * lineSpacing + FontRenderer.getInstance().getFont().lineHeight * scale;

        HorizontalAlignment horizontalAlignment = this.getRenderHorizontalAlignment();
        float renderX =
                switch (horizontalAlignment) {
                    case LEFT -> this.getRenderX();
                    case CENTER -> this.getRenderX() + this.getWidth() / 2f;
                    case RIGHT -> this.getRenderX() + this.getWidth();
                };

        float renderY =
                switch (this.getRenderVerticalAlignment()) {
                    case TOP -> this.getRenderY();
                    case MIDDLE -> this.getRenderY() + (this.getHeight() - totalHeight) / 2f;
                    case BOTTOM -> this.getRenderY() + this.getHeight() - totalHeight;
                };

        for (StyledText line : lines) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            line,
                            renderX,
                            renderY,
                            CommonColors.WHITE,
                            horizontalAlignment,
                            VerticalAlignment.TOP,
                            textShadow.get(),
                            scale);
            renderY += lineSpacing;
        }
    }

    @Override
    public void renderPreview(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, Window window) {
        List<StyledText> previewLines = new ArrayList<>();

        MutableComponent elapsedTimeLine = Component.empty()
                .append(WynnFont.asFont("elapsed_time", WynntilsTimeTrialsFont.class))
                .append(Component.literal(" "))
                .append(Component.literal(formatTime(21657))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TIME_TRIALS)));
        previewLines.add(StyledText.fromComponent(elapsedTimeLine));

        MutableComponent checkpointsLine = Component.empty()
                .append(Component.literal("\uDAFF\uDFFF").withStyle(CommonStyles.SPACE))
                .append(WynnFont.asFont("checkpoints", WynntilsTimeTrialsFont.class))
                .append(Component.literal(" "))
                .append(Component.literal("13/36").withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TIME_TRIALS)));
        previewLines.add(StyledText.fromComponent(checkpointsLine));

        MutableComponent previousCheckpointLine = Component.empty()
                .append(Component.literal("\uDB00\uDC0C").withStyle(CommonStyles.SPACE))
                .append(Component.literal(formatTime(3470))
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_TIME_TRIALS)
                                .withColor(ChatFormatting.GRAY)));
        previewLines.add(StyledText.fromComponent(previousCheckpointLine));

        float scale = fontScale.get();
        float lineSpacing = LINE_SPACING * scale;
        float totalHeight = (previewLines.size() - 1) * lineSpacing
                + FontRenderer.getInstance().getFont().lineHeight * scale;

        HorizontalAlignment horizontalAlignment = this.getRenderHorizontalAlignment();
        float renderX =
                switch (horizontalAlignment) {
                    case LEFT -> this.getRenderX();
                    case CENTER -> this.getRenderX() + this.getWidth() / 2f;
                    case RIGHT -> this.getRenderX() + this.getWidth();
                };

        float renderY =
                switch (this.getRenderVerticalAlignment()) {
                    case TOP -> this.getRenderY();
                    case MIDDLE -> this.getRenderY() + (this.getHeight() - totalHeight) / 2f;
                    case BOTTOM -> this.getRenderY() + this.getHeight() - totalHeight;
                };

        for (StyledText line : previewLines) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            line,
                            renderX,
                            renderY,
                            CommonColors.WHITE,
                            horizontalAlignment,
                            VerticalAlignment.TOP,
                            textShadow.get(),
                            scale);
            renderY += lineSpacing;
        }
    }

    @Override
    public void tick() {
        TimeTrialInfo timeTrialInfo = Models.TimeTrial.getCurrentTimeTrial();
        if (timeTrialInfo == null) return;

        List<StyledText> newLines = new ArrayList<>();

        MutableComponent elapsedTimeLine = Component.empty()
                .append(WynnFont.asFont("elapsed_time", WynntilsTimeTrialsFont.class))
                .append(Component.literal(" "))
                .append(Component.literal(formatTime(timeTrialInfo.getElapsedTime()))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TIME_TRIALS)));
        newLines.add(StyledText.fromComponent(elapsedTimeLine));

        MutableComponent checkpointsLine = Component.empty()
                .append(Component.literal("\uDAFF\uDFFF").withStyle(CommonStyles.SPACE))
                .append(WynnFont.asFont("checkpoints", WynntilsTimeTrialsFont.class))
                .append(Component.literal(" "))
                .append(Component.literal(timeTrialInfo.getCheckpoints().toString())
                        .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TIME_TRIALS)));
        newLines.add(StyledText.fromComponent(checkpointsLine));

        if (Models.TimeTrial.getPreviousCheckpointTime() > 0) {
            MutableComponent previousCheckpointLine = Component.empty()
                    .append(Component.literal("\uDB00\uDC0C").withStyle(CommonStyles.SPACE))
                    .append(Component.literal(formatTime(Models.TimeTrial.getPreviousCheckpointTime()))
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.WYNNTILS_TIME_TRIALS)
                                    .withColor(ChatFormatting.GRAY)));
            newLines.add(StyledText.fromComponent(previousCheckpointLine));
        }

        lines = newLines;
    }

    @Override
    public boolean isVisible() {
        return Models.TimeTrial.getState() == TimeTrialState.IN_PROGRESS;
    }

    private static String formatTime(long milliseconds) {
        long minutes = milliseconds / 60_000;
        long seconds = (milliseconds % 60_000) / 1_000;
        long centiseconds = (milliseconds % 1_000) / 10;

        return minutes > 0
                ? String.format("%d:%02d.%02d", minutes, seconds, centiseconds)
                : String.format("%d.%02d", seconds, centiseconds);
    }
}
