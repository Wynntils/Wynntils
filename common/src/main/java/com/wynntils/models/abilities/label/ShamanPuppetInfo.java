/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class ShamanPuppetInfo extends LabelInfo {
    private final int secondsLeft;
    private final String playerName;
    private final int invigorateTime;
    private final int friendlyFireTime;

    protected ShamanPuppetInfo(
            StyledText label,
            Location location,
            Entity entity,
            int secondsLeft,
            String playerName,
            int invigorateTime,
            int friendlyFireTime) {
        super(label, location, entity);

        this.secondsLeft = secondsLeft;
        this.playerName = playerName;
        this.invigorateTime = invigorateTime;
        this.friendlyFireTime = friendlyFireTime;
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getInvigorateTime() {
        return invigorateTime;
    }

    public int getFriendlyFireTime() {
        return friendlyFireTime;
    }

    @Override
    public String toString() {
        return "ShamanPuppetInfo{" + "secondsLeft="
                + secondsLeft + ", playerName="
                + playerName + ", invigorateTime=" + invigorateTime + ", friendlyFireTime=" + friendlyFireTime
                + ", label="
                + label + ", name='"
                + name + '\''
                + ", location=" + location
                + ", entity=" + entity + '}';
    }
}
