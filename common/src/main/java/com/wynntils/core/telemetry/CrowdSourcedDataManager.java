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
import com.wynntils.core.telemetry.type.CrowdSourcedDataGameVersion;
import com.wynntils.core.telemetry.type.CrowdSourcedDataType;
import com.wynntils.features.wynntils.TelemetryFeature;
import com.wynntils.telemetry.LootrunLocationDataCollector;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrowdSourcedDataManager extends Manager {
    private static final CrowdSourcedDataGameVersion CURRENT_GAME_VERSION =
            CrowdSourcedDataGameVersion.VERSION_203_HOTFIX_4;

    @Persisted
    private final Storage<CrowdSourcedData> collectedData = new Storage<>(new CrowdSourcedData());

    private final Map<CrowdSourcedDataType, CrowdSourcedDataCollector<?>> collectors = new HashMap<>();

    // We only indirectly depend on StorageManager, this manager has storages
    public CrowdSourcedDataManager(StorageManager storageManager) {
        super(List.of(storageManager));

        registerCollectors();
    }

    public <T> void putData(CrowdSourcedDataType crowdSourcedDataType, T telemetryData) {
        TelemetryFeature.ConfirmedBoolean collectionEnabledForType = Managers.Feature.getFeatureInstance(
                        TelemetryFeature.class)
                .telemetryTypeEnabledMap
                .get()
                .getOrDefault(crowdSourcedDataType, TelemetryFeature.ConfirmedBoolean.FALSE);
        if (collectionEnabledForType != TelemetryFeature.ConfirmedBoolean.TRUE) return;

        collectedData.get().putData(CURRENT_GAME_VERSION, crowdSourcedDataType, telemetryData);
        collectedData.touched();
    }

    public <T> Set<T> getData(CrowdSourcedDataType crowdSourcedDataType) {
        return (Set<T>) collectedData
                .get()
                .getData(CURRENT_GAME_VERSION, crowdSourcedDataType, crowdSourcedDataType.getDataClass());
    }

    private void registerCollectors() {
        registerCollector(CrowdSourcedDataType.LOOTRUN_TASK_LOCATIONS, new LootrunLocationDataCollector());
    }

    private void registerCollector(CrowdSourcedDataType crowdSourcedDataType, CrowdSourcedDataCollector<?> collector) {
        Class<?> collectorTypeClass = (Class<?>)
                ((ParameterizedType) collector.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        if (!collectorTypeClass.equals(crowdSourcedDataType.getDataClass())) {
            throw new IllegalStateException(
                    "The provided collector does not collect the provided crowd sourced data type.");
        }

        WynntilsMod.registerEventListener(collector);
        collectors.put(crowdSourcedDataType, collector);
    }
}
