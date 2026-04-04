/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.wynntils.core.WynntilsMod;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

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

    @Test
    public void resolvesLegacyAndStableKeybindNamesToTheSameDefinition() {
        KeyBindDefinition legacyNameDefinition = KeyBindManager.getKeyBindDefinition("Share Item");
        KeyBindDefinition stableNameDefinition = KeyBindManager.getKeyBindDefinition("wynntils.keybind.shareItem");

        Assertions.assertNotNull(legacyNameDefinition);
        Assertions.assertSame(legacyNameDefinition, stableNameDefinition);
    }

    @Test
    public void keepsOnlyLegacyOverridesWhoseLatestEntryWasNotMigrated() {
        Map<String, com.mojang.blaze3d.platform.InputConstants.Key> legacyOverrides =
                KeyBindManager.getLegacyKeybindOverrides(List.of(
                        "key_Share Item:key.keyboard.f5",
                        "key_wynntils.keybind.shareItem:key.keyboard.f6",
                        "key_View player's gear:key.mouse.middle"));

        Assertions.assertFalse(legacyOverrides.containsKey(KeyBindDefinition.SHARE_ITEM.id()));
        Assertions.assertEquals(
                GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
                legacyOverrides.get(KeyBindDefinition.VIEW_PLAYER.id()).getValue());
    }

    @Test
    public void resolvesOnlyActiveKeyMappingsForHintCompatibility() {
        KeyMapping activeShareItemMapping = new KeyMapping(
                KeyBindDefinition.SHARE_ITEM.translationKey(),
                KeyBindDefinition.SHARE_ITEM.type(),
                KeyBindDefinition.SHARE_ITEM.defaultKey(),
                KeyBindDefinition.SHARE_ITEM.category());
        KeyMapping inactiveViewPlayerMapping = new KeyMapping(
                KeyBindDefinition.VIEW_PLAYER.translationKey(),
                KeyBindDefinition.VIEW_PLAYER.type(),
                KeyBindDefinition.VIEW_PLAYER.defaultKey(),
                KeyBindDefinition.VIEW_PLAYER.category());

        Map<String, KeyMapping> mappingsById = Map.of(
                KeyBindDefinition.SHARE_ITEM.id(),
                activeShareItemMapping,
                KeyBindDefinition.VIEW_PLAYER.id(),
                inactiveViewPlayerMapping);

        Assertions.assertSame(
                activeShareItemMapping,
                KeyBindManager.findActiveKeyMapping(
                        "Share Item", new KeyMapping[] {activeShareItemMapping}, mappingsById));
        Assertions.assertSame(
                activeShareItemMapping,
                KeyBindManager.findActiveKeyMapping(
                        KeyBindDefinition.SHARE_ITEM.translationKey(),
                        new KeyMapping[] {activeShareItemMapping},
                        mappingsById));
        Assertions.assertNull(KeyBindManager.findActiveKeyMapping(
                "View player's gear", new KeyMapping[] {activeShareItemMapping}, mappingsById));
    }
}
