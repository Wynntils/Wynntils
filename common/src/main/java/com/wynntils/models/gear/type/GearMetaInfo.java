/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import java.util.Optional;

// The api name is normally the same as the name, but if not, the api name is given
// by apiName
public record GearMetaInfo(
        GearRestrictions restrictions,
        GearMaterial material,
        GearDropType dropType,
        Optional<String> lore,
        Optional<String> apiName,
        boolean allowCraftsman) {}
