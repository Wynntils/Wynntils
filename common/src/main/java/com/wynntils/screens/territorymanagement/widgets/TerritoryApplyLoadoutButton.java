/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.widgets;

import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TerritoryApplyLoadoutButton extends WynntilsButton implements TooltipProvider {
    private static final int TOOLTIP_WIDTH = 200;

    private final Supplier<Texture> textureSupplier;
    private final Consumer<Integer> onClick;
    private final List<Component> tooltip;

    public TerritoryApplyLoadoutButton(
            int x,
            int y,
            int width,
            int height,
            Supplier<Texture> textureSupplier,
            Consumer<Integer> onClick,
            List<Component> tooltip) {
        super(x, y, width, height, Component.empty());

        this.textureSupplier = textureSupplier;
        this.onClick = onClick;
        this.tooltip = tooltip;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(guiGraphics.pose(), textureSupplier.get(), this.getX(), this.getY());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());
        onClick.accept(button);

        return true;
    }

    @Override
    public List<Component> getTooltipLines() {
        return ComponentUtils.wrapTooltips(tooltip, TOOLTIP_WIDTH);
    }

    @Override
    public void onPress() {
        // Unused
    }
}
