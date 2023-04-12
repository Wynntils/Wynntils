/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import java.util.Map;

/**
 * This class represents the current ability tree, where all nodes have a state.
 */
public record ParsedAbilityTree(Map<AbilityTreeSkillNode, AbilityTreeNodeState> nodes) {}
