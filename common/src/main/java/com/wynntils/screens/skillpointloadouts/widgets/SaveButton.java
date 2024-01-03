/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.skillpointloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.skillpointloadouts.SkillPointLoadoutsScreen;
import com.wynntils.utils.type.Pair;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class SaveButton extends WynntilsButton {
    private final Component originalMessage;
    private final SkillPointLoadoutsScreen parent;
    private final Consumer<String> saveFunction;
    private boolean buttonConfirm = false;

    public SaveButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            SkillPointLoadoutsScreen parent,
            Consumer<String> saveFunction) {
        super(x, y, width, height, message);
        this.active = false;
        this.originalMessage = message;
        this.parent = parent;
        this.saveFunction = saveFunction;
    }

    @Override
    public void onPress() {
        String name = parent.saveNameInput.getTextBoxInput();
        if (Models.SkillPoint.hasLoadout(name) && !buttonConfirm) {
            parent.hasSaveNameConflict = true;
            buttonConfirm = true;
            this.setMessage(Component.translatable("screens.wynntils.skillPointLoadouts.confirm")
                    .withStyle(ChatFormatting.RED));
        } else {
            saveFunction.accept(name);
            parent.populateLoadouts();
            parent.setSelectedLoadout(
                    new Pair<>(name, Models.SkillPoint.getLoadouts().get(name)));
            parent.saveNameInput.setTextBoxInput("");
            // in case user pressed on both buttons
            parent.resetSaveButtons();
        }
    }

    public void reset() {
        buttonConfirm = false;
        this.setMessage(originalMessage);
    }
}
