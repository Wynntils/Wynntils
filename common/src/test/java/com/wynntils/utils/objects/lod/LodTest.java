/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects.lod;

import com.wynntils.utils.objects.IBoundingBox.AxisAlignedBoundingBox;
import com.wynntils.utils.objects.bvh.BoundingVolumeHierarchy;
import com.wynntils.utils.objects.lod.presets.MipMapCreator;
import com.wynntils.utils.objects.lod.presets.MipMapImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import testUtils.BvhTestUtils;

public class LodTest {
    private static final File baseDir = new File("src/test/resources/lod/").getAbsoluteFile();
    private static final File lodImagesDir = new File(baseDir, "images/");
    private static final File lodImagesSource = new File(lodImagesDir, "original.png");
    private static final File lodCacheDir = new File(baseDir, "cache/");
    private static final File testOutputDir = new File(baseDir, "out/");
    private static final String lodImageFilePattern = "^-?[0-9]+,-?[0-9]+\\.png$";

    private static BoundingVolumeHierarchy<MipMapImage> bvh;

    @BeforeAll
    public static void setupBeforeAll() throws IOException {
        // split source image for LODing
        final BufferedImage sourceImage = ImageIO.read(lodImagesSource);

        // load image slices
        final File[] lodImageFiles =
                lodImagesDir.listFiles((dir, name) -> dir.equals(lodImagesDir) && name.matches(lodImageFilePattern));
        bvh = new BoundingVolumeHierarchy<>(null, new LodManager<>(lodCacheDir, new MipMapCreator()));
        for (final File imageFile : lodImageFiles) {
            final String name = imageFile.getName();
            final String[] coordStrings = name.substring(0, name.length() - 4).split(",");
            final int x = Integer.parseInt(coordStrings[0]);
            final int y = Integer.parseInt(coordStrings[1]);
            final BufferedImage image = ImageIO.read(imageFile);
            bvh.add(new MipMapImage(image, new AxisAlignedBoundingBox(new Vec3(x, y, 0), new Vec3(x + 1, y + 1, 0))));
        }
        System.out.println(bvh.size());
        BvhTestUtils.waitForBvhRebuild(bvh);
        System.out.println(bvh.size());
    }

    @Test
    @Disabled
    public void testLodLevels() throws IOException {
        Assertions.assertEquals(25, bvh.size());
        List<MipMapImage> lodLevelInfinite =
                bvh.treeCut(AxisAlignedBoundingBox.INFINITE, new Vec3(0, 0, Double.POSITIVE_INFINITY), false);
        // Assertions.assertEquals(1, lodLevelInfinite.size());
        Assertions.assertTrue(testOutputDir.exists() || testOutputDir.mkdirs());
        ImageIO.write(lodLevelInfinite.get(0).image, "PNG", new File(testOutputDir, "lodLevelInfinite"));
    }

    @AfterAll
    public static void cleanupAfterAll() {
        //        try (final Stream<Path> filesToBeDeleted = Files.walk(lodCacheDir.toPath())) {
        //            Assertions.assertTrue(filesToBeDeleted
        //                    .sorted(Comparator.reverseOrder())
        //                    .map(Path::toFile)
        //                    .map(File::delete)
        //                    .reduce(true, Boolean::logicalAnd));
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //            Assertions.fail();
        //        }
    }
}
