/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.ui.base;

import com.wynntils.core.config.reflection.ConfigField;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public abstract class ConfigWidget<T> extends GuiComponent
        implements Widget, GuiEventListener, NarratableEntry {
    private final ConfigField<T> field;
    protected int x;
    protected int y;

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public ConfigWidget(ConfigField<T> field) {
        this.field = field;
    }

    public void setValue(T value) {
        field.setFieldValue(value);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
