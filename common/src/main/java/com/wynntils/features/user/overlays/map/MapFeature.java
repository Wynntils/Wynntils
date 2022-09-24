/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays.map;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.maps.MainMapScreen;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(category = FeatureCategory.MAP)
public class MapFeature extends UserFeature {
    public static MapFeature INSTANCE;

    @Config
    public PointerType pointerType = PointerType.Arrow;

    @Config
    public CustomColor pointerColor = new CustomColor(1f, 1f, 1f, 1f);

    @RegisterKeyBind
    public final KeyBind openMapKeybind = new KeyBind("Open Full Screen Map", GLFW.GLFW_KEY_M, false, () -> {
        // If the current screen is already the map, and we get this event, this means we are holding the keybind
        // and should signal that we should close when the key is not held anymore.
        if (McUtils.mc().screen instanceof MainMapScreen mainMapScreen) {
            mainMapScreen.setHoldingMapKey(true);
            return;
        }

        McUtils.mc().setScreen(new MainMapScreen());
    });
}
