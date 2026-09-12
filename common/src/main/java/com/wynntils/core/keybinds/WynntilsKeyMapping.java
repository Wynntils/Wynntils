/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class WynntilsKeyMapping extends KeyMapping {
    private boolean blockedByChat;

    public WynntilsKeyMapping(KeyBindDefinition definition) {
        super(definition.translationKey(), definition.type(), definition.defaultKey(), definition.category());
    }

    public void onInput(int action, boolean inChat) {
        if (action == GLFW.GLFW_RELEASE) {
            blockedByChat = false;
        } else if (inChat) {
            suppressChatInput();
        } else if (action == GLFW.GLFW_PRESS) {
            blockedByChat = false;
        }
    }

    public void suppressChatInput() {
        if (key.getType() == InputConstants.Type.MOUSE) return;

        blockedByChat = true;
        super.setDown(false);
    }

    @Override
    public void setDown(boolean down) {
        super.setDown(down && !blockedByChat);
    }

    @Override
    public void setKey(InputConstants.Key key) {
        if (!this.key.equals(key)) blockedByChat = false;
        super.setKey(key);
    }
}
