/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.launch;

import com.wynntils.core.components.Service;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import java.util.List;

public final class LaunchCounterService extends Service {
    @Persisted
    private final Storage<Integer> launchCount = new Storage<>(0);

    public LaunchCounterService() {
        super(List.of());
    }

    @Override
    public void onStorageLoad(Storage<?> storage) {
        if (storage != launchCount) return;

        launchCount.store(launchCount.get() + 1);
    }

    public boolean hasCompletedLaunches(int launches) {
        return launchCount.get() > launches;
    }
}
