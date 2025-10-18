/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.google.common.collect.Lists;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class GuideFilterButton<T extends ItemStatProvider<?>> extends AbstractWidget {
    protected final Texture texture;

    protected boolean state;

    protected GuideFilterButton(int x, int y, Texture texture) {
        super(x, y, 16, 16, Component.empty());

        this.texture = texture;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(guiGraphics.pose(), texture, getX(), getY());

        if (!isHovered && !state) return;

        RenderUtils.drawRect(
                guiGraphics.pose(),
                (state ? CommonColors.ORANGE : CommonColors.WHITE).withAlpha(isHovered ? 0.7f : 0.5f),
                getX(),
                getY(),
                0,
                getWidth(),
                getHeight());

        if (isHovered) {
            McUtils.screen()
                    .setTooltipForNextRenderPass(Lists.transform(
                            ComponentUtils.wrapTooltips(
                                    List.of(Component.translatable(
                                            "screens.wynntils.wynntilsGuides.filterWidget.tooltip", getFilterName())),
                                    200),
                            Component::getVisualOrderText));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        state = !state;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected abstract void updateStateFromQuery(ItemSearchQuery searchQuery);

    protected abstract StatProviderAndFilterPair getFilterPair(T provider);

    protected abstract String getFilterName();

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
