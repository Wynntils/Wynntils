/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.telemetry;

import com.wynntils.core.components.Manager;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.persisted.storage.StorageManager;
import com.wynntils.core.telemetry.type.TelemetryGameVersion;
import com.wynntils.core.telemetry.type.TelemetryType;
import java.util.List;
import java.util.Set;

public class TelemetryManager extends Manager {
    private static final TelemetryGameVersion CURRENT_GAME_VERSION = TelemetryGameVersion.VERSION_203_HOTFIX_4;

    @Persisted
    private final Storage<TelemetryData> collectedData = new Storage<>(new TelemetryData());

    // We only indirectly depend on StorageManager, this manager has storages
    public TelemetryManager(StorageManager storageManager) {
        super(List.of(storageManager));
    }

    public <T> void putData(TelemetryType telemetryType, T telemetryData) {
        collectedData.get().putData(CURRENT_GAME_VERSION, telemetryType, telemetryData);
        collectedData.touched();
    }

    public <T> Set<T> getData(TelemetryType telemetryType) {
        return (Set<T>) collectedData.get().getData(CURRENT_GAME_VERSION, telemetryType, telemetryType.getDataClass());
    }
}
