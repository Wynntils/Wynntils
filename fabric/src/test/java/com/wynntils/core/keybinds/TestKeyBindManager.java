/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestKeyBindManager {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    public void resolvesLegacyStableAndOptionsKeybindNamesToTheSameDefinition() {
        KeyBindDefinition legacyNameDefinition = Managers.KeyBind.getKeyBindDefinition("Share Item");
        KeyBindDefinition stableNameDefinition = Managers.KeyBind.getKeyBindDefinition("wynntils.keybind.shareItem");
        KeyBindDefinition optionsKeyDefinition =
                Managers.KeyBind.getKeyBindDefinition("key_wynntils.keybind.shareItem");

        Assertions.assertNotNull(legacyNameDefinition);
        Assertions.assertSame(legacyNameDefinition, stableNameDefinition);
        Assertions.assertSame(legacyNameDefinition, optionsKeyDefinition);
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
                Managers.KeyBind.findActiveKeyMapping(
                        "Share Item", new KeyMapping[] {activeShareItemMapping}, mappingsById));
        Assertions.assertSame(
                activeShareItemMapping,
                Managers.KeyBind.findActiveKeyMapping(
                        KeyBindDefinition.SHARE_ITEM.translationKey(),
                        new KeyMapping[] {activeShareItemMapping},
                        mappingsById));
        Assertions.assertNull(Managers.KeyBind.findActiveKeyMapping(
                "View player's gear", new KeyMapping[] {activeShareItemMapping}, mappingsById));
    }
}
