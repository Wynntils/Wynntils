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
import net.minecraft.client.gui.components.Widget;

/** Handles providing of configs */
public class ConfigProviders {
    private static final List<ConfigProvider<?>> providers = new ArrayList<>();

    static {
        addProvider(
                new ConfigProvider<>(Boolean.class) {
                    @Override
                    public ConfigWidget<Boolean> generate(ConfigField<Boolean> field) {
                        return new ConfigBooleanWidget(field);
                    }
                });
    }

    public static Widget generate(ConfigField<?> o) {
        for (ConfigProvider<?> provider : providers) {
            if (provider.canGenerate(o)) {
                return tryGenerate(o, provider);
            }
        }

        throw new IllegalStateException("No providers for object " + o);
    }

    @SuppressWarnings("unchecked")
    private static <T> Widget tryGenerate(Object o, ConfigProvider<T> provider) {
        return provider.generate((ConfigField<T>) o);
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
            return clazz.isInstance(field.getFieldValue().getClass());
        }

        public Class<T> getClazz() {
            return clazz;
        }

        public abstract ConfigWidget<T> generate(ConfigField<T> field);
    }
}
