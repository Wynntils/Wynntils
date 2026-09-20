/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.aspects.type;

import java.util.List;
import java.util.Map;

public record AspectDump(Map<String, Integer> ownedAspects, List<String> equippedAspects) {}
