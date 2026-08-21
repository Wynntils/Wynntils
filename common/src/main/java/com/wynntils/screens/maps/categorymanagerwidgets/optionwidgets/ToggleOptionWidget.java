package com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.type.OptionCategory;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.function.Function;

public class ToggleOptionWidget extends AbstractOptionWidget<Boolean> {
    private static final int BUTTON_HEIGHT = 20;


    public ToggleOptionWidget(
            String label,
            OptionCategory category,
            boolean defaultValue,
            Function<MapAttributes, Optional<Boolean>> valueGetter) {
        super(label, 20, category, defaultValue, valueGetter);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance().renderText(
                guiGraphics,
                StyledText.fromString(getMessage().getString()),
                getX(),
                getY() + this.height / 2f,
                !this.inherited || isChanged() ? CommonColors.WHITE : CommonColors.GRAY,
                HorizontalAlignment.LEFT,
                VerticalAlignment.MIDDLE,
                TextShadow.NORMAL);

        if (isMouseInsideButton(mouseX, mouseY)) {
            handleCursor(guiGraphics);
        }

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                getButtonTexture(),
                getButtonX(),
                getY() + (this.height - BUTTON_HEIGHT) / 2f,
                getButtonTexture().width(),
                BUTTON_HEIGHT);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        if (isMouseInsideButton(event.x(), event.y())) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());

            setValue(!this.value);

            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private Texture getButtonTexture() {
        return this.value ? Texture.MANAGER_TOGGLE_BUTTON_ON : Texture.MANAGER_TOGGLE_BUTTON_OFF;
    }

    private int getButtonX() {
        return getX() + getWidth() - getButtonTexture().width();
    }

    private boolean isMouseInsideButton(double mouseX, double mouseY) {
        int buttonX = getButtonX();
        int buttonY = getY() + (this.height - BUTTON_HEIGHT) / 2;

        return MathUtils.isInside(
                (int) mouseX,
                (int) mouseY,
                buttonX,
                buttonX + getButtonTexture().width(),
                buttonY,
                buttonY + BUTTON_HEIGHT
        );
    }
}