/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.extension;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.utils.mc.type.StyledText;

public interface ItemStackExtension {
    ItemAnnotation getAnnotation();

    void setAnnotation(ItemAnnotation annotation);

    StyledText getOriginalName();

    void setOriginalName(StyledText name);
}
