/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mobtotem;

import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;

public class MobTotem {
    private final Location location;
    private final String owner;
    private String timerString;

    public MobTotem(Location location, String owner) {
        this.location = location;
        this.owner = owner;
    }

    public Location getLocation() {
        return location;
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
        return Math.sqrt(McUtils.player().distanceToSqr(new Vec3(location.x(), location.y() - 4, location.z())));
    }

    public double getLookAngleDiff() {
        Vec3 playerLook = McUtils.player().getLookAngle();
        double lookAngle = Math.toDegrees(StrictMath.atan2(playerLook.z, playerLook.x));

        Position mobTotemLocation = this.getLocation();
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
