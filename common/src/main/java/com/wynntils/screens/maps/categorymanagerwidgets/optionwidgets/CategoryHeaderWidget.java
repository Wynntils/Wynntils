package com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.type.OptionCategory;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;

public class CategoryHeaderWidget extends AbstractOptionWidget<Void> {
    private static int CATEGORY_HEIGHT = 16;

    private final OptionCategory category;

    public CategoryHeaderWidget(OptionCategory category) {
        super(category.getDisplayName(), 26, category, null, null);
        this.category = category;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.CATEGORY_MANAGER_OPTION_HEADER,
                getX(),
                getY() + (this.height - CATEGORY_HEIGHT) / 2f,
                150,
                CATEGORY_HEIGHT);

        FontRenderer.getInstance().renderText(
                guiGraphics,
                StyledText.fromString(getMessage().getString()),
                getX() + 20,
                getY() + this.height / 2f,
                CommonColors.WHITE,
                HorizontalAlignment.LEFT,
                VerticalAlignment.MIDDLE,
                TextShadow.NORMAL);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.CATEGORY_MANAGER_OPTION_HEADER_LINE,
                getX() + 150 + 5,
                getY() + (this.height - CATEGORY_HEIGHT) / 2f,
                this.width - 150 - 5,
                CATEGORY_HEIGHT);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}