/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.resourcepack.type;

import java.util.Map;
import net.minecraft.resources.Identifier;

public record ModelCache(Map<Float, ModelInfo> modelIdToModelInfo, Map<Identifier, String> textureIdToTextureHash) {
    public static final ModelCache EMPTY = new ModelCache(Map.of(), Map.of());
}
