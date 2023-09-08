/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Optional;

public record GearRequirements(
        int level, Optional<ClassType> classType, List<Pair<Skill, Integer>> skills, Optional<String> quest) {}
