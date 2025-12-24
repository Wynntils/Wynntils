/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.commands;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.utils.mc.McUtils;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.COMMANDS)
public class CustomCommandKeybindsFeature extends Feature {
    @Persisted
    private final Config<String> keybindCommand1 = new Config<>("");

    @Persisted
    private final Config<CommandType> commandType1 = new Config<>(CommandType.EXECUTE);

    @Persisted
    private final Config<String> keybindCommand2 = new Config<>("");

    @Persisted
    private final Config<CommandType> commandType2 = new Config<>(CommandType.EXECUTE);

    @Persisted
    private final Config<String> keybindCommand3 = new Config<>("");

    @Persisted
    private final Config<CommandType> commandType3 = new Config<>(CommandType.EXECUTE);

    @Persisted
    private final Config<String> keybindCommand4 = new Config<>("");

    @Persisted
    private final Config<CommandType> commandType4 = new Config<>(CommandType.EXECUTE);

    @Persisted
    private final Config<String> keybindCommand5 = new Config<>("");

    @Persisted
    private final Config<CommandType> commandType5 = new Config<>(CommandType.EXECUTE);

    @Persisted
    private final Config<String> keybindCommand6 = new Config<>("");

    @Persisted
    private final Config<CommandType> commandType6 = new Config<>(CommandType.EXECUTE);

    @RegisterKeyBind
    private final KeyBind executeKeybind1 = new KeyBind(
            "Execute 1st Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand1.get(), commandType1.get()));

    @RegisterKeyBind
    private final KeyBind executeKeybind2 = new KeyBind(
            "Execute 2nd Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand2.get(), commandType2.get()));

    @RegisterKeyBind
    private final KeyBind executeKeybind3 = new KeyBind(
            "Execute 3rd Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand3.get(), commandType3.get()));

    @RegisterKeyBind
    private final KeyBind executeKeybind4 = new KeyBind(
            "Execute 4th Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand4.get(), commandType4.get()));

    @RegisterKeyBind
    private final KeyBind executeKeybind5 = new KeyBind(
            "Execute 5th Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand5.get(), commandType5.get()));

    @RegisterKeyBind
    private final KeyBind executeKeybind6 = new KeyBind(
            "Execute 6th Custom Command Keybind",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> this.executeKeybind(keybindCommand6.get(), commandType6.get()));

    public CustomCommandKeybindsFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.NEW_PLAYER, ConfigProfile.BLANK_SLATE)
                .build());
    }

    private void executeKeybind(String keybindCommand, CommandType commandType) {
        switch (commandType) {
            case EXECUTE -> Handlers.Command.sendCommandImmediately(keybindCommand);
            case SUGGEST -> McUtils.openChatScreen(keybindCommand);
        }
    }

    public enum CommandType {
        EXECUTE,
        SUGGEST
    }
}
