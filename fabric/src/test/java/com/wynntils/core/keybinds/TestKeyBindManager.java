/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.wynntils.core.WynntilsMod;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestKeyBindManager {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    public void createsLegacyAliasesForStableKeybindOptions() {
        CompoundTag options = new CompoundTag();
        options.putString(KeyBindDefinition.RIDE_MOUNT.legacyOptionsKey(), "key.keyboard.r");
        options.putString(KeyBindDefinition.OPEN_GUILD_BANK.legacyOptionsKey(), "key.keyboard.p");
        options.putString(KeyBindDefinition.VIEW_PLAYER.legacyOptionsKey(), "key.mouse.middle");
        options.putString(KeyBindDefinition.OPEN_CONTENT_BOOK.legacyOptionsKey(), "key.keyboard.k");

        Map<String, String> legacyAliases = KeyBindManager.getLegacyKeybindAliases(options);

        Assertions.assertEquals("key.keyboard.r", legacyAliases.get(KeyBindDefinition.RIDE_MOUNT.optionsKey()));
        Assertions.assertEquals("key.keyboard.p", legacyAliases.get(KeyBindDefinition.OPEN_GUILD_BANK.optionsKey()));
        Assertions.assertEquals("key.mouse.middle", legacyAliases.get(KeyBindDefinition.VIEW_PLAYER.optionsKey()));
        Assertions.assertEquals("key.keyboard.k", legacyAliases.get(KeyBindDefinition.OPEN_CONTENT_BOOK.optionsKey()));
    }

    @Test
    public void skipsLegacyAliasWhenStableKeyAlreadyExists() {
        CompoundTag options = new CompoundTag();
        options.putString(KeyBindDefinition.SHARE_ITEM.legacyOptionsKey(), "key.keyboard.f5");
        options.putString(KeyBindDefinition.SHARE_ITEM.optionsKey(), "key.keyboard.f6");

        Map<String, String> legacyAliases = KeyBindManager.getLegacyKeybindAliases(options);

        Assertions.assertFalse(legacyAliases.containsKey(KeyBindDefinition.SHARE_ITEM.optionsKey()));
    }

    @Test
    public void resolvesLegacyAndStableKeybindNamesToTheSameDefinition() {
        KeyBindDefinition legacyNameDefinition = KeyBindManager.getKeyBindDefinition("Share Item");
        KeyBindDefinition stableNameDefinition = KeyBindManager.getKeyBindDefinition("wynntils.keybind.shareItem");

        Assertions.assertNotNull(legacyNameDefinition);
        Assertions.assertSame(legacyNameDefinition, stableNameDefinition);
    }

    @Test
    public void ignoresUnrelatedOptionsWhenCreatingLegacyAliases() {
        CompoundTag options = new CompoundTag();
        options.putString("key_key.attack", "key.mouse.left");
        options.putString("gamma", "0.5");

        Map<String, String> legacyAliases = KeyBindManager.getLegacyKeybindAliases(options);

        Assertions.assertEquals(Map.of(), legacyAliases);
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
