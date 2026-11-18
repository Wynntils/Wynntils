/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.resourcepack.type;

import java.util.Map;
import java.util.Optional;

public record GroupInfo(String name, Map<String, VariantInfo> variants) {
    public Optional<String> matchingVariant(String textureHash, String fingerprint) {
        for (Map.Entry<String, VariantInfo> variant : variants.entrySet()) {
            if (variant.getValue().matches(textureHash, fingerprint)) {
                return Optional.of(variant.getKey());
            }
        }

        return Optional.empty();
    }
}
