/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public class MapButton extends BasicTexturedButton {
    public MapButton(Texture texture, Consumer<Integer> onClick, List<Component> tooltip) {
        super(0, 0, texture.width(), texture.height(), texture, onClick, tooltip);
    }
}
