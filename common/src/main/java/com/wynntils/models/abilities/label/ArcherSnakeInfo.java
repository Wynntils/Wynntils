/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class ArcherSnakeInfo extends LabelInfo {
    private final int secondsLeft;
    private final String playerName;

    protected ArcherSnakeInfo(StyledText label, Location location, Entity entity, int secondsLeft, String playerName) {
        super(label, location, entity);

        this.secondsLeft = secondsLeft;
        this.playerName = playerName;
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public String toString() {
        return "ArcherSnakeInfo{"
                + "secondsLeft=" + secondsLeft
                + "playerName=" + playerName
                + ", label=" + label
                + ", name='" + name + '\''
                + ", location=" + location
                + ", entity=" + entity + '}';
    }
}
