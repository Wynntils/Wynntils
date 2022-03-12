/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

import com.wynntils.core.config.reflection.ConfigField;
import com.wynntils.core.config.ui.ConfigBooleanWidget;
import com.wynntils.core.config.ui.base.ConfigWidget;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Handles providing of configs */
public class ConfigProviders {
    private static final List<ConfigProvider<?>> providers = new ArrayList<>();

    static {
        addProvider(Boolean.class, ConfigBooleanWidget::new);
    }

    public static <T> ConfigWidget<T> generate(ConfigField<T> o) {
        for (ConfigProvider<?> provider : providers) {
            if (provider.canGenerate(o)) {
                return (ConfigWidget<T>) tryGenerate(o, provider);
            }
        }

        throw new IllegalStateException("No providers for object " + o.getFieldValue().getClass());
    }

    @SuppressWarnings("unchecked")
    private static <T> ConfigWidget<?> tryGenerate(Object o, ConfigProvider<T> provider) {
        return provider.generate((ConfigField<T>) o);
    }

    public static <K, V extends ConfigWidget<K>> void addProvider(
            Class<K> key, Function<ConfigField<K>, V> value) {
        addProvider(
                new ConfigProvider<>(key) {
                    @Override
                    public V generate(ConfigField<K> field) {
                        return value.apply(field);
                    }
                });
    }

    public static void addProvider(ConfigProvider<?> toAdd) {
        for (int i = 0; i < providers.size(); i++) {
            if (providers.get(i).getClazz().isAssignableFrom(toAdd.getClazz())) {
                providers.add(i, toAdd);
                return;
            }
        }

        providers.add(toAdd);
    }

    public abstract static class ConfigProvider<T> {
        private final Class<T> clazz;

        public ConfigProvider(Class<T> clazz) {
            this.clazz = clazz;
        }

        public boolean canGenerate(ConfigField<?> field) {
            return clazz.isInstance(field.getFieldValue());
        }

        public Class<T> getClazz() {
            return clazz;
        }

        public abstract ConfigWidget<T> generate(ConfigField<T> field);
    }
}
