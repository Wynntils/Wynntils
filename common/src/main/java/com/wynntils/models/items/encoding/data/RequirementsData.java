/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.items.encoding.type.ItemData;

public record RequirementsData(GearRequirements requirements) implements ItemData {}
