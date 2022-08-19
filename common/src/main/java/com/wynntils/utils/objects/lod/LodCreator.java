/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects.lod;

import com.wynntils.utils.objects.IBoundingBox;
import java.io.InputStream;

public interface LodCreator<T extends IBoundingBox> {
    T read(InputStream inputStream);
}
