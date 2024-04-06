/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.stats.type.StatPossibleValues;
import java.util.List;

public record CustomIdentificationsData(List<StatPossibleValues> possibleValues) implements ItemData {}
