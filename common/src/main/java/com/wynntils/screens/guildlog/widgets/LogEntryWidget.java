/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guildlog.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class LogEntryWidget extends AbstractWidget {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendLocalized(FormatStyle.SHORT, FormatStyle.SHORT)
            .appendLiteral(' ')
            .appendPattern("z")
            .toFormatter()
            .withLocale(Locale.getDefault());

    private final List<StyledText> logLines;
    private final StyledText timeText;

    public LogEntryWidget(int x, int y, int height, List<StyledText> logLines, Instant dateTime) {
        super(x, y, Texture.LOG_ENTRY_MIDDLE.width(), height, Component.literal("Log Entry Widget"));

        this.logLines = logLines;

        ZonedDateTime logZonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        timeText = StyledText.fromString(logZonedDateTime.format(DATE_TIME_FORMATTER));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int renderY = getY();

        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.LOG_ENTRY_TOP, getX(), renderY);

        renderY += Texture.LOG_ENTRY_BOTTOM.height();

        for (StyledText logLine : logLines) {
            RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.LOG_ENTRY_MIDDLE, getX(), renderY);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics.pose(),
                            logLine,
                            getX() + 6,
                            renderY,
                            CommonColors.BLACK,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NORMAL);

            renderY += Texture.LOG_ENTRY_MIDDLE.height();
        }

        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.LOG_ENTRY_MIDDLE, getX(), renderY);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        timeText,
                        getX() + width - 6,
                        renderY + 1,
                        CommonColors.LIGHT_GRAY,
                        HorizontalAlignment.RIGHT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        renderY += Texture.LOG_ENTRY_MIDDLE.height();

        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.LOG_ENTRY_BOTTOM, getX(), renderY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
