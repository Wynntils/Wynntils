/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.UnsignedByte;
import java.util.Arrays;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestUnsignedByteUtils {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void simpleBitArrayfromBitArray_works() {
        boolean[] bitArray = {
            true, true, false, false, false, true, true, false, false, false, true, true, true, true, true, true
        };

        UnsignedByte[] expected = {UnsignedByte.of((byte) 0b11000110), UnsignedByte.of((byte) 0b00111111)};
        UnsignedByte[] actual = UnsignedByteUtils.fromBitArray(bitArray);

        Assertions.assertArrayEquals(expected, actual, "fromBitArray did not return the expected value");
    }

    @Test
    public void toBitArrayReturnsCorrectBooleanArray_works() {
        UnsignedByte[] unsignedBytes = {UnsignedByte.of((byte) 0b10101010)};

        boolean[] expected = {true, false, true, false, true, false, true, false};
        boolean[] actual = UnsignedByteUtils.toBitArray(unsignedBytes);

        Assertions.assertArrayEquals(expected, actual, "toBitArray did not return the expected value");
    }

    @Test
    public void encodeStringReturnsCorrectUnsignedBytes_works() {
        String string = "Test";

        UnsignedByte[] expected = {
            UnsignedByte.of((byte) 'T'),
            UnsignedByte.of((byte) 'e'),
            UnsignedByte.of((byte) 's'),
            UnsignedByte.of((byte) 't'),
            UnsignedByte.of((byte) 0)
        };

        UnsignedByte[] actual = UnsignedByteUtils.encodeString(string);
        Assertions.assertArrayEquals(expected, actual, "encodeString did not return the expected value");
    }

    @Test
    public void decodeStringReturnsCorrectString_works() {
        List<UnsignedByte> byteReader = Arrays.asList(
                UnsignedByte.of((byte) 'T'),
                UnsignedByte.of((byte) 'e'),
                UnsignedByte.of((byte) 's'),
                UnsignedByte.of((byte) 't'));

        String expected = "Test";
        String actual = UnsignedByteUtils.decodeString(byteReader);

        Assertions.assertEquals(expected, actual, "decodeString did not return the expected value");
    }

    @Test
    public void encodeVariableSizedIntegerReturnsCorrectUnsignedBytes_works() {
        long value = 1234567890L;

        UnsignedByte[] expected = {
            UnsignedByte.of((byte) (value >> 24)),
            UnsignedByte.of((byte) (value >> 16)),
            UnsignedByte.of((byte) (value >> 8)),
            UnsignedByte.of((byte) value)
        };

        UnsignedByte[] actual = UnsignedByteUtils.encodeVariableSizedInteger(value);
        Assertions.assertArrayEquals(expected, actual, "encodeVariableSizedInteger did not return the expected value");
    }

    @Test
    public void decodeVariableSizedIntegerReturnsCorrectLong_works() {
        UnsignedByte[] bytes = {
            UnsignedByte.of((byte) 0x49),
            UnsignedByte.of((byte) 0x96),
            UnsignedByte.of((byte) 0x02),
            UnsignedByte.of((byte) 0xD2)
        };

        long expected = 1234567890L;
        long actual = UnsignedByteUtils.decodeVariableSizedInteger(bytes);

        Assertions.assertEquals(expected, actual, "decodeVariableSizedInteger did not return the expected value");
    }
}
