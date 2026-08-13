package com.wynntils.screens.maps.categorymanagerwidgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.type.OptionCategory;
import com.wynntils.screens.maps.type.ScrollableWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class CategoryHeaderWidget extends AbstractWidget implements ScrollableWidget<Void> {
    private static final int HEIGHT = 14;

    private final OptionCategory category;

    CategoryHeaderWidget(OptionCategory category) {
        super(0, 0, 0, HEIGHT, Component.literal(category.getDisplayName()));
        this.category = category;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance().renderText(
                guiGraphics,
                StyledText.fromString(getMessage().getString()),
                getX() + getWidth() / 2f,
                getY() + getHeight() / 2f,
                CommonColors.WHITE,
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE,
                TextShadow.NORMAL);
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