/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession;

import com.wynntils.core.components.Model;
import com.wynntils.core.net.DownloadRegistry;
import java.util.List;

public class CraftingModel extends Model {
    private final RecipeInfoRegistry recipeInfoRegistry = new RecipeInfoRegistry();

    public CraftingModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        recipeInfoRegistry.registerDownloads(registry);
    }
}
