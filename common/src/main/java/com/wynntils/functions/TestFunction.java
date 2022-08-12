/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.Function;

public class TestFunction implements Function {
    @Override
    public String getValue(String argument) {
        return "42";
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getDescription() {
        return "Calculate the true value of the universe";
    }
}
