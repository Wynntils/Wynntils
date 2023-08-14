/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.google.common.collect.ImmutableMap;

/**
 * This class represents the current ability tree, where all nodes have a state.
 */
public record ParsedAbilityTree(ImmutableMap<AbilityTreeSkillNode, AbilityTreeNodeState> nodes) {}
