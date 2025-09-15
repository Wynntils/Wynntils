/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.type.UnsignedByte;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestEncodedByteBuffer {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    public void simpleByteArray_toUtf16Works() {
        UnsignedByte[] bytes = UnsignedByte.of(new byte[] {(byte) 214, 121, 11, 49, 43, 75});

        String result = EncodedByteBuffer.fromBytes(bytes).toUtf16String();

        // Expected: "\uFD679\uF0B31\uF2B4B"
        final String expected = Character.toString(0xFD679) + Character.toString(0xF0B31) + Character.toString(0xF2B4B);

        Assertions.assertEquals(expected, result, "toUtf16String() did not return the correct string");
    }

    @Test
    public void highBytes_toUtf16Works() {
        UnsignedByte[] bytes = UnsignedByte.of(
                new byte[] {(byte) 255, (byte) 254, (byte) 255, (byte) 255, 0, (byte) 255, (byte) 255, 0});

        String result = EncodedByteBuffer.fromBytes(bytes).toUtf16String();

        // Expected: "\u100000\u100001\uF00FF\uFFF00"
        final String expected = Character.toString(0x100000)
                + Character.toString(0x100001)
                + Character.toString(0xF00FF)
                + Character.toString(0xFFF00);

        Assertions.assertEquals(expected, result, "toUtf16String() did not return the correct string");
    }

    @Test
    public void paddingBytesHighBytes_toUtf16Works() {
        UnsignedByte[] bytes = UnsignedByte.of(new byte[] {(byte) 255, (byte) 254, (byte) 255, (byte) 255, (byte) 255});

        String result = EncodedByteBuffer.fromBytes(bytes).toUtf16String();

        // Expected: "\u100000\u100001\u10FFEE"
        final String expected =
                Character.toString(0x100000) + Character.toString(0x100001) + Character.toString(0x10FFEE);

        Assertions.assertEquals(expected, result, "toUtf16String() did not return the correct string");
    }

    @Test
    public void paddingByte_toUtf16Works() {
        UnsignedByte[] bytes = UnsignedByte.of(new byte[] {(byte) 255, (byte) 254, (byte) 255, (byte) 255, 2});

        String result = EncodedByteBuffer.fromBytes(bytes).toUtf16String();

        // Expected: "\u100000\u100001\u1002EE"
        final String expected =
                Character.toString(0x100000) + Character.toString(0x100001) + Character.toString(0x1002EE);

        Assertions.assertEquals(expected, result, "toUtf16String() did not return the correct string");
    }

    @Test
    public void simpleDecoding_fromUtf16Works() {
        String string = Character.toString(0xFD239) + Character.toString(0xF0F51) + Character.toString(0xFDD5B);

        UnsignedByte[] result = EncodedByteBuffer.fromUtf16String(string).getBytes();

        UnsignedByte[] expected = UnsignedByte.of(new byte[] {(byte) 210, 57, 15, 81, (byte) 221, 91});

        Assertions.assertArrayEquals(expected, result, "fromUtf16String() did not return the correct byte array");
    }

    @Test
    public void highBytes_fromUtf16Works() {
        String string = Character.toString(0x100000) + Character.toString(0x100001) + Character.toString(0xF00FF);

        UnsignedByte[] result = EncodedByteBuffer.fromUtf16String(string).getBytes();

        UnsignedByte[] expected =
                UnsignedByte.of(new byte[] {(byte) 255, (byte) 254, (byte) 255, (byte) 255, 0, (byte) 255});

        Assertions.assertArrayEquals(expected, result, "fromUtf16String() did not return the correct byte array");
    }

    @Test
    public void padding_fromUtf16Works() {
        String string = Character.toString(0x100000) + Character.toString(0x100001) + Character.toString(0x1002EE);

        UnsignedByte[] result = EncodedByteBuffer.fromUtf16String(string).getBytes();

        UnsignedByte[] expected = UnsignedByte.of(new byte[] {(byte) 255, (byte) 254, (byte) 255, (byte) 255, 2});

        Assertions.assertArrayEquals(expected, result, "fromUtf16String() did not return the correct byte array");
    }
}
