/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.telemetry;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.persisted.storage.StorageManager;
import com.wynntils.core.telemetry.type.TelemetryGameVersion;
import com.wynntils.core.telemetry.type.TelemetryType;
import com.wynntils.features.wynntils.TelemetryFeature;
import com.wynntils.telemetry.LootrunLocationTelemetryCollector;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TelemetryManager extends Manager {
    private static final TelemetryGameVersion CURRENT_GAME_VERSION = TelemetryGameVersion.VERSION_203_HOTFIX_4;

    @Persisted
    private final Storage<TelemetryData> collectedData = new Storage<>(new TelemetryData());

    private final Map<TelemetryType, TelemetryCollector<?>> collectors = new HashMap<>();

    // We only indirectly depend on StorageManager, this manager has storages
    public TelemetryManager(StorageManager storageManager) {
        super(List.of(storageManager));

        registerCollectors();
    }

    public <T> void putData(TelemetryType telemetryType, T telemetryData) {
        TelemetryFeature.ConfirmedBoolean telemetryEnabledForType = Managers.Feature.getFeatureInstance(
                        TelemetryFeature.class)
                .telemetryTypeEnabledMap
                .get()
                .getOrDefault(telemetryType, TelemetryFeature.ConfirmedBoolean.FALSE);
        if (telemetryEnabledForType != TelemetryFeature.ConfirmedBoolean.TRUE) return;

        collectedData.get().putData(CURRENT_GAME_VERSION, telemetryType, telemetryData);
        collectedData.touched();
    }

    public <T> Set<T> getData(TelemetryType telemetryType) {
        return (Set<T>) collectedData.get().getData(CURRENT_GAME_VERSION, telemetryType, telemetryType.getDataClass());
    }

    private void registerCollectors() {
        registerCollector(TelemetryType.LOOTRUN_TASK_LOCATIONS, new LootrunLocationTelemetryCollector());
    }

    private void registerCollector(TelemetryType telemetryType, TelemetryCollector<?> collector) {
        Class<?> collectorTypeClass = (Class<?>)
                ((ParameterizedType) collector.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        if (!collectorTypeClass.equals(telemetryType.getDataClass())) {
            throw new IllegalStateException("The provided collector does not collect the provided telemetry type.");
        }

        WynntilsMod.registerEventListener(collector);
        collectors.put(telemetryType, collector);
    }
}
