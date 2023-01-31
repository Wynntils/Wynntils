/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.parsing;

import com.wynntils.models.concepts.Powder;
import com.wynntils.models.stats.type.StatActualValue;
import java.util.List;

public record GearParseResult(com.wynntils.models.gearinfo.type.GearTier tier, List<StatActualValue> identifications, List<Powder> powders, int rerolls) {}
