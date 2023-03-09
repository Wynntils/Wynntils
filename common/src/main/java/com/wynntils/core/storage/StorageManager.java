/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.storage;

import com.wynntils.core.components.Manager;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.reflect.FieldUtils;

public final class StorageManager extends Manager {
    private static final long SAVE_INTERVAL = 10_000;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final Map<String, Storage> storages = new HashMap<>();

    private long lastPersisted;
    private boolean scheduledPersist;

    public StorageManager() {
        super(List.of());
    }

    public void registerStorageable(Storageable storageable) {
        String baseName = storageable.getStorageJsonName();

        List<Field> fields = FieldUtils.getAllFieldsList(storageable.getClass());
        List<Field> storageFields =
                fields.stream().filter(f -> f.getType().equals(Storage.class)).toList();

        for (Field storageField : storageFields) {
            try {
                Storage storage = (Storage) FieldUtils.readField(storageField, storageable, true);
                String jsonName = baseName + "." + storageField.getName();
                storages.put(jsonName, storage);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void restorePersisted() {
        readFromJson();
    }

    void persist(Storage<?> storage) {
        String name = storages.entrySet().stream()
                .filter(e -> e.getValue().equals(storage))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");
        System.out.println("Persisting: " + name + ", value: " + storage.get());

        if (scheduledPersist) return;

        long now = System.currentTimeMillis();
        long delay = Math.max((lastPersisted + SAVE_INTERVAL) - now, 0);

        executor.schedule(
                () -> {
                    scheduledPersist = false;
                    lastPersisted = System.currentTimeMillis();
                    writeToJson();
                },
                delay,
                TimeUnit.MILLISECONDS);
        scheduledPersist = true;
    }

    private void readFromJson() {
        storages.forEach((key, value) -> System.out.println("Restoring value for: " + key + ", value: " + value.get()));
    }

    private void writeToJson() {
        storages.forEach(
                (key, value) -> System.out.println("Persisting value for: " + key + ", value: " + value.get()));
    }
}
