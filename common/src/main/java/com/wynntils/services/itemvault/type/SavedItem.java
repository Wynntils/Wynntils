/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemvault.type;

import java.util.Set;

public record SavedItem(String base64, Set<String> categories, ItemStackInfo itemStackInfo)
        implements Comparable<SavedItem> {
    @Override
    public int compareTo(SavedItem other) {
        return this.base64.compareTo(other.base64);
    }

    public record ItemStackInfo(int itemID, int damage, int hideFlags, boolean unbreakable, int color) {}
}
