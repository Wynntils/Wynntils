/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.map;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.managers.Model;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.screens.maps.MainMapScreen;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.model.map.MapModel;
import com.wynntils.wynn.objects.HealthTexture;
import java.util.List;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(category = FeatureCategory.MAP)
public class MapFeature extends UserFeature {
    public static MapFeature INSTANCE;

    @Config
    public PointerType pointerType = PointerType.Arrow;

    @Config
    public CustomColor pointerColor = new CustomColor(1f, 1f, 1f, 1f);

    @Config
    public boolean renderUsingLinear = true;

    @Config
    public float playerPointerScale = 1.5f;

    @Config
    public float poiScale = 1f;

    @Config(subcategory = "Remote Players")
    public boolean renderRemoteFriendPlayers = true;

    @Config(subcategory = "Remote Players")
    public boolean renderRemotePartyPlayers = true;

    //    @Config(subcategory = "Remote Players")
    //    public boolean renderRemoteGuildPlayers = true;

    @Config(subcategory = "Remote Players")
    public HealthTexture remotePlayerHealthTexture = HealthTexture.a;

    @Config(subcategory = "Remote Players")
    public FontRenderer.TextShadow remotePlayerNameShadow = FontRenderer.TextShadow.OUTLINE;

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

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(MapModel.class);
    }
}
