package com.wynntils.utils;

public class BoundingBox {
    public final int x1;
    public final int z1;
    public final int x2;
    public final int z2;

    public static BoundingBox centered(int centerX, int centerZ, int widthX, int widthZ) {
        return new BoundingBox(centerX - widthX/2, centerZ - widthZ/2, centerX + widthX/2, centerZ + widthZ/2);
    }

    public BoundingBox(int x1, int z1, int x2, int z2) {
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
    }

    public boolean isInside(int x, int z) {
        return x1 <= x && x <= x2 && z1 <= z && z <= z2;
    }

    public boolean intersects(
            BoundingBox other) {
        boolean xIntersects = x1 < other.x2 && other.x1 < x2;
        boolean zIntersects = z1 < other.z2 && other.z1 < z2;
        return xIntersects && zIntersects;
    }
}
