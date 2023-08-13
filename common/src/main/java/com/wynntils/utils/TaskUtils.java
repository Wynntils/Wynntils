/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

public final class TaskUtils {
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("wynntils-utilities-%d").build());

    public static Future<?> runAsync(Runnable r) {
        return EXECUTOR_SERVICE.submit(r);
    }
}
