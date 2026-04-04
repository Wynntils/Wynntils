/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.Options;

final class LegacyKeybindFieldAccess implements Options.FieldAccess {
    private final Options.FieldAccess delegate;
    private final Map<String, String> legacyAliases;
    private final Runnable markMigrated;

    LegacyKeybindFieldAccess(Options.FieldAccess delegate, Map<String, String> legacyAliases, Runnable markMigrated) {
        this.delegate = delegate;
        this.legacyAliases = legacyAliases;
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
        String legacyValue = legacyAliases.get(key);
        if (legacyValue != null) {
            markMigrated.run();
            return legacyValue;
        }

        return delegate.process(key, currentValue);
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
