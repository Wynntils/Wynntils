/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.elements.type.Element;
import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.utils.type.Pair;
import java.util.List;

public record DefenseData(int health, List<Pair<Element, Integer>> defences) implements ItemData {}
