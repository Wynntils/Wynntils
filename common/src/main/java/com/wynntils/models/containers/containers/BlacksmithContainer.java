/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.type.ContainerBounds;
import java.util.regex.Pattern;

public class BlacksmithContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFF8\uE016");

    public BlacksmithContainer() {
        super(TITLE_PATTERN);
    }

    public ContainerBounds getBounds() {
        return new ContainerBounds(1, 2, 2, 6);
    }
}
