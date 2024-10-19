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
    private static final Pattern SANITIZE_PATTERN = Pattern.compile("[^a-zA-Z0-9'\\s]");

    public static NameData from(IdentifiableItemProperty property) {
        return NameData.from(property.getName());
    }

    public static NameData from(String name) {
        name = SANITIZE_PATTERN.matcher(name).replaceAll("");
        name = name.trim();
        name = name.replaceAll("\\s+", " ");
        name = name.substring(0, Math.min(name.length(), MAX_NAME_LENGTH));

        return new NameData(name);
    }
}
