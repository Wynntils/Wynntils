/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public final class StyledTextUtils {
    public static StyledTextPart createLocationPart(Location location) {
        String locationString = "[%d, %d, %d]".formatted(location.x, location.y, location.z);
        Style style = Style.EMPTY.withColor(ChatFormatting.DARK_AQUA).withUnderlined(true);

        style = style.withClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND, "/compass at " + location.x + " " + location.y + " " + location.z));
        style = style.withHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, Component.translatable("utils.wynntils.component.clickToSetCompass")));

        return new StyledTextPart(locationString, style, null, Style.EMPTY);
    }

    public static StyledText joinLines(List<StyledText> lines) {
        String description = String.join(
                        " ", lines.stream().map(StyledText::getString).toList())
                .replaceAll("\\s+", " ")
                .trim();
        return StyledText.fromString(description).getNormalized();
    }
}
