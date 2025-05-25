/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.core.text.StyledText;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GambitItem extends GuiItem {
    private final String name;
    private final List<StyledText> lore;
    private final List<StyledText> description;
    private final int aspectPulls;
    private final int rewardPulls;

    public GambitItem(String name, List<StyledText> lore, int aspectPulls, int rewardPulls) {
        this.name = name;
        this.lore = new LinkedList<>(lore);
        this.description = extractDescriptionLines(lore);
        this.aspectPulls = aspectPulls;
        this.rewardPulls = rewardPulls;
    }

    public String getName() {
        return name;
    }

    public List<StyledText> getLore() {
        return Collections.unmodifiableList(lore);
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

    private List<StyledText> extractDescriptionLines(List<StyledText> lines) {
        int start = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getString().trim().isEmpty()) {
                start = i + 1; // description begins _after_ this
                break;
            }
        }

        if (start < 0 || start >= lines.size()) {
            return Collections.emptyList();
        }

        int end = lines.size();
        for (int i = start; i < lines.size(); i++) {
            if (lines.get(i).getString().contains("Rewards for Enabling")) {
                end = i - 1; // description ends _before_ this. -1 to exclude empty line before "Rewards for Enabling"
                break;
            }
        }

        if (start >= end) {
            return Collections.emptyList();
        }

        return lines.subList(start, end);
    }
}
