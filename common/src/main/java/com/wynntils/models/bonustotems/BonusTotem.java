/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.bonustotems;

import com.wynntils.models.bonustotems.type.BonusTotemType;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;

public class BonusTotem {
    private final BonusTotemType bonusTotemType;
    private final Position position;
    private final String owner;
    private String timerString;

    public BonusTotem(BonusTotemType bonusTotemType, Position position, String owner) {
        this.bonusTotemType = bonusTotemType;
        this.position = position;
        this.owner = owner;
    }

    public BonusTotemType getBuffTotemType() {
        return bonusTotemType;
    }

    public Position getPosition() {
        return position;
    }

    public String getOwner() {
        return owner;
    }

    public String getTimerString() {
        return timerString;
    }

    public void setTimerString(String timerString) {
        this.timerString = timerString;
    }

    public double getDistanceToPlayer() {
        // y() - 4 because the entity is 4 blocks above the ground
        return Math.sqrt(McUtils.player().distanceToSqr(new Vec3(position.x(), position.y() - 4, position.z())));
    }

    public double getLookAngleDiff() {
        Position playerLook = McUtils.player().getLookAngle();
        double lookAngle = Math.toDegrees(StrictMath.atan2(playerLook.z(), playerLook.x()));

        Position mobTotemLocation = this.getPosition();
        double angle = Math.toDegrees(StrictMath.atan2(
                mobTotemLocation.z() - McUtils.player().getZ(),
                mobTotemLocation.x() - McUtils.player().getX()));

        double angleDiff = lookAngle - angle;
        if (angleDiff < 0) angleDiff = 360 + angleDiff; // Convert negative angles to positive
        // Anglediff is now the angle between the player and the mob totem
        // 0 is straight ahead, 90 is to the left, 180 is behind, 270 is to the right

        return angleDiff;
    }
}
