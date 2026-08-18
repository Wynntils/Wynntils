package com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.CategoryManagementScreen;
import com.wynntils.screens.maps.categorymanagerwidgets.TexturedTextInputBoxWidget;
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

public class FloatSliderOptionWidget extends AbstractWidget implements ScrollableWidget<Float> {
    // Gap between the end of the label text and the start of the slider track.
    private static final int LABEL_PADDING = 8;

    // Gap between the end of the slider track and the value textbox.
    private static final int TEXTBOX_GAP = 5;

    // Gap between the head and the end of the track.
    private static final int TRACK_END_PADDING = 2;
    private static final int TEXTBOX_WIDTH = 44;

    private static final int TRACK_HEIGHT = 6;
    private static final int HEAD_WIDTH = 8;
    private static final int HEAD_HEIGHT = 14;

    private final OptionCategory category;
    private final float minValue;
    private final float maxValue;
    private final float step;
    private final int decimalPlaces;

    private final TexturedTextInputBoxWidget valueTextBox;

    private float value;

    private boolean draggingSlider = false;
    private boolean draggingTextbox = false;

    public FloatSliderOptionWidget(
            String label,
            OptionCategory category,
            float initialValue,
            float minValue,
            float maxValue,
            CategoryManagementScreen parent) {
        this(label, category, initialValue, minValue, maxValue, 0.1f, 1, parent);
    }

    private FloatSliderOptionWidget(
            String label,
            OptionCategory category,
            float initialValue,
            float minValue,
            float maxValue,
            float step,
            int decimalPlaces,
            CategoryManagementScreen parent) {
        super(0, 0, 0, 20, Component.literal(label));
        this.category = category;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
        this.decimalPlaces = decimalPlaces;
        this.valueTextBox = new TexturedTextInputBoxWidget(
                getTextboxX(),
                getY(),
                TEXTBOX_WIDTH,
                this.height,
                this::onTextInputUpdate,
                parent,
                TexturedTextInputBoxWidget.Mode.FLOAT);

        setInternalValue(initialValue, false);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isMouseOverHead(mouseX, mouseY)) {
            handleCursor(guiGraphics);
        }

        FontRenderer.getInstance().renderText(
                guiGraphics,
                StyledText.fromString(getMessage().getString()),
                getX(),
                getY() + this.height / 2f,
                CommonColors.WHITE,
                HorizontalAlignment.LEFT,
                VerticalAlignment.MIDDLE,
                TextShadow.NORMAL);

        valueTextBox.setX(getTextboxX());
        valueTextBox.setY(getY());
        valueTextBox.setWidth(TEXTBOX_WIDTH);
        valueTextBox.setHeight(this.height);
        valueTextBox.render(guiGraphics, mouseX, mouseY, partialTick);

        int trackLeft = getTrackLeft();
        int trackRight = getTrackRight();
        int trackY = getY() + (this.height - TRACK_HEIGHT) / 2;

        float headX = getHeadX(trackLeft, trackRight);
        float headCenterX = headX + HEAD_WIDTH / 2f;

        float filledWidth = Math.max(0f, headCenterX - trackLeft);
        float emptyWidth = Math.max(0f, trackRight - headCenterX);

        if (filledWidth > 0f) {
            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics,
                    Texture.MANAGER_SLIDER_FILLED,
                    trackLeft, trackY,
                    filledWidth,
                    TRACK_HEIGHT);
        }

        if (emptyWidth > 0f) {
            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics,
                    Texture.MANAGER_SLIDER_EMPTY,
                    headCenterX,
                    trackY,
                    emptyWidth,
                    TRACK_HEIGHT);
        }

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_SCROLL_BAR_BUTTON,
                headX,
                getY() + (this.height - HEAD_HEIGHT) / 2f,
                HEAD_WIDTH,
                HEAD_HEIGHT);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        boolean overTextbox = valueTextBox.isMouseOver(event.x(), event.y());
        boolean textboxHandled = valueTextBox.mouseClicked(event, isDoubleClick);

        draggingTextbox = overTextbox && textboxHandled;
        if (textboxHandled) return true;

        if (MathUtils.isInside(
                (int) event.x(),
                (int) event.y(),
                getTrackLeft(),
                getTrackRight(),
                getY(),
                getY() + getHeight() - 1)) {
            draggingSlider = true;
            this.playDownSound(McUtils.mc().getSoundManager());
            updateValueFromMouseX(event.x());
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingTextbox) {
            return valueTextBox.mouseDragged(event, dragX, dragY);
        }

        if (draggingSlider) {
            updateValueFromMouseX(event.x());
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean wasDraggingSlider = draggingSlider;
        boolean wasDraggingTextbox = draggingTextbox;
        draggingSlider = false;
        draggingTextbox = false;

        if (wasDraggingTextbox) {
            valueTextBox.mouseReleased(event);
            return true;
        }

        return wasDraggingSlider;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private void onTextInputUpdate(String text) {
        this.value = valueTextBox.getValueAsFloat();
    }

    private void updateValueFromMouseX(double mouseX) {
        float travelLeft = getTrackLeft() + TRACK_END_PADDING;
        float travelRight = getTrackRight() - HEAD_WIDTH - TRACK_END_PADDING;

        if (travelRight <= travelLeft) {
            setInternalValue(minValue, true);
            return;
        }

        float clampedMouseX = MathUtils.clamp((float) mouseX - HEAD_WIDTH / 2f, travelLeft, travelRight);
        float newValue = MathUtils.map(clampedMouseX, travelLeft, travelRight, minValue, maxValue);

        setInternalValue(newValue, true);
    }

    private void setInternalValue(float newValue, boolean snapToStep) {
        float min = Math.min(minValue, maxValue);
        float max = Math.max(minValue, maxValue);
        float clamped = MathUtils.clamp(newValue, min, max);

        if (snapToStep && step > 0f) {
            clamped = minValue + Math.round((clamped - minValue) / step) * step;
            clamped = MathUtils.clamp(clamped, min, max);
        }

        this.value = clamped;
        valueTextBox.setTextBoxInput(formatValue(this.value));
    }

    private String formatValue(float value) {
        if (decimalPlaces <= 0) {
            return String.valueOf(Math.round(value));
        }
        return String.format("%." + decimalPlaces + "f", value);
    }

    private int getTextboxX() {
        return getX() + getWidth() - TEXTBOX_WIDTH;
    }

    private int getTrackLeft() {
        int labelWidth = FontRenderer.getInstance().getFont().width(getMessage().getString());
        return getX() + labelWidth + LABEL_PADDING;
    }

    private int getTrackRight() {
        return getTextboxX() - TEXTBOX_GAP;
    }

    private float getHeadX(int trackLeft, int trackRight) {
        int travelLeft = trackLeft + TRACK_END_PADDING;
        int travelRight = trackRight - HEAD_WIDTH - TRACK_END_PADDING;

        if (travelRight <= travelLeft || maxValue <= minValue) return travelLeft;

        float min = Math.min(minValue, maxValue);
        float max = Math.max(minValue, maxValue);
        float clampedValue = MathUtils.clamp(value, min, max);

        return MathUtils.map(clampedValue, minValue, maxValue, travelLeft, travelRight);
    }

    private boolean isMouseOverHead(double mouseX, double mouseY) {
        float headX = getHeadX(getTrackLeft(), getTrackRight());
        int headY = getY() + (this.height - HEAD_HEIGHT) / 2;

        return MathUtils.isInside(
                (int) mouseX,
                (int) mouseY,
                (int) headX,
                (int) (headX + HEAD_WIDTH),
                headY,
                (headY + HEAD_HEIGHT));
    }

    // ----- ScrollableWidget implementation -----

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public void setValue(Float newValue) {
        setInternalValue(newValue, false);
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

    @Override
    public boolean isMouseOverTextBox(double mouseX, double mouseY) {
        return valueTextBox.isMouseOver(mouseX, mouseY);
    }

    @Override
    public TextInputBoxWidget getTextInputBoxWidget() {
        return valueTextBox;
    }
}