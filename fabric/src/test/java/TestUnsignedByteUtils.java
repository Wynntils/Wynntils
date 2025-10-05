/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.UnsignedByte;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TestUnsignedByteUtils {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
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

    private static Stream<Arguments> provideEncodeDecodeTestData() {
        return Stream.of(
                Arguments.of(0, new UnsignedByte[] {UnsignedByte.of((byte) 0)}),
                Arguments.of(23, new UnsignedByte[] {UnsignedByte.of((byte) 46)}),
                Arguments.of(-10, new UnsignedByte[] {UnsignedByte.of((byte) 19)}),
                Arguments.of(321561, new UnsignedByte[] {
                    UnsignedByte.of((byte) 178), UnsignedByte.of((byte) 160), UnsignedByte.of((byte) 39)
                }),
                Arguments.of(-858101, new UnsignedByte[] {
                    UnsignedByte.of((byte) 233), UnsignedByte.of((byte) 223), UnsignedByte.of((byte) 104)
                }),
                Arguments.of(421581855L, new UnsignedByte[] {
                    UnsignedByte.of((byte) 190),
                    UnsignedByte.of((byte) 208),
                    UnsignedByte.of((byte) 134),
                    UnsignedByte.of((byte) 146),
                    UnsignedByte.of((byte) 3)
                }),
                Arguments.of(-3426567157L, new UnsignedByte[] {
                    UnsignedByte.of((byte) 233),
                    UnsignedByte.of((byte) 143),
                    UnsignedByte.of((byte) 234),
                    UnsignedByte.of((byte) 195),
                    UnsignedByte.of((byte) 25)
                }),
                Arguments.of(Long.MAX_VALUE, new UnsignedByte[] {
                    UnsignedByte.of((byte) 254),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 1)
                }),
                Arguments.of(Long.MIN_VALUE, new UnsignedByte[] {
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 255),
                    UnsignedByte.of((byte) 1)
                }));
    }

    @ParameterizedTest
    @MethodSource("provideEncodeDecodeTestData")
    public void testEncodeVariableSizedInteger(long input, UnsignedByte[] expectedOutput) {
        UnsignedByte[] actualOutput = UnsignedByteUtils.encodeVariableSizedInteger(input);
        Assertions.assertArrayEquals(
                expectedOutput, actualOutput, "encodeVariableSizedInteger did not return the expected value");
    }

    @ParameterizedTest
    @MethodSource("provideEncodeDecodeTestData")
    public void testDecodeVariableSizedInteger(long expectedOutput, UnsignedByte[] input) {
        long actualOutput = UnsignedByteUtils.decodeVariableSizedInteger(new ArrayReader<>(input));
        Assertions.assertEquals(
                expectedOutput, actualOutput, "decodeVariableSizedInteger did not return the expected value");
    }
}
