/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects.lod;

import com.wynntils.utils.objects.IBoundingBox;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LodManager<T extends IBoundingBox> {
    private final File lodDirectory;
    private final Map<UUID, T> cachedLods = new HashMap<>();
    private final LodCreator<T> lodCreator;

    public LodManager(File lodDirectory, LodCreator<T> lodCreator) {
        this.lodDirectory = lodDirectory;
        this.lodCreator = lodCreator;
    }

    /**
     * Finds an existing LOD element.
     * <p>First the loaded elements are checked. Next it tries to load from disk. If all fails, {@code null} is returned.</p>
     * @param uuid of the LOD element.
     * @return The LOD element or {@code  null}.
     */
    public T getLodByUuid(UUID uuid) {
        final T cached = cachedLods.get(uuid);
        if (cached != null) {
            return cached;
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
}
