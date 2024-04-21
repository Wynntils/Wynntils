/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

/**
 * Represents a container that can be searched. These containers are scrollable,
 * and have a defined bounds where the content can appear.
 */
public interface SearchableContainerProperty extends ScrollableContainerProperty {
    ContainerBounds getBounds();

    boolean supportsAdvancedSearch();
}
