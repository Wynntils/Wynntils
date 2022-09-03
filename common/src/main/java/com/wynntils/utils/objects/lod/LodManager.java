/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects.lod;

import com.wynntils.utils.objects.IBoundingBox;
import com.wynntils.utils.objects.minQueue.FibonacciHeapMinQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LodManager<T extends IBoundingBox> {
    private final File lodDirectory;
    private final Map<UUID, T> cachedLods = new HashMap<>();
    private final LodCreator<T> lodCreator;

    private final CleanupWorker cleanupWorker;

    /**
     * Sets up an empty manager from a backup directory and lod creator.
     * @param lodDirectory may be {@code null} to not save created LODs.
     * @param lodCreator used to generate LOD levels.
     */
    public LodManager(@Nullable File lodDirectory, @Nonnull LodCreator<T> lodCreator) {
        this.lodDirectory = lodDirectory;
        this.lodCreator = lodCreator;
        cleanupWorker = new CleanupWorker(this);
        cleanupWorker.setDaemon(true);
        cleanupWorker.start();
    }

    public boolean registerLodObject(LodElement<T> lodElement) {
        if (this.cachedLods.containsKey(lodElement.uuid())) {
            return false;
        }
        cachedLods.put(lodElement.uuid(), lodElement.lodObject());
        if (lodDirectory == null) {
            return true;
        }
        try (FileOutputStream fos =
                new FileOutputStream(new File(lodDirectory, lodElement.uuid().toString()))) {
            this.lodCreator.write(fos, lodElement.lodObject());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Finds an existing LOD element.
     * <p>First the loaded elements are checked. Next it tries to load from disk. If all fails, {@code null} is returned.</p>
     * @param uuid of the LOD element.
     * @return The LOD element or {@code  null}.
     */
    public T getLodByUuid(UUID uuid) {
        cleanupWorker.update(uuid);
        final T cached = cachedLods.get(uuid);
        if (cached != null) {
            return cached;
        }
        if (lodDirectory == null) {
            return null;
        }
        File lodFile = new File(lodDirectory, uuid.toString());
        try {
            InputStream inputStream = new FileInputStream(lodFile);
            return lodCreator.read(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LodCreator<T> lodCreator() {
        return this.lodCreator;
    }

    public LodManager<T> newInstance() {
        return new LodManager<>(this.lodDirectory, this.lodCreator);
    }

    private static class CleanupWorker extends Thread {
        /**
         * Clean up after 10 minutes.
         */
        private static final long CLEANUP_TIMEOUT = 1000 * 60 * 10;

        private final Queue<LastAccessRecord> cleanupQueue =
                new FibonacciHeapMinQueue<>(Comparator.comparingLong(LastAccessRecord::accessTime));
        private final LodManager<?> lodManager;

        public CleanupWorker(LodManager<?> lodManager) {
            this.lodManager = lodManager;
        }

        public void update(UUID uuid) {
            synchronized (this.cleanupQueue) {
                this.cleanupQueue.removeIf(e -> e.object.equals(uuid));
                this.cleanupQueue.add(new LastAccessRecord(System.currentTimeMillis(), uuid));
                this.cleanupQueue.notifyAll();
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    final LastAccessRecord lar;
                    synchronized (this.cleanupQueue) {
                        lar = this.cleanupQueue.peek();
                        if (lar == null) {
                            this.cleanupQueue.wait();
                        }
                    }
                    if (lar != null) {
                        Thread.sleep(Math.max(lar.accessTime + CLEANUP_TIMEOUT - System.currentTimeMillis(), 0));
                    }
                } catch (InterruptedException e) {
                    // ignore - is handled by loop control
                }
                synchronized (this.cleanupQueue) {
                    if (this.cleanupQueue.isEmpty()
                            || this.cleanupQueue.peek().accessTime + CLEANUP_TIMEOUT > System.currentTimeMillis()) {
                        // thread woke up ahead of time - go back to sleep
                        continue;
                    }
                    final LastAccessRecord lar = this.cleanupQueue.poll();
                    this.lodManager.cachedLods.remove(lar.object);
                }
            }
        }
    }

    private record LastAccessRecord(Long accessTime, UUID object) {
        @Override
        public boolean equals(Object o) {
            if (o instanceof LastAccessRecord) {
                return object.equals(((LastAccessRecord) o).object);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return object.hashCode();
        }
    }
}
