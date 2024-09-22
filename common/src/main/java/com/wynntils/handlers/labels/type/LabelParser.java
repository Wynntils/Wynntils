/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels.type;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface LabelParser<T extends LabelInfo> {
    /**
     * Parses the label and returns the label info. If the label is not valid for this parser, returns null.
     * @return The label info, or null if the label is not valid for this parser.
     */
    T getInfo(StyledText label, Location location, Entity entity);
}
