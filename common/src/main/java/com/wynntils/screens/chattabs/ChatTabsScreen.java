/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.chattabs;

import com.wynntils.core.components.Services;
import com.wynntils.screens.chattabs.widgets.ChatTabButton;
import com.wynntils.screens.chattabs.widgets.ChatTabSettingsButton;
import com.wynntils.services.chat.type.ChatTab;
import com.wynntils.utils.mc.KeyboardUtils;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;

public class ChatTabsScreen extends ChatScreen {
    private final boolean oldTabHotkey;

    public ChatTabsScreen(String initial, boolean oldTabHotkey) {
        super(initial);
        this.oldTabHotkey = oldTabHotkey;
    }

    @Override
    public void init() {
        super.init();

        int xOffset = 0;

        this.addRenderableWidget(new ChatTabSettingsButton(xOffset + 2, this.height - 35, 12, 13));
        xOffset += 15;

        for (ChatTab chatTab : Services.ChatTab.getChatTabs()) {
            this.addRenderableWidget(new ChatTabButton(xOffset + 2, this.height - 35, 40, 13, chatTab));
            xOffset += 43;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            int newTab = -1;
            if (oldTabHotkey) {
                if (KeyboardUtils.isShiftDown()) {
                    newTab = Services.ChatTab.getTabIndexAfterFocused();
                }
            } else {
                if (KeyboardUtils.isControlDown()) {
                    newTab = KeyboardUtils.isShiftDown()
                            ? Services.ChatTab.getTabIndexBeforeFocused()
                            : Services.ChatTab.getTabIndexAfterFocused();
                }
            }

            if (newTab != -1) {
                Services.ChatTab.setFocusedTab(newTab);
                return true;
            }
        }

        if (KeyboardUtils.isControlDown() && keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
            ChatTab newTab = Services.ChatTab.getTab(keyCode - GLFW.GLFW_KEY_1);
            if (newTab != null) {
                Services.ChatTab.setFocusedTab(newTab);
            }
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void changeFocus(ComponentPath componentPath) {
        GuiEventListener guiEventListener = componentPath instanceof ComponentPath.Path path
                ? path.childPath().component()
                : componentPath.component();

        // These should not be focused
        if (guiEventListener instanceof ChatTabButton || guiEventListener instanceof ChatTabSettingsButton) return;

        super.changeFocus(componentPath);
    }
}
