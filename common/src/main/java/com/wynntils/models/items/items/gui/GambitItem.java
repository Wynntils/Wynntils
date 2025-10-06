/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.gambits.type.Gambit;
import com.wynntils.models.gambits.type.GambitStatus;
import com.wynntils.utils.colors.CustomColor;
import java.util.Collections;
import java.util.List;

public class GambitItem extends GuiItem {
    private final Gambit gambit;
    private final String name;
    private final CustomColor color;
    private final List<StyledText> description;
    private final GambitStatus gambitStatus;

    public GambitItem(
            Gambit gambit, String name, CustomColor color, List<StyledText> description, GambitStatus gambitStatus) {
        this.gambit = gambit;
        // name left here, in case we have an unknown gambit
        this.name = name;
        this.color = color;
        this.description = description;
        this.gambitStatus = gambitStatus;
    }

    public String getName() {
        return name;
    }

    public Gambit getGambit() {
        return gambit;
    }

    public GambitStatus getGambitStatus() {
        return gambitStatus;
    }

    public CustomColor getColor() {
        return color;
    }

    public List<StyledText> getDescription() {
        return Collections.unmodifiableList(description);
    }
}
