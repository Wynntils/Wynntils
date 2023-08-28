/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.testutils.RegexTest;
import com.wynntils.testutils.TestPrivateFieldsUtil;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class BulkBuyFeatureTest extends RegexTest {
    @Test
    public void testPricePattern() {
        Pattern pattern = TestPrivateFieldsUtil.getPrivateStaticRegexPattern(BulkBuyFeature.class, "PRICE_PATTERN");
        assertMatchesAny(pattern, "§6 - §a✔ §f10§7²");
        assertMatchesAny(pattern, "§6 - §a✔ §f1483§7²");
        assertMatchesAny(pattern, "§6 - §c✖ §f244§7²");
        assertMatchesAny(pattern, "§6 - §c✖ §f1§7²");
        assertMatchesAny(pattern, "§6 - §a✔ §f24§7²");
    }
}
