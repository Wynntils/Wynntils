/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.resourcepack.type;

import java.util.Set;

public record VariantInfo(Set<String> textureHashes, Set<String> fingerprints) {
    public boolean matches(String textureHash, String fingerprint) {
        if (!textureHashes.contains(textureHash)) return false;
        if (fingerprints.isEmpty()) return true;
        return fingerprints.contains(fingerprint);
    }
}
