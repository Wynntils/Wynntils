/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.type;

import java.util.List;

public record MaterialConversionInfo(int id, String name, List<VariationInfo> variations) {
    public record VariationInfo(int metadata, String name) {}
}
