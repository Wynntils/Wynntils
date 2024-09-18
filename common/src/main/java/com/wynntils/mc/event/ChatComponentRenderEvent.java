/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.neoforged.bus.api.Event;

public abstract class ChatComponentRenderEvent extends Event {
    private final ChatComponent chatComponent;

    protected ChatComponentRenderEvent(ChatComponent chatComponent) {
        this.chatComponent = chatComponent;
    }

    public ChatComponent getChatComponent() {
        return chatComponent;
    }

    public static class Pre extends ChatComponentRenderEvent {
        private final GuiGraphics guiGraphics;

        public Pre(ChatComponent chatComponent, GuiGraphics guiGraphics) {
            super(chatComponent);

            this.guiGraphics = guiGraphics;
        }

        public GuiGraphics getGuiGraphics() {
            return guiGraphics;
        }
    }

    public static class Translate extends ChatComponentRenderEvent {
        private float x;

        public Translate(ChatComponent chatComponent, float x) {
            super(chatComponent);

            this.x = x;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }
    }

    public static class MapMouseX extends ChatComponentRenderEvent {
        private double x;

        public MapMouseX(ChatComponent chatComponent, double x) {
            super(chatComponent);

            this.x = x;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }
    }

    public static class Background extends ChatComponentRenderEvent {
        private final GuiGraphics guiGraphics;
        private final int renderX;
        private final int lineHeight;
        private final int opacity;

        public Background(
                ChatComponent chatComponent, GuiGraphics guiGraphics, int renderX, int lineHeight, int opacity) {
            super(chatComponent);

            this.guiGraphics = guiGraphics;
            this.renderX = renderX;
            this.lineHeight = lineHeight;
            this.opacity = opacity;
        }

        public GuiGraphics getGuiGraphics() {
            return guiGraphics;
        }

        public int getRenderX() {
            return renderX;
        }

        public int getLineHeight() {
            return lineHeight;
        }

        public int getOpacity() {
            return opacity;
        }
    }

    public static class Text extends ChatComponentRenderEvent {
        private final GuiGraphics guiGraphics;
        private final GuiMessage.Line line;
        private final Font font;
        private final int renderY;
        private final int textOpacity;

        public Text(
                ChatComponent chatComponent,
                GuiGraphics guiGraphics,
                GuiMessage.Line line,
                Font font,
                int renderY,
                int textOpacity) {
            super(chatComponent);

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
