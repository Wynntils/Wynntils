package com.wynntils.screens.maps.categorymanagerwidgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.type.OptionCategory;
import com.wynntils.screens.maps.type.ScrollableWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class CategoryHeaderWidget extends AbstractWidget implements ScrollableWidget<Void> {
    private final OptionCategory category;

    CategoryHeaderWidget(OptionCategory category) {
        super(0, 0, 0, 16, Component.literal(category.getDisplayName()));
        this.category = category;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.CATEGORY_MANAGER_OPTION_HEADER,
                getX(),
                getY(),
                150,
                this.height);

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
                getY(),
                this.width - 150 - 5,
                this.height);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    // ----- ScrollableWidget implementation -----

    @Override
    public Void getValue() {
        return null;
    }

    @Override
    public void setValue(Void newValue) {}

    @Override
    public OptionCategory getCategory() {
        return category;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public int getHeight() {
        return height;
    }
}