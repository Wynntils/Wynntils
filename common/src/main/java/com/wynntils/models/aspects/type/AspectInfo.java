/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.aspects.type;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.wynnitem.type.ItemMaterial;
import com.wynntils.utils.type.Pair;
import java.util.List;

public record AspectInfo(
        String name,
        GearTier gearTier,
        ClassType classType,
        List<Pair<Integer, List<StyledText>>> effects,
        ItemMaterial itemMaterial) {}
