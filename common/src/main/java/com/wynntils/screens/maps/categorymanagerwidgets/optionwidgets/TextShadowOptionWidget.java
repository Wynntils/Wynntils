package com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.type.OptionCategory;
import com.wynntils.screens.maps.type.ScrollableWidget;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
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
import org.lwjgl.glfw.GLFW;

public class TextShadowOptionWidget extends AbstractWidget implements ScrollableWidget<TextShadow> {
    private static final int BUTTON_WIDTH = 70;
    private static final int BUTTON_HEIGHT = 20;

    private final OptionCategory category;
    private TextShadow value;

    public TextShadowOptionWidget(String label, OptionCategory category, TextShadow initialValue) {
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

        int elementY = getY() + (this.height - BUTTON_HEIGHT) / 2;
        int buttonX = getX() + getWidth() - BUTTON_WIDTH;

        if (isMouseOverButton(mouseX, mouseY)) {
            handleCursor(guiGraphics);
        }

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_WIDGET_BACKGROUND,
                buttonX,
                elementY,
                BUTTON_WIDTH,
                BUTTON_HEIGHT);

        fontRenderer.renderText(
                guiGraphics,
                StyledText.fromString(TextShadowInternal.from(value).getDisplayName()),
                buttonX + BUTTON_WIDTH / 2f,
                elementY + BUTTON_HEIGHT / 2f,
                CommonColors.WHITE,
                HorizontalAlignment.CENTER,
                VerticalAlignment.MIDDLE,
                value);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        if (isMouseOverButton(event.x(), event.y())) {
            this.playDownSound(McUtils.mc().getSoundManager());
            this.value = TextShadowInternal.from(value).next().toTextShadow();
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private boolean isMouseOverButton(double mouseX, double mouseY) {
        int buttonY = getY() + (this.height - BUTTON_HEIGHT) / 2;
        int buttonX = getX() + getWidth() - BUTTON_WIDTH;

        return MathUtils.isInside(
                (int) mouseX,
                (int) mouseY,
                buttonX,
                buttonX + BUTTON_WIDTH - 1,
                buttonY,
                buttonY + BUTTON_HEIGHT - 1
        );
    }

    // ----- ScrollableWidget implementation -----

    @Override
    public TextShadow getValue() {
        return value;
    }

    @Override
    public void setValue(TextShadow newValue) {
        this.value = newValue;
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

    private enum TextShadowInternal {
        NONE(TextShadow.NONE, "None"),
        NORMAL(TextShadow.NORMAL, "Normal"),
        OUTLINE(TextShadow.OUTLINE, "Outline");

        private static final TextShadowInternal[] VALUES = values();

        private final TextShadow textShadow;
        private final String displayName;

        TextShadowInternal(TextShadow textShadow, String displayName) {
            this.textShadow = textShadow;
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public TextShadow toTextShadow() {
            return textShadow;
        }

        public TextShadowInternal next() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }

        public static TextShadowInternal from(TextShadow textShadow) {
            for (TextShadowInternal internal : VALUES) {
                if (internal.toTextShadow() == textShadow) {
                    return internal;
                }
            }
            return null;
        }
    }
}