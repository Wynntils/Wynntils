package com.wynntils.core.consumers.atlas;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class AtlasManager extends Manager {
    public static final List<net.minecraft.client.resources.model.AtlasManager.AtlasConfig> ATLASES = List.of(
        new net.minecraft.client.resources.model.AtlasManager.AtlasConfig(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "textures/atlas/ui_components.png"), Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "ui_components"), false)
    );

    public AtlasManager() {
        super(List.of());
    }

}
