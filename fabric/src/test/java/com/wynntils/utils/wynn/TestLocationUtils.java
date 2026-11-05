/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.utils.mc.type.Location;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class TestLocationUtils {
    @ParameterizedTest
    @MethodSource("validCoordinates")
    public void acceptsValidCoordinates(String input, Location expected) {
        Assertions.assertEquals(Optional.of(expected), LocationUtils.parseFromString(input));
    }

    private static Stream<Arguments> validCoordinates() {
        return Stream.of(
                Arguments.of("1 2 3", new Location(1, 2, 3)),
                Arguments.of("  1 2 3  ", new Location(1, 2, 3)),
                Arguments.of("  1   2   3   ", new Location(1, 2, 3)),
                Arguments.of("1,2,3", new Location(1, 2, 3)),
                Arguments.of("1, 2, 3", new Location(1, 2, 3)),
                Arguments.of("1,  2,  3", new Location(1, 2, 3)),
                Arguments.of("1/2/3", new Location(1, 2, 3)),
                Arguments.of("1.23, 4.56, 7.89", new Location(1, 4, 7)),
                Arguments.of("-1, +2, -3", new Location(-1, 2, -3)),
                Arguments.of("123456 123 123456", new Location(123456, 123, 123456)),
                Arguments.of("-123456 -123 -123456", new Location(-123456, -123, -123456)),
                Arguments.of("6 8", new Location(6, 0, 8)),
                Arguments.of("6,8", new Location(6, 0, 8)),
                Arguments.of("6, 8", new Location(6, 0, 8)),
                Arguments.of("6,  8", new Location(6, 0, 8)),
                Arguments.of("-6 -8", new Location(-6, 0, -8)),
                Arguments.of("-6.123 -8.123", new Location(-6, 0, -8)),
                Arguments.of("123456 123456", new Location(123456, 0, 123456)),
                Arguments.of("X: -1165, Z: -5054", new Location(-1165, 0, -5054)),
                Arguments.of("X: -1165 Y: 99 Z: -5054", new Location(-1165, 99, -5054)),
                Arguments.of("x = 37, y = -5, z = 1056", new Location(37, -5, 1056)),
                Arguments.of("x=37, y=-5, z=1056", new Location(37, -5, 1056)),
                Arguments.of("x=37,y=-5,z=1056", new Location(37, -5, 1056)),
                Arguments.of("X=37, Y=-5, Z=1056", new Location(37, -5, 1056)),
                Arguments.of("x:37, y:-5, z:1056", new Location(37, -5, 1056)),
                Arguments.of("x: 37, y: -5, z: 1056", new Location(37, -5, 1056)),
                Arguments.of("y: -5, z: 1056, x: 37", new Location(37, -5, 1056)),
                Arguments.of("x37y-5z1056", new Location(37, -5, 1056)),
                Arguments.of("(37, -5, 1056)", new Location(37, -5, 1056)),
                Arguments.of("(37 -5 1056)", new Location(37, -5, 1056)),
                Arguments.of("<37, -5, 1056>", new Location(37, -5, 1056)),
                Arguments.of("[37, -5, 1056]", new Location(37, -5, 1056)),
                Arguments.of("{37, -5, 1056}", new Location(37, -5, 1056)),
                Arguments.of("(x:37, y:-5, z:1056)", new Location(37, -5, 1056)),
                Arguments.of("37.0, -5.0, 1056.0", new Location(37, -5, 1056)),
                Arguments.of("123456 123 123456", new Location(123456, 123, 123456)),
                Arguments.of("-123456 -123 -123456", new Location(-123456, -123, -123456)));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "",
                " ",
                "      ",
                "morph!",
                "no morph!",
                "not a coordinate",
                "1a2a3",
                "42",
                "1b23 456 789",
                "123 4b56 789",
                "123 456 7b89",
                "1b23 456",
                "123 4b56",
                "1. 2. 3.",
                "1., 2., 3.,",
                "x",
                "x:",
                "x:5",
                "x:5, y:5",
                "37, -5, 1056, 783",
                "37, -, 1056",
                "a, -5, 1056",
                "37, a, 1056",
                "37, -5, a",
                "x=1, 2, 3",
                "1, y=2, 3",
                "1, 2, z=3",
                "=37, y=-5, z=1056",
                "x=37, =-5, z=1056",
                "x=37, y=-5, =1056",
                "x:37, :-5, z:1056",
                "y:37, y:-5, z:1056",
                "y:37, y:-5, x:1056",
                "x:37, y: -5",
                "x:37, y: -5, 37",
                "x:1, x:2, x:3",
                "y:1, y:2, y:3",
                "z:1, z:2, z:3",
                "x:1, z:2, d:3",
                "(37, -5, 1056>",
                "((37, -5, 1056)",
                "(37, -5, 1056))",
                "37, -5, 1056)",
                "(37, -5, 1056",
                "(invalid37, -5, 1056)",
                "37,,-5,1056",
                "x:37 ,-5.0 / 1056 ",
                "37, y=-5, z=1056",
                "1234567 8 9",
                "1 2345 6",
                "1 2 3456789",
                "9999999999999999999",
                "9999999999999999999 9999999999999999999",
                "9999999999999999999 9999999999999999999 9999999999999999999",
                "NaN, NaN, NaN",
                "Infinity, Infinity, Infinity"
            })
    public void rejectsInvalidCoordinates(String input) {
        Assertions.assertTrue(LocationUtils.parseFromString(input).isEmpty());
    }
}
