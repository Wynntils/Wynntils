/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects.bvh;

import com.wynntils.utils.objects.IBoundingBox;
import com.wynntils.utils.objects.IBoundingBox.AxisAlignedBoundingBox;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.phys.Vec3;

/**
 * Implements a tiling strategy. Base tiles are the coordinate quadrants. Each is split in four along all powers of two. This property is continued to all subdivisions.
 * <p>This strategy assumes, that all elements are aligned in a grid anchored at zero.</p>
 */
public class MipMapStrategy implements ISplitStrategy {
    @Override
    public <T extends IBoundingBox> List<Set<T>> split(Set<T> set) {
        final IBoundingBox bounds = new AxisAlignedBoundingBox(set);
        if (bounds.contains(Vec3.ZERO)) {
            // spans across zero - split into quadrants
            final List<Set<T>> buckets = new ArrayList<>(4);
            for (int i = 0; i < 4; i++) {
                buckets.add(new HashSet<>());
            }
            set.forEach(e -> {
                final int bucketIndex = (e.mid().x > 0 ? 1 << 0 : 0) | (e.mid().y > 0 ? 1 << 1 : 0);
                buckets.get(bucketIndex).add(e);
            });
            return buckets;
        } else {
            // all in one quadrant - split into tiles
            // TODO
            return null;
        }
    }

    @Override
    public int bucketCount() {
        return 4;
    }

    private static double roundUpToNextPowerOfTwo(double x) {
        final long inputBits = Double.doubleToRawLongBits(x);
        final long MANTISSA_MASK = 0x000fffffffffffffL;
        if ((inputBits & MANTISSA_MASK) == 0 || Double.isNaN(x) || Double.isInfinite(x)) {
            return x;
        }
        final long EXPONENT_MASK = 0x7ff0000000000000L;
        final long exponent = (inputBits & EXPONENT_MASK) + 1;
        final long SIGN_MASK = 0x8000000000000000L;
        return Double.longBitsToDouble((inputBits & SIGN_MASK) | exponent);
    }
}
