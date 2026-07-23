/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.atlas;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import java.util.List;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.resources.Identifier;

public final class WynntilsAtlasManager extends Manager {
    public static final List<AtlasManager.AtlasConfig> ATLASES = List.of(
            new AtlasManager.AtlasConfig(
                    Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "textures/atlas/ui_components.png"),
                    Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "ui_components"),
                    false),
            new AtlasManager.AtlasConfig(
                    Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "textures/atlas/aspect.png"),
                    Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "aspect"),
                    false));

    public WynntilsAtlasManager() {
        super(List.of());
    }
}
