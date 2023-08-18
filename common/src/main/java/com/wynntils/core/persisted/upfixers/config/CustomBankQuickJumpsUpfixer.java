/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.wynntils.core.persisted.upfixers.RenamedKeysUpfixer;
import com.wynntils.utils.type.Pair;
import java.util.List;

public class CustomBankQuickJumpsUpfixer extends RenamedKeysUpfixer {
    private static final List<Pair<String, String>> RENAMED_KEYS = List.of(
            Pair.of("customBankPagesFeature.bankDestinations", "customBankQuickJumpsFeature.bankDestinations"),
            Pair.of(
                    "customBankPagesFeature.blockBankDestinations",
                    "customBankQuickJumpsFeature.blockBankDestinations"),
            Pair.of(
                    "customBankPagesFeature.bookshelfDestinations",
                    "customBankQuickJumpsFeature.bookshelfDestinations"),
            Pair.of(
                    "customBankPagesFeature.miscBucketDestinations",
                    "customBankQuickJumpsFeature.miscBucketDestinations"));

    @Override
    protected List<Pair<String, String>> getRenamedKeys() {
        return RENAMED_KEYS;
    }
}
