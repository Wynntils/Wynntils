/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import java.util.List;

public record ArchetypeInfo(
        String name, String formattedName, List<String> description, ItemInformation itemInformation) {}
