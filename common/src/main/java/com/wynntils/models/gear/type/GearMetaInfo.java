/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.wynnitem.type.ItemMaterial;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import java.util.List;
import java.util.Optional;

// The api name is normally the same as the name, but if not, the api name is given
// by apiName
public record GearMetaInfo(
        GearDropRestrictions dropRestrictions,
        GearRestrictions restrictions,
        ItemMaterial material,
        List<ItemObtainInfo> obtainInfo,
        Optional<StyledText> lore,
        Optional<String> apiName,
        boolean allowCraftsman,
        boolean preIdentified) {}
