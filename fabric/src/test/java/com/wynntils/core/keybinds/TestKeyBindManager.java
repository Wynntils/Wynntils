/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.wynntils.core.WynntilsMod;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestKeyBindManager {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    public void migratesLegacyKeybindNamesToStableTranslationKeys() {
        List<String> migratedLines = KeyBindManager.migrateLegacyKeybindLines(List.of(
                "gamma:0.5",
                "key_Mount Horse:key.keyboard.r",
                "key_Open Guild Bank:key.keyboard.p",
                "key_View player's gear:key.mouse.middle",
                "key_Open Quest Book:key.keyboard.k"));

        Assertions.assertEquals(
                List.of(
                        "gamma:0.5",
                        "key_wynntils.keybind.mountHorse:key.keyboard.r",
                        "key_wynntils.keybind.openGuildBank:key.keyboard.p",
                        "key_wynntils.keybind.viewPlayer:key.mouse.middle",
                        "key_wynntils.keybind.openContentBook:key.keyboard.k"),
                migratedLines);
    }

    @Test
    public void keepsMigratedAndUnrelatedKeybindLinesAsTheyAre() {
        List<String> lines =
                List.of("key_wynntils.keybind.mountHorse:key.keyboard.r", "key_key.attack:key.mouse.left", "gamma:0.5");

        List<String> migratedLines = KeyBindManager.migrateLegacyKeybindLines(lines);

        Assertions.assertEquals(lines, migratedLines);
    }
}
