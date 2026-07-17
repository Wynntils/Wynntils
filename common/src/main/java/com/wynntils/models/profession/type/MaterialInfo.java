/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.wynnitem.type.ItemMaterial;
import java.util.Map;

public record MaterialInfo(
        String name,
        int level,
        String apiName,
        Map<Integer, Integer> chances,
        ItemMaterial material,
        ProfessionType professionType,
        StyledText lore,
        String emblem) {}
