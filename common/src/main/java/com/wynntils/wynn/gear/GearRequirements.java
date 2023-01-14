/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear;

import com.wynntils.utils.Pair;
import com.wynntils.wynn.objects.ClassType;
import com.wynntils.wynn.objects.Skill;
import java.util.List;
import java.util.Optional;

// FIXME: quest should be a type, not a string
public record GearRequirements(
        int level, Optional<ClassType> classType, List<Pair<Skill, Integer>> skills, Optional<String> quest) {}
