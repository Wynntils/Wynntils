/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.utils.type.UnsignedByte;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    public static boolean[] toBitArray(UnsignedByte[] unsignedBytes) {
        boolean[] values = new boolean[unsignedBytes.length * 8];
        for (int i = 0; i < unsignedBytes.length; i++) {
            byte value = unsignedBytes[i].toByte();
            for (int j = 0; j < 8; j++) {
                values[i * 8 + j] = ((value >> (7 - j)) & 1) == 1;
            }
        }
        return values;
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

    public static String decodeString(List<UnsignedByte> byteReader) {
        // Strings are encoded by encoding the char's ASCII value
        byte[] asciiBytes = new byte[byteReader.size()];

        for (int i = 0; i < byteReader.size(); i++) {
            asciiBytes[i] = byteReader.get(i).toByte();
        }

        return new String(asciiBytes, StandardCharsets.US_ASCII);
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

    public static long decodeVariableSizedInteger(UnsignedByte[] bytes) {
        // Decode a variable sized integer value from a byte array.
        // The first byte is the most significant byte.

        // Do the decoding
        long value;
        if (bytes.length == 1) {
            value = bytes[0].toByte();
        } else if (bytes.length == 2) {
            value = ((long) bytes[0].toByte() << 8) | bytes[1].value();
        } else if (bytes.length == 3) {
            value = ((long) bytes[0].toByte() << 16) | (bytes[1].value() << 8) | bytes[2].value();
        } else if (bytes.length == 4) {
            value = ((long) bytes[0].toByte() << 24)
                    | (bytes[1].value() << 16)
                    | (bytes[2].value() << 8)
                    | bytes[3].value();
        } else {
            value = ((long) bytes[0].toByte() << 56)
                    | ((long) bytes[1].value() << 48)
                    | ((long) bytes[2].value() << 40)
                    | ((long) bytes[3].value() << 32)
                    | (bytes[4].value() << 24)
                    | (bytes[5].value() << 16)
                    | (bytes[6].value() << 8)
                    | bytes[7].value();
        }

        return value;
    }
}
