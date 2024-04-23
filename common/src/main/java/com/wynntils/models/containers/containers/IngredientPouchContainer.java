/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.HighlightableProfessionProperty;
import java.util.regex.Pattern;

public class IngredientPouchContainer extends Container implements HighlightableProfessionProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile(".+'s? Pouch");

    public IngredientPouchContainer() {
        super(TITLE_PATTERN);
    }

    @Override
    public ContainerBounds getBounds() {
        return new ContainerBounds(0, 0, 2, 8);
    }
}
