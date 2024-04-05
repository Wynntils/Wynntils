/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.screens.itemfilter.ItemFilterScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class GeneralValueWidget extends AbstractWidget {
    protected final ItemFilterScreen filterScreen;

    protected List<AbstractWidget> widgets = new ArrayList<>();
    protected String query;

    protected GeneralValueWidget(Component title, ItemFilterScreen filterScreen) {
        super(150, 30, 195, 145, title);

        this.filterScreen = filterScreen;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (AbstractWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener listener : widgets) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return listener.mouseClicked(mouseX, mouseY, button);
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (GuiEventListener listener : widgets) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return listener.mouseReleased(mouseX, mouseY, button);
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        return false;
    }

    public abstract void updateValues(List<String> query);

    protected abstract void updateQuery();

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
