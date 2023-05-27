/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.screens.base.WynntilsMenuScreenBase;
import com.wynntils.screens.guides.WynntilsGuidesListScreen;
import com.wynntils.screens.guides.emeraldpouch.WynntilsEmeraldPouchGuideScreen;
import com.wynntils.screens.guides.gear.WynntilsItemGuideScreen;
import com.wynntils.screens.guides.ingredient.WynntilsIngredientGuideScreen;
import com.wynntils.screens.guides.powder.WynntilsPowderGuideScreen;
import com.wynntils.utils.mc.KeyboardUtils;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.INVENTORY)
public class ItemGuideListHotkeyFeature extends Feature {

    @RegisterKeyBind
    private final KeyBind itemGuideListKeyBind =
            new KeyBind("Open Item Guide List", GLFW.GLFW_KEY_UNKNOWN, true, this::onItemGuideListKeyPress);

    @RegisterConfig
    public final Config<Boolean> emeraldPouchGuide = new Config<>(false);

    @RegisterConfig
    public final Config<Boolean> powderGuide = new Config<>(false);

    @RegisterConfig
    public final Config<Boolean> ingredientGuide = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> itemGuide = new Config<>(true);

    private void onItemGuideListKeyPress() {
        int key = -1;
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_ALT)) {
            key = 1;
        } else if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            key = 2;
        } else if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
            key = 3;
        } else if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_ALT)) {
            key = 4;
        }

        switch (key) {
            case 1 -> {
                if (powderGuide.get()) {
                    WynntilsMenuScreenBase.openBook(WynntilsPowderGuideScreen.create());
                }
            }
            case 2 -> {
                if (itemGuide.get()) {
                    WynntilsMenuScreenBase.openBook(WynntilsItemGuideScreen.create());
                }
            }
            case 3 -> {
                if (ingredientGuide.get()) {
                    WynntilsMenuScreenBase.openBook(WynntilsIngredientGuideScreen.create());
                }
            }
            case 4 -> {
                if (emeraldPouchGuide.get()) {
                    WynntilsMenuScreenBase.openBook(WynntilsEmeraldPouchGuideScreen.create());
                }
            }
            default -> WynntilsMenuScreenBase.openBook(WynntilsGuidesListScreen.create());
        }
    }
}
