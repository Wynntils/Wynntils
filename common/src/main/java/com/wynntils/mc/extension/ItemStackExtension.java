/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.extension;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;

public interface ItemStackExtension {
    ItemAnnotation getAnnotation();

    void setAnnotation(ItemAnnotation annotation);

    StyledText getOriginalName();

    void setOriginalName(StyledText name);
}
