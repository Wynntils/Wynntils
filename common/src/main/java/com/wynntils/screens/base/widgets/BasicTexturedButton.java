/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class BasicTexturedButton extends WynntilsButton implements TooltipProvider {
    protected static final int TOOLTIP_WIDTH = 200;

    private final Texture texture;

    protected final Consumer<Integer> onClick;
    private List<Component> tooltip;

    private final boolean scaleTexture;

    public BasicTexturedButton(
            int x, int y, int width, int height, Texture texture, Consumer<Integer> onClick, List<Component> tooltip) {
        this(x, y, width, height, texture, onClick, tooltip, false);
    }

    public BasicTexturedButton(
            int x,
            int y,
            int width,
            int height,
            Texture texture,
            Consumer<Integer> onClick,
            List<Component> tooltip,
            boolean scaleTexture) {
        super(x, y, width, height, Component.literal("Basic Button"));
        this.texture = texture;
        this.onClick = onClick;
        this.scaleTexture = scaleTexture;
        this.setTooltip(tooltip);
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (scaleTexture) {
            RenderUtils.drawScalingTexturedRect(
                    guiGraphics,
                    texture.identifier(),
                    this.getX(),
                    this.getY(),
                    getWidth(),
                    getHeight(),
                    texture.width(),
                    texture.height());
        } else {
            RenderUtils.drawTexturedRect(guiGraphics, texture, this.getX(), this.getY());
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());
        onClick.accept(event.button());

        return true;
    }

    @Override
    public void onPress(InputWithModifiers input) {}

    public void setTooltip(List<Component> newTooltip) {
        tooltip = ComponentUtils.wrapTooltips(newTooltip, TOOLTIP_WIDTH);
    }

    @Override
    public List<Component> getTooltipLines() {
        return tooltip;
    }
}
