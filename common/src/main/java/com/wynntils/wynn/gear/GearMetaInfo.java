/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear;

import com.wynntils.models.gear.type.GearDropType;
import com.wynntils.wynn.gear.types.GearMaterial;
import com.wynntils.wynn.gear.types.GearRestrictions;
import java.util.Optional;

public record GearMetaInfo(
        GearRestrictions restrictions,
        GearMaterial material,
        GearDropType dropType,
        Optional<String> lore,
        Optional<String> altName,
        boolean allowCraftsman) {}
