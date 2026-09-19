/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

import java.util.Set;

public record SavableCharacterInfo(ClassType classType, boolean reskinned, Set<CharacterGamemode> gamemodes) {}
