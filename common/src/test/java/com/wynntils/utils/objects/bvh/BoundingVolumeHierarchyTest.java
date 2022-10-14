/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects.bvh;

import com.wynntils.utils.objects.IBoundingBox;
import com.wynntils.utils.objects.IBoundingBox.AxisAlignedBoundingBox;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import testUtils.BvhTestUtils;

/**
 * Tests all functionality of the BVH.
 *
 * @author Kepler-17c
 */
class BoundingVolumeHierarchyTest {
    /**
     * Adds and removes a large number of elements in quick succession.
     */
    @Test
    void addRemoveBulkTest() {
        final BoundingVolumeHierarchy<IBoundingBox> bvh = new BoundingVolumeHierarchy<>();
        final Set<IBoundingBox> referenceSet = new HashSet<>();
        final Random random = new Random("Wynntils".hashCode()); // fixed seed for reproducible test
        final int cycles = 1 << 8;
        final int insertCount = 1 << 8;
        final int removalCount = 1 << 6;
        for (int c = 0; c < cycles; c++) {
            // fill BVH and collect elements to be removed later
            final List<IBoundingBox> insertElements = IntStream.range(0, insertCount)
                    .mapToObj(i -> generatePointBB(random))
                    .toList();
            final List<IBoundingBox> removals = IntStream.range(0, removalCount)
                    .map(i -> random.nextInt(insertCount))
                    .mapToObj(insertElements::get)
                    .toList();
            bvh.addAll(insertElements);
            referenceSet.addAll(insertElements);
            // create probe point and compare the results for nearest
            final Vec3 probePoint = generatePoint(random);
            final IBoundingBox referenceNearest = referenceSet.stream()
                    .min(Comparator.comparingDouble(a -> a.squareDistance(probePoint)))
                    .get();
            // test bvh against reference element
            IBoundingBox bvhNearest = bvh.findNearest(probePoint);
            Assertions.assertTrue(bvh.contains(referenceNearest));
            Assertions.assertEquals(
                    referenceNearest,
                    bvhNearest,
                    "Cycle: " + c + ", expected dist: " + referenceNearest.squareDistance(probePoint)
                            + ", actual dist: " + bvhNearest.squareDistance(probePoint));
            // remove marked elements and test again
            bvh.removeAll(removals);
            referenceSet.removeAll(removals);
            bvhNearest = bvh.findNearest(probePoint);
            if (removals.contains(referenceNearest)) {
                Assertions.assertNotEquals(
                        referenceNearest, bvhNearest, "Cycle: " + c + " - expected unequal, but matched");
            } else {
                Assertions.assertEquals(
                        referenceNearest,
                        bvhNearest,
                        "Cycle: " + c + ", expected dist: " + referenceNearest.squareDistance(probePoint)
                                + ", actual dist: "
                                + bvhNearest.squareDistance(probePoint));
            }
        }
    }

    /**
     * Adds a bunch of points and checks that findNearest returns the correct ones.
     */
    @Test
    void addTest() {
        final BoundingVolumeHierarchy<IBoundingBox> bvh = new BoundingVolumeHierarchy<>();
        final int range = 1 << 7;
        final IBoundingBox[][] expected = new IBoundingBox[range][range];
        for (int y = 0; y < range; y++) {
            for (int x = 0; x < range; x++) {
                final IBoundingBox bb = new AxisAlignedBoundingBox(new Vec3(x, y, 0));
                expected[y][x] = bb;
            }
            bvh.addAll(Arrays.asList(expected[y]));
        }
        for (int y = 0; y < range; y++) {
            for (int x = 0; x < range; x++) {
                Assertions.assertEquals(expected[y][x], bvh.findNearest(new Vec3(x, y, 0)));
            }
        }
    }

    /**
     * Adds elements while checking the size.
     */
    @Test
    void sizeTest() {
        final BoundingVolumeHierarchy<IBoundingBox> bvh = new BoundingVolumeHierarchy<>();
        final int totalLimit = 1 << 12;
        final int batchLimit = 1 << 8;
        final Random random = new Random("Ragni".hashCode());
        for (int size = 0; size < totalLimit; ) {
            final int batch = random.nextInt(batchLimit);
            for (int b = 0; b < batch; b++, size++) {
                bvh.add(generatePointBB(random));
            }
            Assertions.assertEquals(size, bvh.size());
        }
    }

    /**
     * Testing lookup efficiency of the BVH vs linear search.
     */
    @Test
    @Disabled
    void lookupTimesTest() throws IOException {
        final Random random = new Random("Artemis".hashCode());
        final int probeCount = 1 << 5;
        final int maxElementCount = 1 << 20;
        final Function<Integer, Integer> probeSizeSupplier = (index) -> maxElementCount * index / probeCount;
        final int measurementCount = 1 << 8;
        final List<Quadruple<Integer, Long, Long, Long>> timingResults = new ArrayList<>();
        for (int i = 1; i <= probeCount; i++) {
            final List<IBoundingBox> list = IntStream.range(0, probeSizeSupplier.apply(i))
                    .mapToObj(x -> generatePointBB(random))
                    .toList();
            final long buildTimeStart = System.currentTimeMillis();
            final BoundingVolumeHierarchy<IBoundingBox> bvh = new BoundingVolumeHierarchy<>();
            bvh.addAll(list);
            Assertions.assertTrue(BvhTestUtils.waitForBvhRebuild(bvh));
            final long buildTimeEnd = System.currentTimeMillis();
            long buildTime = buildTimeEnd - buildTimeStart;
            long listTimeSum = 0;
            long bvhTimeSum = 0;
            for (int n = 0; n < measurementCount; n++) {
                final Vec3 probePoint = generatePoint(random);
                final long listStartTime = System.nanoTime();
                final IBoundingBox listMin = list.stream()
                        .min(Comparator.comparingDouble(a -> a.squareDistance(probePoint)))
                        .orElse(null);
                final long listEndTime = System.nanoTime();
                final long bvhStartTime = System.nanoTime();
                final IBoundingBox bvhMin = bvh.findNearest(probePoint);
                final long bvhEndTime = System.nanoTime();
                Assertions.assertEquals(listMin, bvhMin);
                listTimeSum += listEndTime - listStartTime;
                bvhTimeSum += bvhEndTime - bvhStartTime;
            }
            timingResults.add(new Quadruple<>(list.size(), listTimeSum, bvhTimeSum, buildTime));
        }
        // export data
        final File file = new File("src/test/resources/bvh_vs_linear_times.csv");
        final FileWriter fw = new FileWriter(file);
        final BufferedWriter bw = new BufferedWriter(fw);
        bw.write("\"Element Count\",\"List Times\",\"BVH Times\",\"BVH Build Time\"");
        bw.newLine();
        for (var t : timingResults) {
            bw.append(t.first + "," + t.second + "," + t.third + "," + t.fourth);
            bw.newLine();
        }
        bw.close();
        fw.close();
    }

    /**
     * Creates a new random point in the interval {@code [0,1)} using the given generator.
     * @param random RNG source.
     * @return the random point.
     */
    private static Vec3 generatePoint(final Random random) {
        return new Vec3(random.nextDouble(), random.nextDouble(), random.nextDouble());
    }

    /**
     * Encapsulates the point from {@link #generatePoint(Random)} in a bounding box.
     *
     * @param random RNG source.
     * @return the random point bounding box.
     */
    private static IBoundingBox generatePointBB(final Random random) {
        return new AxisAlignedBoundingBox(generatePoint(random));
    }

    public record Quadruple<A, B, C, D>(A first, B second, C third, D fourth) {}
}
