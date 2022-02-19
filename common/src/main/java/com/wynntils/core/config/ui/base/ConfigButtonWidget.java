/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.ui.base;

import com.wynntils.core.config.reflection.ConfigField;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public abstract class ConfigButtonWidget<T> extends ConfigWidget<T> {
    protected int width;
    protected int height;

    public ConfigButtonWidget(ConfigField<T> field, int width, int height) {
        super(field);

        this.width = width;
        this.height = height;
    }

    public abstract void onPress();

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= (double) x
                && mouseY >= (double) y
                && mouseX < (double) (x + width)
                && mouseY < (double) (y + height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            McUtils.mc()
                    .getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            onPress();
            return true;
        }

        return false;
    }
}
