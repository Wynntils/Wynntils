/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects.lod.presets;

import com.wynntils.utils.MathUtils;
import com.wynntils.utils.objects.IBoundingBox;
import com.wynntils.utils.objects.IBoundingBox.AxisAlignedBoundingBox;
import com.wynntils.utils.objects.Pair;
import com.wynntils.utils.objects.bvh.ISplitStrategy;
import com.wynntils.utils.objects.bvh.MipMapStrategy;
import com.wynntils.utils.objects.lod.LodCreator;
import com.wynntils.utils.objects.lod.LodElement;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.ArrayUtils;

public class MipMapCreator implements LodCreator<MipMapImage> {
    private static final ISplitStrategy SPLIT_STRATEGY = new MipMapStrategy();

    @Override
    public MipMapImage read(InputStream inputStream) {
        final AxisAlignedBoundingBox bounds;
        final BufferedImage image;
        try {
            bounds = new AxisAlignedBoundingBox(
                    byteArrayToVec3(inputStream.readNBytes(Double.BYTES * 3)),
                    byteArrayToVec3(inputStream.readNBytes(Double.BYTES * 3)));
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new MipMapImage(image, bounds);
    }

    @Override
    public boolean write(OutputStream outputStream, MipMapImage data) {
        final Pair<Vec3, Vec3> definingPoints = data.definingPoints();
        try {
            outputStream.write(vec3ToByteArray(definingPoints.a));
            outputStream.write(vec3ToByteArray(definingPoints.b));
            outputStream.write(lodObjectToByteArray(data));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public ISplitStrategy splitStrategy() {
        return SPLIT_STRATEGY;
    }

    @Override
    public boolean testLodLevel(final Vec3 observer, final IBoundingBox objectBounds, final int lodLevel) {
        return MathUtils.log2(observer.z) <= lodLevel;
    }

    @Override
    public LodElement<MipMapImage> buildLod(Collection<MipMapImage> elements, int lodLevel) {
        final int lodMultiplier = (int) Math.sqrt(SPLIT_STRATEGY.bucketCount());
        final int tileSize = elements.iterator().next().image.getWidth();
        final int canvasSize = tileSize * lodMultiplier;
        final BufferedImage lodCanvas = new BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_INT_ARGB);
        final AxisAlignedBoundingBox totalBounds = elements.stream()
                .map(lodElement -> (AxisAlignedBoundingBox) lodElement)
                .reduce(new AxisAlignedBoundingBox(), AxisAlignedBoundingBox::mergeBounds);
        final Graphics lodGraphics = lodCanvas.getGraphics();
        elements.forEach(lodElement -> lodGraphics.drawImage(
                lodElement.image,
                (int) ((lodElement.getMinX() - totalBounds.getMinX()) / lodElement.size().x),
                (int) ((lodElement.getMinY() - totalBounds.getMinY()) / lodElement.size().y),
                null));
        lodGraphics.dispose();
        final BufferedImage lodImage = new BufferedImage(
                lodCanvas.getWidth() / lodMultiplier, lodCanvas.getHeight() / lodMultiplier, lodCanvas.getType());
        lodImage.getGraphics()
                .drawImage(
                        lodCanvas.getScaledInstance(lodImage.getWidth(), lodImage.getHeight(), Image.SCALE_SMOOTH),
                        0,
                        0,
                        null);
        lodImage.getGraphics().dispose();
        final MipMapImage lodObject = new MipMapImage(lodImage, totalBounds);
        return new LodElement<>(lodObject, logObjectToUuid(lodObject), lodLevel + 1);
    }

    @Override
    public byte[] lodObjectToByteArray(final MipMapImage element) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(element.image, "PNG", outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    private static byte[] vec3ToByteArray(final Vec3 point) {
        final Byte[] data = Stream.of(point.x, point.y, point.z)
                .map(Double::doubleToRawLongBits)
                .map(bits -> Arrays.asList(
                        (byte) ((bits >>> 0) & 0xFF),
                        (byte) ((bits >>> 8) & 0xFF),
                        (byte) ((bits >>> 16) & 0xFF),
                        (byte) ((bits >>> 24) & 0xFF),
                        (byte) ((bits >>> 32) & 0xFF),
                        (byte) ((bits >>> 40) & 0xFF),
                        (byte) ((bits >>> 48) & 0xFF),
                        (byte) ((bits >>> 56) & 0xFF)))
                .flatMap(List::stream)
                .toArray(Byte[]::new);
        return ArrayUtils.toPrimitive(data);
    }

    private static Vec3 byteArrayToVec3(final byte[] data) {
        final long[] coords = new long[3];
        for (int i = 0; i < data.length; i++) {
            coords[i / Double.BYTES] |= (0xFFL & data[i]) << (i % Double.BYTES);
        }
        return new Vec3(
                Double.longBitsToDouble(coords[0]),
                Double.longBitsToDouble(coords[1]),
                Double.longBitsToDouble(coords[2]));
    }
}
