/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.utils.type.Pair;
import java.util.List;

public record PowderData(List<Pair<Powder, Integer>> powders) implements ItemData {}
