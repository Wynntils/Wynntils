/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import java.util.regex.Pattern;

public record NameData(String name) implements ItemData {
    private static final int MAX_NAME_LENGTH = 48;
    private static final Pattern NAME_SANITIZER_PATTERN = Pattern.compile("[^a-zA-Z0-9 ]");

    public static NameData from(String itemName) {
        // Sanitize the name
        itemName = itemName.substring(0, Math.min(itemName.length(), MAX_NAME_LENGTH));
        itemName = NAME_SANITIZER_PATTERN.matcher(itemName).replaceAll("");
        itemName = itemName.trim().replaceAll("\\s{2,}", " ");

        return new NameData(itemName);
    }

    public static NameData from(IdentifiableItemProperty property) {
        String itemName = property.getName();
        return from(itemName);
    }
}
