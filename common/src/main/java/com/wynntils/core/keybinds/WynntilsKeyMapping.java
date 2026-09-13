/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class WynntilsKeyMapping extends KeyMapping {
    private boolean blockedByScreen;

    public WynntilsKeyMapping(KeyBindDefinition definition) {
        super(definition.translationKey(), definition.type(), definition.defaultKey(), definition.category());
    }

    public void onInput(int action, boolean inScreen) {
        if (action == GLFW.GLFW_RELEASE) {
            blockedByScreen = false;
        } else if (inScreen) {
            suppressScreenInput();
        } else if (action == GLFW.GLFW_PRESS) {
            blockedByScreen = false;
        }
    }

    public void suppressScreenInput() {
        if (key.getType() == InputConstants.Type.MOUSE) return;

        blockedByScreen = true;
        super.setDown(false);
    }

    @Override
    public void setDown(boolean down) {
        setDown(down, down && McUtils.mc() != null && McUtils.screen() != null);
    }

    void setDown(boolean down, boolean inScreen) {
        if (down && inScreen) {
            blockedByScreen = false;
        }
        super.setDown(down && !blockedByScreen);
    }

    @Override
    public void setKey(InputConstants.Key key) {
        if (!this.key.equals(key)) blockedByScreen = false;
        super.setKey(key);
    }
}
