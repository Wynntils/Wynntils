/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.upfixers.impl;

import com.wynntils.core.config.upfixers.RenamedPrefixesUpfixer;
import com.wynntils.utils.type.Pair;
import java.util.List;

public class MapToMainMapRenamedConfigsUpfixer extends RenamedPrefixesUpfixer {
    private static final List<Pair<String, String>> RENAMED_PREFIXES =
            List.of(Pair.of("mapFeature.", "mainMapFeature."));

    @Override
    protected List<Pair<String, String>> getRenamedPrefixes() {
        return RENAMED_PREFIXES;
    }
}
