/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.UnsignedByte;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
        // Check if the string only contains ASCII characters
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) > 127) {
                throw new IllegalArgumentException("String contains non-ASCII characters");
            }
        }

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

    public static String decodeString(List<UnsignedByte> bytes) {
        // Strings are encoded by encoding the char's ASCII value
        byte[] asciiBytes = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++) {
            asciiBytes[i] = bytes.get(i).toByte();
        }

        return new String(asciiBytes, StandardCharsets.US_ASCII);
    }

    public static UnsignedByte[] encodeVariableSizedInteger(long value) {
        // Use zig-zag encoding to encode negative numbers
        // (this gets rid of the sign bit, so we only work with positive numbers)
        value = (value << 1) ^ (value >> 63);

        // Store 7 bits of the source data in the target byte. Use the highest bit to mark if we're done.
        // If it is 0, then we're done, and the byte is exactly the value we wanted.
        // If it is 1, use the 7 bits we have, and grab the next 7 bits from the next byte.
        // If that byte's highest bit is 0, then we're done (the value could be stored in 14 bits), otherwise continue.

        // Calculate the number of bytes we need
        int numBytes = 1;
        long temp = value;
        while ((temp >>>= 7) != 0) {
            numBytes++;
        }

        // Encode the value
        UnsignedByte[] bytes = new UnsignedByte[numBytes];
        for (int i = 0; i < numBytes; i++) {
            // Grab the next 7 bits
            byte nextByte = (byte) (value & 0x7F);
            value >>>= 7;

            // If we're not done, set the highest bit
            if (i != numBytes - 1) {
                nextByte |= 0x80;
            }

            // Store the byte
            bytes[i] = UnsignedByte.of(nextByte);
        }

        return bytes;
    }

    public static long decodeVariableSizedInteger(ArrayReader<UnsignedByte> byteReader) {
        long value = 0;

        // If the highest bit is set, read the next byte
        int numBytes = 0;
        while ((byteReader.peek().toByte() & 0x80) != 0) {
            value |= (long) (byteReader.read().toByte() & 0x7F) << (7 * numBytes);
            numBytes++;
        }

        // Read the last byte
        value |= (long) (byteReader.read().toByte() & 0x7F) << (7 * numBytes);

        // Use zig-zag encoding to decode negative numbers
        return (value >>> 1) ^ -(value & 1);
    }
}
