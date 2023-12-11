/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.utils.type.UnsignedByte;
import java.nio.charset.StandardCharsets;

// FIXME: Unit testing
public final class UnsignedByteUtils {
    public static UnsignedByte[] fromBitArray(boolean[] values) {
        assert values.length % 8 == 0;

        UnsignedByte[] bytes = new UnsignedByte[values.length / 8];
        for (int i = 0; i < values.length; i += 8) {
            byte value = 0;
            for (int j = 0; j < 8; j++) {
                value |= (values[i + j] ? 1 : 0) << (7 - j);
            }
            bytes[i / 8] = UnsignedByte.of(value);
        }
        return bytes;
    }

    public static UnsignedByte[] encodeString(String string) {
        // Strings are encoded by encoding the char's ASCII value
        // and is terminated by a 0 byte
        UnsignedByte[] bytes = new UnsignedByte[string.length() + 1];
        byte[] asciiBytes = string.getBytes(StandardCharsets.US_ASCII);

        for (int i = 0; i < asciiBytes.length; i++) {
            bytes[i] = UnsignedByte.of(asciiBytes[i]);
        }

        // NULL terminate the string
        bytes[bytes.length - 1] = UnsignedByte.of((byte) 0);

        return bytes;
    }

    public static UnsignedByte[] encodeVariableSizedInteger(long value) {
        // Encode an signed integer value with a variable number of bytes.

        // Do the encoding
        UnsignedByte[] bytes;
        if (value >= -128 && value <= 127) {
            bytes = new UnsignedByte[1];
            bytes[0] = UnsignedByte.of((byte) value);
        } else if (value >= -32768 && value <= 32767) {
            bytes = new UnsignedByte[2];
            bytes[0] = UnsignedByte.of((byte) (value >> 8));
            bytes[1] = UnsignedByte.of((byte) value);
        } else if (value >= -8388608 && value <= 8388607) {
            bytes = new UnsignedByte[3];
            bytes[0] = UnsignedByte.of((byte) (value >> 16));
            bytes[1] = UnsignedByte.of((byte) (value >> 8));
            bytes[2] = UnsignedByte.of((byte) value);
        } else if (value >= -2147483648 && value <= 2147483647) {
            bytes = new UnsignedByte[4];
            bytes[0] = UnsignedByte.of((byte) (value >> 24));
            bytes[1] = UnsignedByte.of((byte) (value >> 16));
            bytes[2] = UnsignedByte.of((byte) (value >> 8));
            bytes[3] = UnsignedByte.of((byte) value);
        } else {
            bytes = new UnsignedByte[8];
            bytes[0] = UnsignedByte.of((byte) (value >> 56));
            bytes[1] = UnsignedByte.of((byte) (value >> 48));
            bytes[2] = UnsignedByte.of((byte) (value >> 40));
            bytes[3] = UnsignedByte.of((byte) (value >> 32));
            bytes[4] = UnsignedByte.of((byte) (value >> 24));
            bytes[5] = UnsignedByte.of((byte) (value >> 16));
            bytes[6] = UnsignedByte.of((byte) (value >> 8));
            bytes[7] = UnsignedByte.of((byte) value);
        }

        return bytes;
    }
}
