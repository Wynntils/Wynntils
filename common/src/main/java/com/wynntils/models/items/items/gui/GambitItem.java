/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.core.text.StyledText;
import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GambitItem extends GuiItem {
    private final String name;
    private final Color color;
    private final List<StyledText> description;
    private final int aspectPulls;
    private final int rewardPulls;

    public GambitItem(String name, Color color, List<StyledText> description, int aspectPulls, int rewardPulls) {
        this.name = name;
        this.color = color;
        this.description = new LinkedList<>(description);
        this.aspectPulls = aspectPulls;
        this.rewardPulls = rewardPulls;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public List<StyledText> getDescription() {
        return Collections.unmodifiableList(description);
    }

    public int getAspectPulls() {
        return aspectPulls;
    }

    public int getRewardPulls() {
        return rewardPulls;
    }
}
