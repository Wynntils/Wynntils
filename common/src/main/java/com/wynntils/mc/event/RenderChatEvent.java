/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class RenderChatEvent extends Event {
    private final PoseStack poseStack;
    private final Window window;
    private ChatComponent renderedChat;

    public RenderChatEvent(PoseStack poseStack, Window window, ChatComponent renderedChat) {
        this.poseStack = poseStack;
        this.window = window;
        this.renderedChat = renderedChat;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public Window getWindow() {
        return window;
    }

    public ChatComponent getRenderedChat() {
        return renderedChat;
    }

    public void setRenderedChat(ChatComponent renderedChat) {
        this.renderedChat = renderedChat;
    }
}
