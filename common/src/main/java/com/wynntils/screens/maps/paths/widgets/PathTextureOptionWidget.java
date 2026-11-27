/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.paths.widgets;

import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.screens.maps.managers.widgets.categorymanager.options.IconOptionWidget;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import net.minecraft.network.chat.Component;

public final class PathTextureOptionWidget extends IconOptionWidget {
    public PathTextureOptionWidget() {
        super(
                Component.translatable("screens.wynntils.map.paths.texture"),
                Component.translatable("screens.wynntils.map.paths.texture.description"),
                OptionCategory.PATH,
                MapAttributes::getPathIconId);
    }

    @Override
    protected Component getNoneText() {
        return Component.translatable("screens.wynntils.map.paths.lineTexture");
    }
}
