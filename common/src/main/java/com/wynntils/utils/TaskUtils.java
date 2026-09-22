/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class TaskUtils {
    private static final Set<ExecutorService> EXECUTORS = ConcurrentHashMap.newKeySet();

    private static final ScheduledExecutorService EXECUTOR_SERVICE =
            createSingleThreadScheduledExecutor("wynntils-utilities-%d");

    private TaskUtils() {}

    public static ThreadFactory daemonThreadFactory(String nameFormat) {
        return new ThreadFactoryBuilder()
                .setNameFormat(nameFormat)
                .setDaemon(true)
                .build();
    }

    public static ExecutorService createSingleThreadExecutor(String nameFormat) {
        ExecutorService executor = Executors.newSingleThreadExecutor(daemonThreadFactory(nameFormat));

        EXECUTORS.add(executor);

        return executor;
    }

    public static ScheduledExecutorService createSingleThreadScheduledExecutor(String nameFormat) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory(nameFormat));

        EXECUTORS.add(executor);

        return executor;
    }

    public static ScheduledExecutorService createScheduledThreadPool(int threads, String nameFormat) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(threads, daemonThreadFactory(nameFormat));

        EXECUTORS.add(executor);

        return executor;
    }

    public static Future<?> runAsync(Runnable r) {
        return EXECUTOR_SERVICE.submit(r);
    }

    public static Future<?> schedule(Runnable r, int delay) {
        return EXECUTOR_SERVICE.schedule(r, delay, TimeUnit.SECONDS);
    }

    public static Future<?> schedule(Runnable r, int delay, TimeUnit unit) {
        return EXECUTOR_SERVICE.schedule(r, delay, unit);
    }

    public static void close() {
        for (ExecutorService executor : EXECUTORS) {
            executor.shutdownNow();
        }

        EXECUTORS.clear();
    }
}
