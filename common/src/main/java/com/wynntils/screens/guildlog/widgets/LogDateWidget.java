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
import java.time.format.FormatStyle;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class LogDateWidget extends AbstractWidget {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault());

    private final StyledText renderText;

    public LogDateWidget(int x, int y, Instant dateTime) {
        super(
                x,
                y,
                Texture.LOG_DATE_ENTRY.width(),
                Texture.LOG_DATE_ENTRY.height(),
                Component.literal("Log Date Widget"));

        ZonedDateTime logZonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        renderText = StyledText.fromString(logZonedDateTime.format(DATE_TIME_FORMATTER));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.LOG_DATE_ENTRY, getX(), getY());

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        renderText,
                        getX() + 4,
                        getY() + height / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
