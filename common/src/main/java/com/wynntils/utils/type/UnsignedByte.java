/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import java.util.Objects;

public final class UnsignedByte {
    private final short value;

    public UnsignedByte(byte value) {
        this.value = (short) (value & 0xFF);
    }

    public static UnsignedByte of(byte value) {
        return new UnsignedByte(value);
    }

    public static UnsignedByte[] of(byte[] values) {
        UnsignedByte[] result = new UnsignedByte[values.length];

        for (int i = 0; i < values.length; i++) {
            result[i] = of(values[i]);
        }

        return result;
    }

    public static byte[] toPrimitive(UnsignedByte[] values) {
        byte[] result = new byte[values.length];

        for (int i = 0; i < values.length; i++) {
            // Convert unsigned bytes to signed bytes
            result[i] = (byte) values[i].value();
        }

        return result;
    }

    public static short[] asShort(UnsignedByte[] values) {
        short[] result = new short[values.length];

        for (int i = 0; i < values.length; i++) {
            // Convert unsigned bytes to signed bytes
            result[i] = values[i].value();
        }

        return result;
    }

    /**
     * Returns the value of this {@link UnsignedByte} as an int.
     * @return the value of this {@link UnsignedByte} as an int, in the range 0 to 255
     */
    public short value() {
        return value;
    }

    public byte toByte() {
        return (byte) value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnsignedByte that = (UnsignedByte) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "UnsignedByte{" + value + '}';
    }
}
