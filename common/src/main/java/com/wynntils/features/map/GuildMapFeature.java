/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.screens.maps.GuildMapScreen;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.type.PointerType;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.MAP)
public class GuildMapFeature extends Feature {
    @Persisted
    public final Config<PointerType> pointerType = new Config<>(PointerType.ARROW);

    @Persisted
    public final Config<CustomColor> pointerColor = new Config<>(new CustomColor(1f, 1f, 1f, 1f));

    @RegisterKeyBind
    public final KeyBind openGuildMapKeybind =
            new KeyBind("Open Guild Map", GLFW.GLFW_KEY_J, false, this::openGuildMap);

    public GuildMapFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.NEW_PLAYER, ConfigProfile.BLANK_SLATE)
                .build());
    }

    private void openGuildMap() {
        // If the current screen is already the map, and we get this event, this means we are holding the keybind
        // and should signal that we should close when the key is not held anymore.
        if (McUtils.screen() instanceof GuildMapScreen guildMapScreen) {
            guildMapScreen.setHoldingMapKey(true);
            return;
        }

        McUtils.setScreen(GuildMapScreen.create());
    }
}
