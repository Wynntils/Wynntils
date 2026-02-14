/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.neoforged.bus.api.Event;

public abstract class ChatComponentRenderEvent extends Event {
    public static class Pre extends ChatComponentRenderEvent {
        private final ChatComponent chatComponent;

        public Pre(ChatComponent chatComponent) {
            this.chatComponent = chatComponent;
        }

        public ChatComponent getChatComponent() {
            return chatComponent;
        }
    }

    public static class Translate extends ChatComponentRenderEvent {
        private float x;

        public Translate(float x) {
            this.x = x;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }
    }

    public static class Background extends ChatComponentRenderEvent {
        private final GuiGraphics guiGraphics;
        private final int renderY;
        private final int lineHeight;
        private final float opacity;

        public Background(GuiGraphics guiGraphics, int renderY, int lineHeight, float opacity) {
            this.guiGraphics = guiGraphics;
            this.renderY = renderY;
            this.lineHeight = lineHeight;
            this.opacity = opacity;
        }

        public GuiGraphics getGuiGraphics() {
            return guiGraphics;
        }

        public int getRenderY() {
            return renderY;
        }

        public int getLineHeight() {
            return lineHeight;
        }

        public float getOpacity() {
            return opacity;
        }
    }

    public static class Text extends ChatComponentRenderEvent {
        private final GuiGraphics guiGraphics;
        private final GuiMessage.Line line;
        private final Font font;
        private final int renderY;
        private final int textOpacity;

        public Text(GuiGraphics guiGraphics, GuiMessage.Line line, Font font, int renderY, int textOpacity) {
            this.guiGraphics = guiGraphics;
            this.line = line;
            this.font = font;
            this.renderY = renderY;
            this.textOpacity = textOpacity;
        }

        public GuiGraphics getGuiGraphics() {
            return guiGraphics;
        }

        public GuiMessage.Line getLine() {
            return line;
        }

        public Font getFont() {
            return font;
        }

        public int getRenderY() {
            return renderY;
        }

        public int getTextOpacity() {
            return textOpacity;
        }
    }
}
