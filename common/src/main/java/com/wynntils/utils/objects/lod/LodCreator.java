/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects.lod;

import com.wynntils.utils.objects.IBoundingBox;
import com.wynntils.utils.objects.bvh.ISplitStrategy;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.UUID;
import net.minecraft.world.phys.Vec3;

public interface LodCreator<T extends IBoundingBox> {
    T read(final InputStream inputStream);

    boolean write(final OutputStream outputStream, final T data);

    ISplitStrategy splitStrategy();

    boolean testLodLevel(final Vec3 observer, final IBoundingBox objectBounds, final int lodLevel);

    LodElement<T> buildLod(final Collection<T> elements, final int lodLevel);

    byte[] lodObjectToByteArray(final T element);

    default UUID logObjectToUuid(final T element) {
        final byte[] hashBytes;
        try {
            hashBytes = MessageDigest.getInstance("SHA-256").digest(this.lodObjectToByteArray(element));
        } catch (NoSuchAlgorithmException e) {
            // this will never happen, unless SHA-256 is removed from MessageDigest
            throw new RuntimeException(e);
        }
        long msb = 0;
        long lsb = 0;
        final long byteMask = 0xFFL;
        for (int i = 0; i < hashBytes.length; i++) {
            if ((i / Long.BYTES) % 2 == 0) {
                lsb ^= (byteMask & hashBytes[i]) << (Byte.SIZE * (i % Long.BYTES));
            } else {
                msb ^= (byteMask & hashBytes[i]) << (8 * (i % Long.BYTES));
            }
        }
        return new UUID(msb, lsb);
    }
}
