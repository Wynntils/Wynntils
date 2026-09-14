/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.utils.type.BoundingBox;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestDiscoveryRecord {
    @Test
    public void nothingIsDiscoveredInitially() {
        DiscoveryRecord record = new DiscoveryRecord();

        Assertions.assertFalse(record.isDiscovered(0, 0));
    }

    @Test
    public void revealMarksTheSquareOfChunksAroundThePosition() {
        DiscoveryRecord record = new DiscoveryRecord();

        record.reveal(100, -200, 1);

        // Sample chunk is (6, -13); radius 1 covers chunks 5..7 x -14..-12
        Assertions.assertTrue(record.isDiscovered(80, -224)); // chunk (5, -14)
        Assertions.assertTrue(record.isDiscovered(127, -177)); // chunk (7, -12)
        Assertions.assertFalse(record.isDiscovered(79, -200)); // chunk (4, -13)
        Assertions.assertFalse(record.isDiscovered(128, -200)); // chunk (8, -13)
        Assertions.assertFalse(record.isDiscovered(100, -225)); // chunk (6, -15)
        Assertions.assertFalse(record.isDiscovered(100, -176)); // chunk (6, -11)
    }

    @Test
    public void emptyRecordHasNoRuns() {
        DiscoveryRecord record = new DiscoveryRecord();

        Assertions.assertEquals(List.of(), record.discoveredRuns(new BoundingBox(-100, -100, 100, 100)));
    }

    @Test
    public void singleChunkIsOneRunOfItsOwnBounds() {
        DiscoveryRecord record = new DiscoveryRecord();
        record.reveal(40, 40, 0); // chunk (2, 2) = blocks 32..48

        Assertions.assertEquals(
                List.of(new BoundingBox(32, 32, 48, 48)), record.discoveredRuns(new BoundingBox(0, 0, 100, 100)));
    }

    @Test
    public void adjacentChunksInARowMergeIntoOneRunButRowsDoNot() {
        DiscoveryRecord record = new DiscoveryRecord();
        record.reveal(24, 24, 1); // chunks 0..2 x 0..2

        Assertions.assertEquals(
                List.of(new BoundingBox(0, 0, 48, 16), new BoundingBox(0, 16, 48, 32), new BoundingBox(0, 32, 48, 48)),
                record.discoveredRuns(new BoundingBox(-100, -100, 100, 100)));
    }

    @Test
    public void gapsSplitRuns() {
        DiscoveryRecord record = new DiscoveryRecord();
        record.reveal(8, 8, 0); // chunk (0, 0)
        record.reveal(40, 8, 0); // chunk (2, 0)

        Assertions.assertEquals(
                List.of(new BoundingBox(0, 0, 16, 16), new BoundingBox(32, 0, 48, 16)),
                record.discoveredRuns(new BoundingBox(-100, -100, 100, 100)));
    }

    @Test
    public void runsAreClippedToTheQueryBoxAndOutsideChunksIgnored() {
        DiscoveryRecord record = new DiscoveryRecord();
        record.reveal(24, 24, 1); // chunks 0..2 x 0..2, blocks 0..48
        record.reveal(200, 200, 0); // outside the box

        Assertions.assertEquals(
                List.of(
                        new BoundingBox(10, 10, 40, 16),
                        new BoundingBox(10, 16, 40, 32),
                        new BoundingBox(10, 32, 40, 40)),
                record.discoveredRuns(new BoundingBox(10, 10, 40, 40)));
    }

    @Test
    public void everyBlockInsideARevealedChunkIsDiscovered() {
        DiscoveryRecord record = new DiscoveryRecord();
        record.reveal(-1, -1, 0); // chunk (-1, -1) = blocks -16..-1

        for (int x = -16; x <= -1; x++) {
            for (int z = -16; z <= -1; z++) {
                Assertions.assertTrue(record.isDiscovered(x + 0.5, z + 0.5), x + "," + z);
            }
        }
        Assertions.assertFalse(record.isDiscovered(0, -1));
        Assertions.assertFalse(record.isDiscovered(-17, -1));
    }

    @Test
    public void radiusEightRevealsSeventeenChunksAcross() {
        DiscoveryRecord record = new DiscoveryRecord();
        record.reveal(0, 0, 8); // chunk (0, 0); covers -8..8

        Assertions.assertTrue(record.isDiscovered(-128, -128)); // chunk (-8, -8)
        Assertions.assertTrue(record.isDiscovered(143, 143)); // chunk (8, 8)
        Assertions.assertFalse(record.isDiscovered(-129, 0)); // chunk (-9, 0)
        Assertions.assertFalse(record.isDiscovered(144, 0)); // chunk (9, 0)
        Assertions.assertEquals(
                List.of(new BoundingBox(-128, -128, 144, -112)),
                record.discoveredRuns(new BoundingBox(-200, -128, 200, -112)));
    }

    @Test
    public void overlappingRevealsMergeIntoOneRunPerRow() {
        DiscoveryRecord record = new DiscoveryRecord();
        record.reveal(8, 8, 1); // chunks -1..1
        record.reveal(24, 8, 1); // chunks 0..2

        Assertions.assertEquals(
                List.of(new BoundingBox(-16, 0, 48, 16)), record.discoveredRuns(new BoundingBox(-100, 0, 100, 16)));
    }
}
