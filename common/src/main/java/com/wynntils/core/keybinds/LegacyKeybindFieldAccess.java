/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import java.util.function.Function;
import net.minecraft.client.Options;

public final class LegacyKeybindFieldAccess implements Options.FieldAccess {
    private static final String MISSING_KEYBIND_SENTINEL = "__wynntils_missing_keybind__";

    private final Options.FieldAccess delegate;
    private final KeyBindManager keyBindManager;
    private final Runnable markMigrated;

    public LegacyKeybindFieldAccess(
            Options.FieldAccess delegate, KeyBindManager keyBindManager, Runnable markMigrated) {
        this.delegate = delegate;
        this.keyBindManager = keyBindManager;
        this.markMigrated = markMigrated;
    }

    @Override
    public <T> void process(String key, net.minecraft.client.OptionInstance<T> optionInstance) {
        delegate.process(key, optionInstance);
    }

    @Override
    public int process(String key, int currentValue) {
        return delegate.process(key, currentValue);
    }

    @Override
    public boolean process(String key, boolean currentValue) {
        return delegate.process(key, currentValue);
    }

    @Override
    public String process(String key, String currentValue) {
        KeyBindDefinition definition = keyBindManager.getKeyBindDefinition(key);
        if (definition == null || !definition.optionsKey().equals(key)) {
            return delegate.process(key, currentValue);
        }

        String stableValue = delegate.process(key, MISSING_KEYBIND_SENTINEL);
        if (!MISSING_KEYBIND_SENTINEL.equals(stableValue)) {
            return stableValue;
        }

        String legacyValue = delegate.process(definition.legacyOptionsKey(), MISSING_KEYBIND_SENTINEL);
        if (MISSING_KEYBIND_SENTINEL.equals(legacyValue)) {
            return currentValue;
        }

        markMigrated.run();
        return legacyValue;
    }

    @Override
    public float process(String key, float currentValue) {
        return delegate.process(key, currentValue);
    }

    @Override
    public <T> T process(String key, T currentValue, Function<String, T> decoder, Function<T, String> encoder) {
        return delegate.process(key, currentValue, decoder, encoder);
    }
}
