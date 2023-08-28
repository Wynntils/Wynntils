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
import com.wynntils.core.telemetry.type.CrowdSourceDataGameVersion;
import com.wynntils.core.telemetry.type.CrowdSourceDataType;
import com.wynntils.features.wynntils.TelemetryFeature;
import com.wynntils.telemetry.LootrunLocationDataCollector;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrowdSourceDataManager extends Manager {
    private static final CrowdSourceDataGameVersion CURRENT_GAME_VERSION =
            CrowdSourceDataGameVersion.VERSION_203_HOTFIX_4;

    @Persisted
    private final Storage<CrowdSourceData> collectedData = new Storage<>(new CrowdSourceData());

    private final Map<CrowdSourceDataType, CrowdSourceDataCollector<?>> collectors = new HashMap<>();

    // We only indirectly depend on StorageManager, this manager has storages
    public CrowdSourceDataManager(StorageManager storageManager) {
        super(List.of(storageManager));

        registerCollectors();
    }

    public <T> void putData(CrowdSourceDataType crowdSourceDataType, T telemetryData) {
        TelemetryFeature.ConfirmedBoolean collectionEnabledForType = Managers.Feature.getFeatureInstance(
                        TelemetryFeature.class)
                .telemetryTypeEnabledMap
                .get()
                .getOrDefault(crowdSourceDataType, TelemetryFeature.ConfirmedBoolean.FALSE);
        if (collectionEnabledForType != TelemetryFeature.ConfirmedBoolean.TRUE) return;

        collectedData.get().putData(CURRENT_GAME_VERSION, crowdSourceDataType, telemetryData);
        collectedData.touched();
    }

    public <T> Set<T> getData(CrowdSourceDataType crowdSourceDataType) {
        return (Set<T>) collectedData
                .get()
                .getData(CURRENT_GAME_VERSION, crowdSourceDataType, crowdSourceDataType.getDataClass());
    }

    private void registerCollectors() {
        registerCollector(CrowdSourceDataType.LOOTRUN_TASK_LOCATIONS, new LootrunLocationDataCollector());
    }

    private void registerCollector(CrowdSourceDataType crowdSourceDataType, CrowdSourceDataCollector<?> collector) {
        Class<?> collectorTypeClass = (Class<?>)
                ((ParameterizedType) collector.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        if (!collectorTypeClass.equals(crowdSourceDataType.getDataClass())) {
            throw new IllegalStateException(
                    "The provided collector does not collect the provided crowd sourced data type.");
        }

        WynntilsMod.registerEventListener(collector);
        collectors.put(crowdSourceDataType, collector);
    }
}
