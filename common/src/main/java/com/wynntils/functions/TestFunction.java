/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.Function;

public class TestFunction extends Function<Integer> {
    @Override
    public Integer getValue(String argument) {
        return 42;
    }
}
