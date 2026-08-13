package com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.type.OptionCategory;
import com.wynntils.screens.maps.type.ScrollableWidget;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class IconOptionWidget extends AbstractWidget implements ScrollableWidget<Boolean> {
    private final OptionCategory category;
    private boolean value;

    public IconOptionWidget(String label, OptionCategory category, boolean initialValue) {
        super(0, 0, 0, 20, Component.literal(label));
        this.category = category;
        this.value = initialValue;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer fontRenderer = FontRenderer.getInstance();

        fontRenderer.renderText(
                guiGraphics,
                StyledText.fromString(getMessage().getString()),
                getX(),
                getY() + this.height / 2f,
                CommonColors.WHITE,
                HorizontalAlignment.LEFT,
                VerticalAlignment.MIDDLE,
                TextShadow.NORMAL);

        String valueText = value ? "ON" : "OFF";
        CustomColor valueColor = value ? CommonColors.GREEN : CommonColors.RED;

        int valueX = getX() + getWidth() - fontRenderer.getFont().width(valueText) - 4;
        fontRenderer.renderText(
                guiGraphics,
                StyledText.fromString(valueText),
                valueX,
                getY() + this.height / 2f,
                valueColor,
                HorizontalAlignment.LEFT,
                VerticalAlignment.MIDDLE,
                TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;


        if (isMouseOverValueText(event.x(), event.y())) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            value = !value;
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private boolean isMouseOverValueText(double mouseX, double mouseY) {
        String valueText = value ? "ON" : "OFF";
        FontRenderer fontRenderer = FontRenderer.getInstance();
        int textWidth = fontRenderer.getFont().width(valueText);

        int valueX = getX() + getWidth() - textWidth - 4;

        return MathUtils.isInside(
                (int) mouseX,
                (int) mouseY,
                valueX,
                valueX + textWidth - 1,
                getY(),
                getY() + getHeight() - 1
        );
    }

    // ----- ScrollableWidget implementation -----

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(Boolean newValue) {
        value = value;
    }

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

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}