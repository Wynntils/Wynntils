/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc.type;

import com.wynntils.models.map.PoiLocation;
import com.wynntils.utils.wynn.WynnUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class Location extends Vector3d implements Position {
    public Location(double x, double y, double z) {
        super(x, y, z);
    }

    public Location(Entity entity) {
        this(entity.getX(), entity.getY(), entity.getZ());
    }

    public Location(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public Location(PoiLocation location) {
        this(location.getX(), location.getY().orElse(0), location.getZ());
    }

    public Location(Position location) {
        this(location.x(), location.y(), location.z());
    }

    public Location add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;

        return this;
    }

    // An egregious circumvention of the type system; Should have two signatures:
    // public void subtract(Vector3d) and public Vector3d subtract(Point3d), but that would
    // be confusing for method chaining, so just subtract *anything*, and this location might become
    // a vector if we subtracted another location.
    public Location subtract(Vector3d loc) {
        x -= loc.x;
        y -= loc.y;
        z -= loc.z;

        return this;
    }

    public Location subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;

        return this;
    }

    public Location subtract(double amount) {
        this.x -= amount;
        this.y -= amount;
        this.z -= amount;

        return this;
    }

    public Location multiply(Vector3d loc) {
        x *= loc.x;
        y *= loc.y;
        z *= loc.z;

        return this;
    }

    public Location multiply(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;

        return this;
    }

    public Location multiply(double amount) {
        this.x *= amount;
        this.y *= amount;
        this.z *= amount;

        return this;
    }

    public BlockPos toBlockPos() {
        return WynnUtils.newBlockPos(x, y, z);
    }

    public Vec3 toVec3() {
        return new Vec3(x, y, z);
    }

    @Override
    public Location clone() throws CloneNotSupportedException {
        super.clone();
        return new Location(x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof Vector3d) {
            return super.equals(obj);
        }
        return false;
    }

    public boolean equals(Vector3d other) {
        if (other == null) return false;
        return x == other.x && y == other.y && z == other.z;
    }

    public String toString() {
        return "[" + (int) Math.round(this.x) + ", " + (int) Math.round(this.y) + ", " + (int) Math.round(this.z) + "]";
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public double z() {
        return z;
    }
}
