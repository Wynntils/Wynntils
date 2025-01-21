/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.core.text.StyledText;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GuildLogItem extends GuiItem {
    private final Instant logInstant;
    private final List<StyledText> logInfo;

    public GuildLogItem(Instant instant, LinkedList<StyledText> logInfo) {
        this.logInstant = instant;
        this.logInfo = new LinkedList<>(logInfo);
    }

    public Instant getLogInstant() {
        return logInstant;
    }

    public List<StyledText> getLogInfo() {
        return Collections.unmodifiableList(logInfo);
    }

    @Override
    public String toString() {
        return "GuildLogItem{" + "logInstant=" + logInstant + ", logInfo=" + logInfo + '}';
    }
}
