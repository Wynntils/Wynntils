/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers.crafting;

import com.wynntils.models.containers.type.HighlightableProfessionProperty;
import com.wynntils.models.profession.type.ProfessionType;

import java.util.regex.Pattern;

public class ScribingContainer extends CraftingStationContainer implements HighlightableProfessionProperty {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFF8\uE053\uDAFF\uDF80\uF045");
    private static final ProfessionType PROFESSION_TYPE = ProfessionType.SCRIBING;

    public ScribingContainer() {
        super(TITLE_PATTERN, PROFESSION_TYPE);
    }
}
