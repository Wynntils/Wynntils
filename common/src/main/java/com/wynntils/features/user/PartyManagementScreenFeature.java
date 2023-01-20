package com.wynntils.features.user;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.gui.screens.PartyManagementScreen;
import com.wynntils.mc.utils.McUtils;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@FeatureInfo(stability = FeatureInfo.Stability.STABLE)
public class PartyManagementScreenFeature extends UserFeature {
    @RegisterKeyBind
    private final KeyBind openPartyManagementScreen = new KeyBind("Open Party Management Screen", GLFW.GLFW_KEY_RIGHT_BRACKET, true, () -> McUtils.mc().setScreen(PartyManagementScreen.create()));

    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.PlayerRelations);
    }
}
