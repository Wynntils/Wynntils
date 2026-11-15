/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.render.FontRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public abstract class GuideItemStack extends ItemStack {
    protected GuideItemStack(ItemStack itemStack, ItemAnnotation annotation, String baseName) {
        super(itemStack.getItem(), 1);
        this.applyComponents(itemStack.getComponentsPatch());
        Handlers.Item.updateItem(this, annotation, StyledText.fromString(baseName));
    }

    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<Component> tooltipLines = new ArrayList<>(LoreUtils.getTooltipLines(this));

        guiGraphics.setTooltipForNextFrame(
                FontRenderer.getInstance().getFont(),
                tooltipLines,
                this.getTooltipImage(),
                mouseX,
                mouseY,
                this.get(DataComponents.TOOLTIP_STYLE));
    }
}
