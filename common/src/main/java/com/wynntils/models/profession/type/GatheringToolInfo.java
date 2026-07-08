/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.wynnitem.type.ItemMaterial;

public record GatheringToolInfo(
        String name,
        int level,
        String apiName,
        int tier,
        ItemMaterial material,
        ProfessionType professionType,
        int gatheringSpeed,
        int durability,
        GearTier gearTier,
        String emblem) {}
