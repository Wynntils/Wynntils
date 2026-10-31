/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.google.common.base.CaseFormat;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.type.MapDataProvider;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapDataProvidedType;
import com.wynntils.services.mapdata.type.MapIcon;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;

public abstract class BuiltInProvider implements MapDataProvider {
    protected final List<Consumer<MapDataProvidedType>> callbacks = new ArrayList<>();

    public abstract String getProviderId();

    public Component getName() {
        return Component.translatable(getTranslationKey("name"));
    }

    public Component getDescription() {
        return Component.translatable(getTranslationKey("description"));
    }

    private String getTranslationKey(String suffix) {
        String keyName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, getProviderId());
        return "service.wynntils.mapdata.provider." + keyName + "." + suffix;
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return Stream.empty();
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return Stream.empty();
    }

    @Override
    public Stream<MapIcon> getIcons() {
        return Stream.empty();
    }

    @Override
    public void onChange(Consumer<MapDataProvidedType> callback) {
        callbacks.add(callback);
    }

    protected void notifyCallbacks(MapDataProvidedType type) {
        callbacks.forEach(c -> c.accept(type));
    }
}
