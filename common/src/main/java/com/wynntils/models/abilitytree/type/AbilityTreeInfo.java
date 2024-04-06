/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import java.util.List;

/**
 * This class contains all relevant info to a specific class' ability tree.
 */
public record AbilityTreeInfo(List<AbilityTreeSkillNode> nodes) {}
