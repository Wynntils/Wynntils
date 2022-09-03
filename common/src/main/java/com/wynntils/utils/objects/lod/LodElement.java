/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects.lod;

import com.wynntils.utils.objects.IBoundingBox;
import java.util.UUID;

public record LodElement<T extends IBoundingBox>(T lodObject, UUID uuid, int lodLevel) {}
