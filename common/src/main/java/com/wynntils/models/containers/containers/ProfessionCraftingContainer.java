/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import com.wynntils.models.containers.type.HighlightableProfessionProperty;
import com.wynntils.models.profession.type.ProfessionType;
import java.util.Arrays;
import java.util.regex.Pattern;

public class ProfessionCraftingContainer extends Container implements HighlightableProfessionProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile(String.join(
            "|",
            Arrays.stream(ProfessionType.values())
                    .map(ProfessionType::getDisplayName)
                    .toList()));

    public ProfessionCraftingContainer() {
        super(TITLE_PATTERN);
    }

    @Override
    public ContainerBounds getBounds() {
        // Technically this includes more than just the ingredient slots,
        // but ContainerBounds can't select only a subset of slots.
        return new ContainerBounds(0, 0, 2, 4);
    }
}
