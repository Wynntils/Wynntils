/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import java.util.Optional;
import java.util.regex.Pattern;

public record NameData(Optional<String> name) implements ItemData {
    private static final NameData EMPTY = new NameData(Optional.empty());

    private static final int MAX_NAME_LENGTH = 48;
    private static final Pattern SANITIZE_PATTERN = Pattern.compile("[^a-zA-Z0-9'\\-.,!?\\s]");

    /**
     * Creates a new {@link NameData} object with the given name.
     * The name is obtained from the given {@link IdentifiableItemProperty}, and we assume it is safe.
     *
     * @param property the property to get the name from
     * @return a new {@link NameData} object with the given name
     */
    public static NameData fromSafeName(IdentifiableItemProperty property) {
        return new NameData(Optional.ofNullable(property.getName()));
    }

    /**
     * Sanitizes the given name.
     * If any characters are not alphanumeric, an apostrophe, or a space, the name is sanitized, and is not displayed.
     * The name is trimmed, and any consecutive spaces are replaced with a single space.
     * @param name the name to sanitize
     * @return an {@link Optional} containing the sanitized name, or an empty {@link Optional} if the name is empty
     */
    public static NameData sanitized(String name) {
        if (SANITIZE_PATTERN.matcher(name).find()) {
            return EMPTY;
        }

        name = name.trim();
        name = name.replaceAll("\\s+", " ");
        name = name.substring(0, Math.min(name.length(), MAX_NAME_LENGTH));

        return new NameData(Optional.of(name));
    }
}
