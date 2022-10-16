/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.objects;

import java.util.Map;

public record MaterialMapping(int id, String name, Map<Integer, Variation> variations) {
    public static final MaterialMapping DEFAULT = new MaterialMapping(1, "stone", Map.of());

    public record Variation(int metadata, String name) {}
}
