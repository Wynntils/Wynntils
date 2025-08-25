/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import com.wynntils.services.itemfilter.type.ItemProviderType;
import java.util.List;

/**
 * Represents a container that can be searched. These containers are scrollable,
 * and have a defined bounds where the content can appear.
 */
public interface SearchableContainerProperty extends ScrollableContainerProperty, BoundedContainerProperty {
    /**
     * Returns the supported provider types for this container.
     * If basic search should be used, return an empty list.
     * @return The supported provider types or an empty list if basic search should be used.
     */
    List<ItemProviderType> supportedProviderTypes();

    default boolean supportsAdvancedSearch() {
        return !supportedProviderTypes().isEmpty();
    }

    /**
     * Some container backgrounds render above the renderY of the container so we need to offset the search bar.
     * @return A positive integer for how much to offset the search bar by.
     */
    default int renderYOffset() {
        return 0;
    }
}
