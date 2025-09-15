/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.type.UnsignedByte;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestUnsignedByte {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    public void simpleByteArray_toUnsignedByteWorks() {
        UnsignedByte[] bytes = UnsignedByte.of(new byte[] {(byte) 214, 121, 11, 49, 43, 75});

        short[] result = UnsignedByte.asShort(bytes);

        final short[] expected = {214, 121, 11, 49, 43, 75};

        Assertions.assertArrayEquals(expected, result, "toUnsignedByte() did not return the correct bytes");
    }

    @Test
    public void simpleByteArray_toPrimitiveWorks() {
        UnsignedByte[] bytes = UnsignedByte.of(new byte[] {(byte) 214, 121, 11, 49, 43, 75});

        byte[] result = UnsignedByte.toPrimitive(bytes);

        final byte[] expected = {(byte) 214, 121, 11, 49, 43, 75};

        Assertions.assertArrayEquals(expected, result, "toPrimitive() did not return the correct bytes");
    }

    @Test
    public void simpleByteArray_fromNegativeBytesWorks() {
        byte[] bytes = {-1, -2, -127, -128, 0};

        UnsignedByte[] result = UnsignedByte.of(bytes);

        final UnsignedByte[] expected = {
            UnsignedByte.of((byte) 255),
            UnsignedByte.of((byte) 254),
            UnsignedByte.of((byte) 129),
            UnsignedByte.of((byte) 128),
            UnsignedByte.of((byte) 0)
        };

        Assertions.assertArrayEquals(expected, result, "of() did not transform the bytes correctly");
    }

    @Test
    public void simpleByteArray_fromPositiveBytesWorks() {
        byte[] bytes = {1, 2, 127, 0};

        UnsignedByte[] result = UnsignedByte.of(bytes);

        final UnsignedByte[] expected = {
            UnsignedByte.of((byte) 1), UnsignedByte.of((byte) 2), UnsignedByte.of((byte) 127), UnsignedByte.of((byte) 0)
        };

        Assertions.assertArrayEquals(expected, result, "of() did not transform the bytes correctly");
    }
}
