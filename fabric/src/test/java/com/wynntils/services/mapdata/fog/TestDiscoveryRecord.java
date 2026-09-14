/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.fog;

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
        Assertions.assertTrue(record.isDiscoveredChunk(8, -8));
        Assertions.assertFalse(record.isDiscoveredChunk(9, -8));
    }
}
