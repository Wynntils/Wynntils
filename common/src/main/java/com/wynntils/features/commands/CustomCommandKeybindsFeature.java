/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.commands;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigInfo;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.COMMANDS)
public class CustomCommandKeybindsFeature extends UserFeature {
    @ConfigInfo
    private Config<String> keybindCommand1 = new Config<>("");

    @ConfigInfo
    private Config<CommandType> commandType1 = new Config<>(CommandType.EXECUTE);

    @ConfigInfo
    private Config<String> keybindCommand2 = new Config<>("");

    @ConfigInfo
    private Config<CommandType> commandType2 = new Config<>(CommandType.EXECUTE);

    @ConfigInfo
    private Config<String> keybindCommand3 = new Config<>("");

    @ConfigInfo
    private Config<CommandType> commandType3 = new Config<>(CommandType.EXECUTE);

    @ConfigInfo
    private Config<String> keybindCommand4 = new Config<>("");

    @ConfigInfo
    private Config<CommandType> commandType4 = new Config<>(CommandType.EXECUTE);

    @ConfigInfo
    private Config<String> keybindCommand5 = new Config<>("");

    @ConfigInfo
    private Config<CommandType> commandType5 = new Config<>(CommandType.EXECUTE);

    @ConfigInfo
    private Config<String> keybindCommand6 = new Config<>("");

    @ConfigInfo
    private Config<CommandType> commandType6 = new Config<>(CommandType.EXECUTE);

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
