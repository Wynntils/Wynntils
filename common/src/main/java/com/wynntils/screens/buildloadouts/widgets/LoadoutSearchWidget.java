package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public class LoadoutSearchWidget extends SearchWidget {
    public LoadoutSearchWidget(int x, int y, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
        super(x + 5 + Texture.BUILD_LOADOUTS_SEARCH_ICON.width(),
                y,
                133 - 10 - 5 - Texture.BUILD_LOADOUTS_SEARCH_ICON.width(),
                20,
                onUpdateConsumer,
                textboxScreen);
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                getX() - 5 - Texture.BUILD_LOADOUTS_SEARCH_ICON.width(),
                getY(),
                this.width + 5 + Texture.BUILD_LOADOUTS_SEARCH_ICON.width(),
                this.height);

        RenderUtils.drawTexturedRect(guiGraphics,
                Texture.BUILD_LOADOUTS_SEARCH_ICON,
                this.getX() - Texture.BUILD_LOADOUTS_SEARCH_ICON.width(),
                this.getY() + VERTICAL_OFFSET
        );
    }

    @Override
    protected void renderText(
            GuiGraphics guiGraphics,
            String renderedText,
            int renderedTextStart,
            String firstPortion,
            String highlightedPortion,
            String lastPortion,
            Font font,
            int firstWidth,
            int highlightedWidth,
            int lastWidth,
            boolean defaultText) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(defaultText ? DEFAULT_TEXT.getString() : firstPortion),
                        this.getX() + textPadding,
                        this.getX() + this.width - textPadding - lastWidth - highlightedWidth,
                        this.getY() + VERTICAL_OFFSET,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        TextShadow.NORMAL);

        if (!defaultText) {
            FontRenderer.getInstance()
                    .renderAlignedHighlightedTextInBox(
                            guiGraphics,
                            StyledText.fromString(highlightedPortion),
                            this.getX() + textPadding + firstWidth,
                            this.getX() + this.width - textPadding - lastWidth,
                            this.getY() + VERTICAL_OFFSET,
                            this.getY() + VERTICAL_OFFSET,
                            0,
                            CommonColors.BLUE,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP);

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            StyledText.fromString(lastPortion),
                            this.getX() + textPadding + firstWidth + highlightedWidth,
                            this.getX() + this.width - textPadding,
                            this.getY() + VERTICAL_OFFSET,
                            0,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            TextShadow.NORMAL);
        }

        drawCursor(
                guiGraphics,
                this.getX()
                        + font.width(renderedText.substring(0, Math.min(cursorPosition, renderedText.length())))
                        + textPadding
                        - 2,
                this.getY() + VERTICAL_OFFSET,
                VerticalAlignment.TOP,
                false);
    }
}
