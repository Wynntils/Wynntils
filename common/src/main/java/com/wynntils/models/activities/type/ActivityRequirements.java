/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.type.Pair;
import java.util.List;

/** A collection of pairs of the format "requirement type, fulfillment" where "fulfillment" is a boolean
 * indicating wether the player fulfils the requirement or not */
public record ActivityRequirements(
        Pair<Integer, Boolean> level,
        List<Pair<Pair<ProfessionType, Integer>, Boolean>> professionLevels,
        List<Pair<String, Boolean>> quests) {}
