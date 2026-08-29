/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

import java.util.List;

public record MountColorInfo(int id, String key, String displayName, List<MountColorType> mounts) {
    public static final MountColorInfo UNKNOWN = new MountColorInfo(0, "unknown", "Unknown", List.of());
}
