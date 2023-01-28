/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.type;

import java.util.Optional;

public record GearMetaInfo(
        GearRestrictions restrictions,
        GearMaterial material,
        GearDropType dropType,
        Optional<String> lore,
        Optional<String> altName,
        boolean allowCraftsman) {}
