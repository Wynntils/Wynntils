/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public abstract class GuideButton extends WynntilsButton {
    protected final GuideItemStack itemStack;
    protected final String itemName;

    protected GuideButton(int x, int y, int width, GuideItemStack itemStack) {
        super(x, y, width, 20, Component.empty());

        this.itemStack = itemStack;

        Optional<NamedItemProperty> namedItemPropertyOpt =
                Models.Item.asWynnItemProperty(itemStack, NamedItemProperty.class);
        if (namedItemPropertyOpt.isPresent()) {
            itemName = namedItemPropertyOpt.get().getName();
        } else if (itemStack != null) {
            // All guide items should be a NamedItemProperty but just in case
            itemName = StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting();
        } else {
            // Set guide buttons
            itemName = "";
        }
    }

    protected GuideButton(int x, int y, GuideItemStack itemStack) {
        this(x, y, 20, itemStack);
    }

    @Override
    public void renderContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBaseItem(guiGraphics, getColor());
        renderFavoriteIcon(guiGraphics);
        renderTooltipIfHovered(guiGraphics, mouseX, mouseY);
    }

    protected void renderBaseItem(GuiGraphicsExtractor guiGraphics, CustomColor color) {
        RenderUtils.drawSprite(guiGraphics, Texture.HIGHLIGHT_WYNN, color, getX() - 6, getY() - 6);
        RenderUtils.renderItem(guiGraphics, itemStack, getX() + 2, getY() + 2);
    }

    protected void renderFavoriteIcon(GuiGraphicsExtractor guiGraphics) {
        if (!Services.Favorites.isFavorite(itemName)) return;

        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                Texture.FAVORITE_ICON.identifier(),
                getX(),
                getY(),
                9,
                9,
                Texture.FAVORITE_ICON.width(),
                Texture.FAVORITE_ICON.height());
    }

    protected void renderTextOverlay(
            GuiGraphicsExtractor guiGraphics,
            String text,
            int minXOffset,
            int maxXOffset,
            int yOffset,
            CustomColor color,
            HorizontalAlignment alignment) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(text),
                        getX() + minXOffset,
                        getX() + maxXOffset,
                        getY() + yOffset,
                        0,
                        color,
                        alignment,
                        TextShadow.OUTLINE);
    }

    protected void renderTooltipIfHovered(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (isHovered) {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    protected abstract CustomColor getColor();

    protected void renderTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        itemStack.queueGuideTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (itemName.isEmpty()) return;

        if (input.hasShiftDown()) {
            Services.Favorites.toggleFavorite(itemName);
        }
    }
}
