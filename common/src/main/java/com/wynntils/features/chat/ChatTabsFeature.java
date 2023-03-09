/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.chat.ChatTab;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.ChatScreenKeyTypedEvent;
import com.wynntils.mc.event.ClientsideMessageEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.ScreenRenderEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.screens.chattabs.widgets.ChatTabAddButton;
import com.wynntils.screens.chattabs.widgets.ChatTabButton;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.CHAT)
public class ChatTabsFeature extends UserFeature {
    // We do this here, and not in Models.ChatTab to not introduce a feature-model dependency.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChatReceived(ChatMessageReceivedEvent event) {
        // We are already sending this message to every matching tab, so we can cancel it.
        event.setCanceled(true);

        Managers.ChatTab.matchMessage(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClientsideChat(ClientsideMessageEvent event) {
        // We've already sent this message to every matching tab, so we can cancel it.
        event.setCanceled(true);

        Managers.ChatTab.matchMessage(event);
    }

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        if (event.getScreen() instanceof ChatScreen chatScreen) {
            int xOffset = 0;

            chatScreen.addRenderableWidget(new ChatTabAddButton(xOffset + 2, chatScreen.height - 35, 12, 13));
            xOffset += 15;

            for (ChatTab chatTab : Managers.ChatTab.getTabs().toList()) {
                chatScreen.addRenderableWidget(new ChatTabButton(xOffset + 2, chatScreen.height - 35, 40, 13, chatTab));
                xOffset += 43;
            }
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldState.WORLD) return;
        if (Managers.ChatTab.getFocusedTab() != null) return;

        Managers.ChatTab.resetFocusedTab();
    }

    @SubscribeEvent
    public void onScreenRender(ScreenRenderEvent event) {
        if (!(event.getScreen() instanceof ChatScreen chatScreen)) return;

        // We render this twice for chat screen, but it is not heavy and this is a simple and least conflicting way of
        // rendering command suggestions on top of chat tab buttons.
        chatScreen.commandSuggestions.render(event.getPoseStack(), event.getMouseX(), event.getMouseY());
    }

    @SubscribeEvent
    public void onChatScreenKeyTyped(ChatScreenKeyTypedEvent event) {
        // We can't use keybinds here to not conflict with TAB key's other behaviours.
        if (event.getKeyCode() != GLFW.GLFW_KEY_TAB) return;
        if (!(McUtils.mc().screen instanceof ChatScreen)) return;
        if (!KeyboardUtils.isShiftDown()) return;

        event.setCanceled(true);
        Managers.ChatTab.setFocusedTab(Managers.ChatTab.getNextFocusedTab());
    }

    @Override
    protected void postEnable() {
        Managers.ChatTab.resetFocusedTab();
    }

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        Managers.ChatTab.resetFocusedTab();

        if ((McUtils.mc().screen instanceof ChatScreen chatScreen)) {
            // Reload chat tab buttons
            chatScreen.init(
                    McUtils.mc(),
                    McUtils.mc().getWindow().getGuiScaledWidth(),
                    McUtils.mc().getWindow().getGuiScaledHeight());
        }
    }
}
