/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CustomColor;
import java.util.Collections;
import java.util.List;

public class GambitItem extends GuiItem {
    private final String name;
    private final CustomColor color;
    private final List<StyledText> description;

    public GambitItem(String name, CustomColor color, List<StyledText> description) {
        this.name = name;
        this.color = color;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public CustomColor getColor() {
        return color;
    }

    public List<StyledText> getDescription() {
        return Collections.unmodifiableList(description);
    }
}
