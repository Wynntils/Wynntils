/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.screens.maps.GuildMapScreen;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.type.PointerType;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.MAP)
public class GuildMapFeature extends Feature {
    public static GuildMapFeature INSTANCE;

    @Config
    public boolean renderUsingLinear = true;

    @Config
    public PointerType pointerType = PointerType.Arrow;

    @Config
    public CustomColor pointerColor = new CustomColor(1f, 1f, 1f, 1f);

    @RegisterKeyBind
    public final KeyBind openGuildMapKeybind =
            new KeyBind("Open Guild Map", GLFW.GLFW_KEY_J, false, this::openGuildMap);

    private void openGuildMap() {
        // If the current screen is already the map, and we get this event, this means we are holding the keybind
        // and should signal that we should close when the key is not held anymore.
        if (McUtils.mc().screen instanceof GuildMapScreen guildMapScreen) {
            guildMapScreen.setHoldingMapKey(true);
            return;
        }

        McUtils.mc().setScreen(GuildMapScreen.create());
    }
}
