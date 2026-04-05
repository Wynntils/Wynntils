/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.wynntils.core.WynntilsMod;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestLegacyKeybindFieldAccess {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    public void migratesLegacyKeybindWhenStableOptionsKeyIsMissing() {
        AtomicBoolean migrated = new AtomicBoolean(false);
        LegacyKeybindFieldAccess fieldAccess = new LegacyKeybindFieldAccess(
                new TestFieldAccess(Map.of(KeyBindDefinition.SHARE_ITEM.legacyOptionsKey(), "key.keyboard.g")),
                () -> migrated.set(true));

        String value = fieldAccess.process(KeyBindDefinition.SHARE_ITEM.optionsKey(), "key.keyboard.f5");

        Assertions.assertEquals("key.keyboard.g", value);
        Assertions.assertTrue(migrated.get());
    }

    @Test
    public void prefersStableKeybindWhenStableOptionsKeyExists() {
        AtomicBoolean migrated = new AtomicBoolean(false);
        LegacyKeybindFieldAccess fieldAccess = new LegacyKeybindFieldAccess(
                new TestFieldAccess(Map.of(
                        KeyBindDefinition.SHARE_ITEM.optionsKey(),
                        "key.keyboard.f8",
                        KeyBindDefinition.SHARE_ITEM.legacyOptionsKey(),
                        "key.keyboard.g")),
                () -> migrated.set(true));

        String value = fieldAccess.process(KeyBindDefinition.SHARE_ITEM.optionsKey(), "key.keyboard.f5");

        Assertions.assertEquals("key.keyboard.f8", value);
        Assertions.assertFalse(migrated.get());
    }

    @Test
    public void leavesNonKeybindStringOptionsUntouched() {
        AtomicBoolean migrated = new AtomicBoolean(false);
        LegacyKeybindFieldAccess fieldAccess =
                new LegacyKeybindFieldAccess(new TestFieldAccess(Map.of("lang", "en_gb")), () -> migrated.set(true));

        String value = fieldAccess.process("lang", "en_us");

        Assertions.assertEquals("en_gb", value);
        Assertions.assertFalse(migrated.get());
    }

    private static final class TestFieldAccess implements Options.FieldAccess {
        private final Map<String, String> values;

        private TestFieldAccess(Map<String, String> values) {
            this.values = values;
        }

        @Override
        public <T> void process(String key, OptionInstance<T> optionInstance) {}

        @Override
        public int process(String key, int currentValue) {
            return currentValue;
        }

        @Override
        public boolean process(String key, boolean currentValue) {
            return currentValue;
        }

        @Override
        public String process(String key, String currentValue) {
            return values.getOrDefault(key, currentValue);
        }

        @Override
        public float process(String key, float currentValue) {
            return currentValue;
        }

        @Override
        public <T> T process(String key, T currentValue, Function<String, T> decoder, Function<T, String> encoder) {
            return currentValue;
        }
    }
}
