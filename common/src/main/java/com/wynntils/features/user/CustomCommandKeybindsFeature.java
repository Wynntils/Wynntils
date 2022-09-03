/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.mc.utils.McUtils;
import org.lwjgl.glfw.GLFW;

public class CustomCommandKeybindsFeature extends UserFeature {
    @Config
    private String keybindCommand1 = "";

    @Config
    private String keybindCommand2 = "";

    @Config
    private String keybindCommand3 = "";

    @Config
    private String keybindCommand4 = "";

    @Config
    private String keybindCommand5 = "";

    @Config
    private String keybindCommand6 = "";

    @RegisterKeyBind
    private final KeyBind executeKeybind1 = new KeyBind(
            "Execute 1st Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand1));

    @RegisterKeyBind
    private final KeyBind executeKeybind2 = new KeyBind(
            "Execute 2nd Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand2));

    @RegisterKeyBind
    private final KeyBind executeKeybind3 = new KeyBind(
            "Execute 3rd Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand3));

    @RegisterKeyBind
    private final KeyBind executeKeybind4 = new KeyBind(
            "Execute 4th Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand4));

    @RegisterKeyBind
    private final KeyBind executeKeybind5 = new KeyBind(
            "Execute 5th Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand5));

    @RegisterKeyBind
    private final KeyBind executeKeybind6 = new KeyBind(
            "Execute 6th Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand6));

    private void executeKeybind(String keybindCommand) {
        keybindCommand = keybindCommand.trim();

        if (!keybindCommand.startsWith("/")) {
            keybindCommand = "/" + keybindCommand;
        }

        McUtils.player().chat(keybindCommand);
    }
}
