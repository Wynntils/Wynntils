/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.testutils.RegexTest;
import com.wynntils.testutils.TestPrivateFieldsUtil;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class ArchetypeAbilitiesAnnotatorTest extends RegexTest {
    @Test
    public void testArchetypeName() {
        Pattern pattern =
                TestPrivateFieldsUtil.getPrivateStaticRegexPattern(ArchetypeAbilitiesAnnotator.class, "ARCHETYPE_NAME");
        assertMatchesAny(pattern, "§e§lBoltslinger Archetype").assertGroupIs(1, "e");
        assertMatchesAny(pattern, "§d§lSharpshooter Archetype").assertGroupIs(1, "d");
        assertMatchesAny(pattern, "§2§lTrapper Archetype").assertGroupIs(1, "2");
        assertMatchesAny(pattern, "§d§lLight Bender Archetype").assertGroupIs(1, "d");
    }
    @Test
    public void testArchetypePattern(){
        Pattern pattern =
                TestPrivateFieldsUtil.getPrivateStaticRegexPattern(ArchetypeAbilitiesAnnotator.class, "ARCHETYPE_PATTERN");
        assertMatchesAny(pattern, "§a✔ §7Unlocked Abilities: §f14§7/15").assertGroupIs(1,"14").assertGroupIs(2,"15");
        assertMatchesAny(pattern, "§a✔ §7Unlocked Abilities: §f2§7/16").assertGroupIs(1,"2").assertGroupIs(2,"16");
        assertMatchesAny(pattern, "§a✔ §7Unlocked Abilities: §f0§7/15").assertGroupIs(1,"0").assertGroupIs(2,"15");
    }
}
