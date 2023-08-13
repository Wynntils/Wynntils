/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.partymanagement.widgets;

import com.wynntils.screens.base.widgets.WynntilsButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class LegendButton extends WynntilsButton {
    public LegendButton(int x, int y) {
        super(x, y, 20, 20, Component.literal("?"));
        this.setTooltip(Tooltip.create(Component.literal("")
                .append(Component.translatable("screens.wynntils.partyManagementGui.legend")
                        .withStyle(ChatFormatting.UNDERLINE))
                .append(Component.literal("\n"))
                .append(Component.translatable("screens.wynntils.partyManagementGui.self")
                        .withStyle(ChatFormatting.BOLD))
                .append(Component.literal("\n"))
                .append(Component.translatable("screens.wynntils.partyManagementGui.leader")
                        .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("\n"))
                .append(Component.translatable("screens.wynntils.partyManagementGui.offline")
                        .withStyle(ChatFormatting.STRIKETHROUGH))
                .append(Component.literal("\n"))
                .append(Component.translatable("screens.wynntils.partyManagementGui.friend")
                        .withStyle(ChatFormatting.GREEN))));
    }

    @Override
    public void onPress() {
        // Do nothing
    }
}
