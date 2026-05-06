/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class ShamanTotemLabelInfo extends LabelInfo {
    private final String playerName;
    private final int regenPerSecond;
    private final String poisonAmount;
    private final int invigorateTime;
    private final int timeLeft;

    protected ShamanTotemLabelInfo(
            StyledText label,
            Location location,
            Entity entity,
            String playerName,
            int regenPerSecond,
            String poisonAmount,
            int invigorateTime,
            int timeLeft) {
        super(label, location, entity);

        this.playerName = playerName;
        this.regenPerSecond = regenPerSecond;
        this.poisonAmount = poisonAmount;
        this.invigorateTime = invigorateTime;
        this.timeLeft = timeLeft;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getRegenPerSecond() {
        return regenPerSecond;
    }

    public String getPoisonAmount() {
        return poisonAmount;
    }

    public int getInvigorateTime() {
        return invigorateTime;
    }

    public int getTimeLeft() {
        return timeLeft;
    }
}
