/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

public interface BoundingShape {
    boolean contains(float x, float z);

    static boolean intersects(BoundingBox boundingBox1, BoundingBox boundingBox2) {
        // Check if the bounding boxes intersect in the x and z dimensions
        boolean xIntersects =
                Math.max(boundingBox1.x1(), boundingBox2.x1()) < Math.min(boundingBox1.x2(), boundingBox2.x2());
        boolean zIntersects =
                Math.max(boundingBox1.z1(), boundingBox2.z1()) < Math.min(boundingBox1.z2(), boundingBox2.z2());

        // If the bounding boxes intersect in both dimensions, they intersect
        return xIntersects && zIntersects;
    }

    static boolean intersects(BoundingBox boundingBox, BoundingCircle boundingCircle) {
        // Nearest point on the bounding box to the center of the circle
        float nearestX = Math.max(boundingBox.x1(), Math.min(boundingBox.x2(), boundingCircle.x()));
        float nearestZ = Math.max(boundingBox.z1(), Math.min(boundingBox.z2(), boundingCircle.z()));

        // Find the distance between the nearest point and the center of the circle
        float deltaX = boundingCircle.x() - nearestX;
        float deltaZ = boundingCircle.z() - nearestZ;

        // If the distance is less than the radius, the circle intersects the bounding box
        return (deltaX * deltaX + deltaZ * deltaZ) < (boundingCircle.radius() * boundingCircle.radius());
    }

    static boolean intersects(BoundingCircle boundingCircle1, BoundingCircle boundingCircle2) {
        // Find the distance between the centers of the circles
        float deltaX = boundingCircle2.x() - boundingCircle1.x();
        float deltaZ = boundingCircle2.z() - boundingCircle1.z();

        // If the distance is less than the sum of the radii, the circles intersect
        return (deltaX * deltaX + deltaZ * deltaZ)
                < ((boundingCircle1.radius() + boundingCircle2.radius())
                        * (boundingCircle1.radius() + boundingCircle2.radius()));
    }
}
