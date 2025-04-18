/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.GuildLogItem;
import com.wynntils.utils.mc.LoreUtils;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuildLogAnnotator implements GuiItemAnnotator {
    private static final Pattern LOG_NAME_PATTERN = Pattern.compile(
            "§3(\\d+) ((month|week|day|hour|minute|second)s?) ago §8\\((?<month>\\d+)\\/(?<day>\\d+)\\/(?<year>\\d+) (?<hour>\\d+):(?<minute>\\d+) (AM|PM) (EST|EDT)\\)");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/yy hh:mm a", Locale.ROOT);
    private static final ZoneId LOG_ZONE = ZoneId.of("America/New_York");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (itemStack.getItem() != Items.PAPER) return null;

        Matcher matcher = name.getMatcher(LOG_NAME_PATTERN);

        if (matcher.matches()) {
            String month = matcher.group("month");
            String day = matcher.group("day");
            String year = matcher.group("year");
            String hour = matcher.group("hour");
            String minute = matcher.group("minute");
            String amPm = matcher.group(9);
            StringBuilder dateTimeStr = new StringBuilder(month)
                    .append("/")
                    .append(day)
                    .append("/")
                    .append(year)
                    .append(" ")
                    .append(hour)
                    .append(":")
                    .append(minute)
                    .append(" ")
                    .append(amPm);

            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);

            ZonedDateTime estZonedDateTime = localDateTime.atZone(LOG_ZONE);

            LinkedList<StyledText> unpaddedLore = new LinkedList<>(LoreUtils.getLore(itemStack));

            // Remove the first and last line which are always empty
            unpaddedLore.removeFirst();
            unpaddedLore.removeLast();

            return new GuildLogItem(estZonedDateTime.toInstant(), unpaddedLore);
        }

        return null;
    }
}
