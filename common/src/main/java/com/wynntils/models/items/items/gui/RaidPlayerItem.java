/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.gambits.type.Gambit;
import java.util.Collections;
import java.util.List;

public class RaidPlayerItem extends GuiItem {
    private final String playerName;
    private final boolean isNickname;
    private final List<Gambit> enabledGambits;

    public RaidPlayerItem(String playerName, boolean isNickname, List<Gambit> enabledGambits) {
        this.playerName = playerName;
        this.isNickname = isNickname;
        this.enabledGambits = enabledGambits;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isNickname() {
        return isNickname;
    }

    public List<Gambit> getEnabledGambits() {
        return Collections.unmodifiableList(enabledGambits);
    }
}
