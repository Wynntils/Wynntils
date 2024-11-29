/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ScrollableContainerProperty;
import com.wynntils.models.guild.type.GuildLogType;
import com.wynntils.utils.EnumUtils;
import java.util.regex.Pattern;

public class GuildLogContainer extends Container implements ScrollableContainerProperty {
    private static final String LOG_PATTERN = ".+'s? Log: ";
    private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("§7Next Page");
    private static final Pattern PREVIOUS_PAGE_PATTERN = Pattern.compile("§7Previous Page");

    private final GuildLogType logType;

    public GuildLogContainer(GuildLogType logType) {
        super(Pattern.compile(LOG_PATTERN + EnumUtils.toNiceString(logType)));

        this.logType = logType;
    }

    @Override
    public Pattern getNextItemPattern() {
        return NEXT_PAGE_PATTERN;
    }

    @Override
    public Pattern getPreviousItemPattern() {
        return PREVIOUS_PAGE_PATTERN;
    }

    @Override
    public int getNextItemSlot() {
        return 45;
    }

    @Override
    public int getPreviousItemSlot() {
        return 18;
    }

    public GuildLogType getLogType() {
        return logType;
    }
}
