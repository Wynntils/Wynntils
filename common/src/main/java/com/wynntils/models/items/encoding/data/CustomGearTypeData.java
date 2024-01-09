/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.encoding.type.ItemData;

public record CustomGearTypeData(GearType gearType) implements ItemData {}
