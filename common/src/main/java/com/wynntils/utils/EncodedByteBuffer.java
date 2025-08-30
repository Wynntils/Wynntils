/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.UnsignedByte;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A buffer of bytes that can be encoded and decoded to various formats.
 */
public final class EncodedByteBuffer {
    private static final int PRIVATE_USE_AREA_A_START = 0xF0000;
    private static final int PRIVATE_USE_AREA_B_START = 0x100000;

    private final UnsignedByte[] bytes;

    private EncodedByteBuffer(UnsignedByte[] bytes) {
        this.bytes = bytes;
    }

    public static EncodedByteBuffer fromBytes(UnsignedByte[] bytes) {
        return new EncodedByteBuffer(bytes);
    }

    public static EncodedByteBuffer fromUtf16String(String string) {
        List<UnsignedByte> bytes = new ArrayList<>();

        int[] codePoints = string.codePoints().toArray();

        for (int codePoint : codePoints) {
            // Special cases
            if (codePoint >= PRIVATE_USE_AREA_B_START) {
                // Single byte
                int singleByteOffset = PRIVATE_USE_AREA_B_START + 0xEE;
                if ((codePoint & 0xFF) == 0xEE) {
                    int actualValue = (codePoint - singleByteOffset) >> 8;
                    bytes.add(UnsignedByte.of((byte) actualValue));

                    assert actualValue <= 255 : "Invalid code point: " + codePoint;
                    continue;
                }

                // Two bytes
                int values = codePoint - PRIVATE_USE_AREA_B_START;

                UnsignedByte firstByte = UnsignedByte.of((byte) 255);
                UnsignedByte secondByte = UnsignedByte.of((byte) (254 + (values & 0xFF)));

                bytes.add(firstByte);
                bytes.add(secondByte);

                // Only 0x100000-0x100001 are used
                assert codePoint < 0x100002 : "Invalid code point: " + codePoint;
                continue;
            }

            // Normal case
            int values = codePoint - PRIVATE_USE_AREA_A_START;

            UnsignedByte firstByte = UnsignedByte.of((byte) (values >> 8));
            UnsignedByte secondByte = UnsignedByte.of((byte) (values & 0xFF));

            bytes.add(firstByte);
            bytes.add(secondByte);

            // Only 0xF0000-0xFFFFD are used
            assert codePoint < 0xFFFFE : "Invalid code point: " + codePoint;
        }

        return fromBytes(bytes.toArray(UnsignedByte[]::new));
    }

    public static EncodedByteBuffer fromBase64String(String string) {
        byte[] decodedBytes = Base64.getDecoder().decode(string);
        UnsignedByte[] bytes = UnsignedByte.of(decodedBytes);
        return fromBytes(bytes);
    }

    public String toUtf16String() {
        StringBuilder builder = new StringBuilder(bytes.length / 2 + bytes.length % 2);

        // 2 byte -> UTF-16
        for (int i = 0; i < bytes.length - 1; i += 2) {
            int codePoint;

            // 0xFFFE-0xFFFF are using private use area B
            if (bytes[i].value() == 255 && bytes[i + 1].value() >= 254) {
                codePoint = PRIVATE_USE_AREA_B_START + (bytes[i + 1].value() - 254);
            } else {
                codePoint = PRIVATE_USE_AREA_A_START + (bytes[i].value() << 8 | bytes[i + 1].value());
            }

            builder.appendCodePoint(codePoint);
        }

        // Handle leftover byte
        if (bytes.length % 2 == 1) {
            // Odd number of bytes, so we add a padding character
            // Pad with 0xEE to stay in the private use area
            builder.appendCodePoint(PRIVATE_USE_AREA_B_START + (bytes[bytes.length - 1].value() << 8) + 238);
        }

        return builder.toString();
    }

    public String toBase64String() {
        byte[] bytes = UnsignedByte.toPrimitive(this.bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public UnsignedByte[] getBytes() {
        return bytes;
    }

    public ArrayReader<UnsignedByte> getReader() {
        return new ArrayReader<>(bytes);
    }

    @Override
    public String toString() {
        return "EncodedByteBuffer{" + "bytes="
                + Arrays.stream(bytes)
                        .map(UnsignedByte::value)
                        .map(Objects::toString)
                        .collect(Collectors.joining(", ")) + '}';
    }
}
