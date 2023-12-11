/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.UnsignedByte;
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
    public void simpleBitArray_toUnsignedBytesWork() {
        boolean[] bitArray = {
            true, true, false, false, false, true, true, false, false, false, true, true, true, true, true, true
        };

        UnsignedByte[] expected = {UnsignedByte.of((byte) 0b11000110), UnsignedByte.of((byte) 0b00111111)};
        UnsignedByte[] actual = UnsignedByteUtils.fromBitArray(bitArray);

        Assertions.assertArrayEquals(expected, actual, "fromBitArray did not return the expected value");
    }
}
