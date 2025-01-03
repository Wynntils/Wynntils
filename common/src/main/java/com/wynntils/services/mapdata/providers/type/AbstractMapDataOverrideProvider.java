/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.type;

import com.wynntils.services.mapdata.type.MapDataProvidedType;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public abstract class AbstractMapDataOverrideProvider implements MapDataOverrideProvider {
    protected final Set<Consumer<MapDataProvidedType>> callbacks = new CopyOnWriteArraySet<>();

    @Override
    public void onChange(Consumer<MapDataProvidedType> callback) {
        callbacks.add(callback);
    }

    public void notifyCallbacks(MapDataProvidedType type) {
        callbacks.forEach(c -> c.accept(type));
    }
}
