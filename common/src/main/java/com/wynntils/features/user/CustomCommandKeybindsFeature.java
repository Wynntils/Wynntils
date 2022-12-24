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
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;

public class CustomCommandKeybindsFeature extends UserFeature {
    @Config
    private String keybindCommand1 = "";

    @Config
    private CommandType commandType1 = CommandType.EXECUTE;

    @Config
    private String keybindCommand2 = "";

    @Config
    private CommandType commandType2 = CommandType.EXECUTE;

    @Config
    private String keybindCommand3 = "";

    @Config
    private CommandType commandType3 = CommandType.EXECUTE;

    @Config
    private String keybindCommand4 = "";

    @Config
    private CommandType commandType4 = CommandType.EXECUTE;

    @Config
    private String keybindCommand5 = "";

    @Config
    private CommandType commandType5 = CommandType.EXECUTE;

    @Config
    private String keybindCommand6 = "";

    @Config
    private CommandType commandType6 = CommandType.EXECUTE;

    @RegisterKeyBind
    private final KeyBind executeKeybind1 = new KeyBind(
            "Execute 1st Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand1, commandType1));

    @RegisterKeyBind
    private final KeyBind executeKeybind2 = new KeyBind(
            "Execute 2nd Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand2, commandType2));

    @RegisterKeyBind
    private final KeyBind executeKeybind3 = new KeyBind(
            "Execute 3rd Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand3, commandType3));

    @RegisterKeyBind
    private final KeyBind executeKeybind4 = new KeyBind(
            "Execute 4th Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand4, commandType4));

    @RegisterKeyBind
    private final KeyBind executeKeybind5 = new KeyBind(
            "Execute 5th Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand5, commandType5));

    @RegisterKeyBind
    private final KeyBind executeKeybind6 = new KeyBind(
            "Execute 6th Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand6, commandType6));

    private void executeKeybind(String keybindCommand, CommandType commandType) {
        switch (commandType) {
            case EXECUTE -> McUtils.sendCommand(keybindCommand);
            case SUGGEST -> McUtils.mc().setScreen(new ChatScreen(keybindCommand));
        }
    }

    public enum CommandType {
        EXECUTE,
        SUGGEST
    }
}
