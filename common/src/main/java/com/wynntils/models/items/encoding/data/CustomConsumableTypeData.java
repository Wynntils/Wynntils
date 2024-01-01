/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.gear.type.ConsumableType;
import com.wynntils.models.items.encoding.type.ItemData;

public record CustomConsumableTypeData(ConsumableType consumableType) implements ItemData {}
