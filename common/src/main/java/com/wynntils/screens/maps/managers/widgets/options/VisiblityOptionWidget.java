/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.options;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.managers.CategoryManagementScreen;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.services.mapdata.attributes.impl.MapVisibilityImpl;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class VisiblityOptionWidget extends AbstractOptionWidget<MapVisibility> {
    private static final int TOP_ROW_HEIGHT = 20;
    private static final int SLIDER_HEIGHT = 18;
    private static final int SLIDER_PADDING = 4;
    private static final int BUTTON_WIDTH = 70;
    private static final int BUTTON_HEIGHT = 20;

    private final MapVisibility neverVisibility;
    private final MapVisibility alwaysVisibility;

    public final FloatSliderOptionWidget minSlider;
    public final FloatSliderOptionWidget maxSlider;
    public final FloatSliderOptionWidget fadeSlider;

    private final List<FloatSliderOptionWidget> sliders;

    private final Component minSliderName;
    private final Component maxSliderName;
    private final Component fadeSliderName;

    private Mode currentMode = Mode.ALWAYS;

    public VisiblityOptionWidget(
            Component label,
            Component description,
            OptionCategory category,
            MapVisibility neverVisibility,
            MapVisibility alwaysVisibility,
            Component minSliderName,
            Component minSliderDescription,
            Component maxSliderName,
            Component maxSliderDescription,
            Component fadeSliderName,
            Component fadeSliderDescription,
            Function<MapAttributes, Optional<MapVisibility>> valueGetter,
            CategoryManagementScreen parent) {
        super(label, description, TOP_ROW_HEIGHT, category, valueGetter);
        this.neverVisibility = neverVisibility;
        this.alwaysVisibility = alwaysVisibility;

        this.minSlider = createSlider(minSliderName, minSliderDescription, category, MapVisibility::getMin, parent);
        this.maxSlider = createSlider(maxSliderName, maxSliderDescription, category, MapVisibility::getMax, parent);
        this.fadeSlider = createSlider(fadeSliderName, fadeSliderDescription, category, MapVisibility::getFade, parent);

        this.minSliderName = minSliderName;
        this.maxSliderName = maxSliderName;
        this.fadeSliderName = fadeSliderName;

        this.sliders = List.of(minSlider, maxSlider, fadeSlider);

        applyMode(currentMode);
    }

    private FloatSliderOptionWidget createSlider(
            Component label,
            Component description,
            OptionCategory category,
            Function<MapVisibility, Optional<Float>> component,
            CategoryManagementScreen parent) {
        return new FloatSliderOptionWidget(
                label,
                description,
                category,
                0f,
                100f,
                attrs -> valueGetter.apply(attrs).flatMap(component),
                parent) {
            @Override
            public void setValue(Float newValue) {
                super.setValue(newValue);
                onSliderChanged();
            }
        };
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(getMessage().getString()),
                        getX(),
                        getY() + TOP_ROW_HEIGHT / 2f,
                        !inherited || isChanged() ? CommonColors.WHITE : CommonColors.GRAY,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        int buttonX = getX() + getWidth() - BUTTON_WIDTH;
        int buttonY = getY() + (TOP_ROW_HEIGHT - BUTTON_HEIGHT) / 2;

        if (isMouseOverButton(mouseX, mouseY)) {
            handleCursor(guiGraphics);
        }

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_WIDGET_BACKGROUND, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(currentMode.getDisplayName()),
                        buttonX + BUTTON_WIDTH / 2f,
                        buttonY + BUTTON_HEIGHT / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (isExtended()) {
            for (FloatSliderOptionWidget slider : sliders) {
                slider.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (super.mouseClicked(event, isDoubleClick)) {
            return true;
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            // Mode button
            if (isMouseOverButton(event.x(), event.y())) {
                this.playDownSound(McUtils.mc().getSoundManager());
                cycleMode();
                return true;
            }

            if (isExtended()) {
                for (FloatSliderOptionWidget slider : sliders) {
                    if (slider.mouseClicked(event, isDoubleClick)) {
                        return true;
                    }
                }
            }
        } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (isExtended()) {
                for (FloatSliderOptionWidget slider : sliders) {
                    if (slider.mouseClicked(event, isDoubleClick)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!isExtended()) return false;

        for (FloatSliderOptionWidget slider : sliders) {
            if (slider.mouseDragged(event, dragX, dragY)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (!isExtended()) return false;

        for (FloatSliderOptionWidget slider : sliders) {
            if (slider.mouseReleased(event)) return true;
        }
        return false;
    }

    private boolean isMouseOverButton(double mouseX, double mouseY) {
        int buttonX = getX() + getWidth() - BUTTON_WIDTH;
        int buttonY = getY() + (TOP_ROW_HEIGHT - BUTTON_HEIGHT) / 2;

        return MathUtils.isInside(
                (int) mouseX, (int) mouseY, buttonX, buttonX + BUTTON_WIDTH - 1, buttonY, buttonY + BUTTON_HEIGHT - 1);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private boolean isExtended() {
        return currentMode == Mode.CUSTOM;
    }

    private void cycleMode() {
        applyMode(currentMode.next());
    }

    private void applyMode(Mode mode) {
        this.currentMode = mode;

        switch (mode) {
            case NEVER -> setValue(neverVisibility);
            case ALWAYS -> setValue(alwaysVisibility);
            case CUSTOM -> {
                if (getValue() == null) {
                    setValue(alwaysVisibility);
                }
            }
        }

        updateSlidersFromValue();
        setHeight(isExtended() ? computeExtendedHeight() : TOP_ROW_HEIGHT);
    }

    private Mode determineMode(MapVisibility value) {
        if (isSameVisibility(value, neverVisibility)) {
            return Mode.NEVER;
        }
        if (isSameVisibility(value, alwaysVisibility)) {
            return Mode.ALWAYS;
        }

        return Mode.CUSTOM;
    }

    private int computeExtendedHeight() {
        return TOP_ROW_HEIGHT + SLIDER_PADDING + sliders.size() * (SLIDER_HEIGHT + SLIDER_PADDING);
    }

    private void updateSliderPositions() {
        int sliderY = getY() + TOP_ROW_HEIGHT + SLIDER_PADDING;

        for (FloatSliderOptionWidget slider : sliders) {
            slider.setX(getX());
            slider.setY(sliderY);
            slider.setWidth(getWidth());
            slider.setHeight(SLIDER_HEIGHT);
            sliderY += SLIDER_HEIGHT + SLIDER_PADDING;
        }
    }

    private void updateSlidersFromValue() {
        MapVisibility vis = getValue();
        if (vis == null) {
            minSlider.setValue(0f);
            maxSlider.setValue(100f);
            fadeSlider.setValue(0f);
            return;
        }

        minSlider.setValue(vis.getMin().orElse(0f));
        maxSlider.setValue(vis.getMax().orElse(100f));
        fadeSlider.setValue(vis.getFade().orElse(0f));
    }

    private void onSliderChanged() {
        if (currentMode == Mode.CUSTOM) {
            setValue(new MapVisibilityImpl(minSlider.getValue(), maxSlider.getValue(), fadeSlider.getValue()));
        }
    }

    private boolean isSameVisibility(MapVisibility a, MapVisibility b) {
        return a == b
                || (a != null
                        && Objects.equals(a.getMin(), b.getMin())
                        && Objects.equals(a.getMax(), b.getMax())
                        && Objects.equals(a.getFade(), b.getFade()));
    }

    @Override
    public void updateFromAttributes(
            Optional<MapAttributes> ownAttributes, Optional<MapAttributes> resolvedAttributes) {
        super.updateFromAttributes(ownAttributes, resolvedAttributes);

        for (FloatSliderOptionWidget slider : sliders) {
            slider.updateFromAttributes(ownAttributes, resolvedAttributes);
        }

        // Manually update this again, because resolvedattributes does not contain default attributes
        // and the sliders do resolve them.
        setValue(new MapVisibilityImpl(minSlider.getValue(), maxSlider.getValue(), fadeSlider.getValue()));

        this.currentMode = determineMode(getValue());
        setHeight(isExtended() ? computeExtendedHeight() : TOP_ROW_HEIGHT);
        generateTooltip();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        updateSliderPositions();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        updateSliderPositions();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        updateSliderPositions();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        updateSliderPositions();
    }

    @Override
    public void setValue(MapVisibility newValue) {
        value = newValue;
        generateTooltip();
    }

    @Override
    public void resetToDefault() {
        super.resetToDefault();

        minSlider.resetToDefault();
        maxSlider.resetToDefault();
        fadeSlider.resetToDefault();

        this.currentMode = determineMode(getValue());
        setHeight(isExtended() ? computeExtendedHeight() : TOP_ROW_HEIGHT);
        generateTooltip();
    }

    @Override
    public boolean isChanged() {
        return !isSameVisibility(value, loadedValue);
    }

    @Override
    public boolean isMouseOverTextBox(double mouseX, double mouseY) {
        if (!isExtended()) return false;

        for (FloatSliderOptionWidget slider : sliders) {
            if (slider.isMouseOverTextBox(mouseX, mouseY)) return true;
        }

        return false;
    }

    @Override
    public TextInputBoxWidget getTextInputBoxWidget(double mouseX, double mouseY) {
        if (!isExtended()) return null;

        for (FloatSliderOptionWidget slider : sliders) {
            if (slider.isMouseOverTextBox(mouseX, mouseY)) {
                return slider.getTextInputBoxWidget(mouseX, mouseY);
            }
        }

        return null;
    }

    @Override
    public List<Component> getTooltipLines(double mouseX, double mouseY) {
        if (isExtended()) {
            for (FloatSliderOptionWidget slider : sliders) {
                List<Component> lines = slider.getTooltipLines(mouseX, mouseY);

                if (!lines.isEmpty()) {
                    return lines;
                }
            }
        }

        int textWidth = FontRenderer.getInstance().getFont().width(getMessage().getString());
        int textHeight = FontRenderer.getInstance().getFont().lineHeight;
        int labelY = getY() + (TOP_ROW_HEIGHT - textHeight) / 2;

        boolean isTextHovered =
                MathUtils.isInside((int) mouseX, (int) mouseY, getX(), getX() + textWidth, labelY, labelY + textHeight);

        if (isTextHovered) {
            return Collections.unmodifiableList(this.generatedTooltip);
        }
        return new ArrayList<>();
    }

    @Override
    protected void generateTooltip() {
        this.generatedTooltip = new ArrayList<>();

        this.generatedTooltip.add(Component.empty().append(this.getMessage()).withStyle(ChatFormatting.GOLD));

        ChatFormatting color = this.inherited ? ChatFormatting.GRAY : ChatFormatting.WHITE;

        Component label = (this.inherited
                        ? Component.translatable(
                                "screens.wynntils.map.managers.categoryManager.abstractOptionWidget.inheritedText")
                        : Component.translatable(
                                "screens.wynntils.map.managers.categoryManager.abstractOptionWidget.notInheritedText"))
                .withStyle(color);

        this.generatedTooltip.add(
                Component.literal("- ").withStyle(ChatFormatting.GOLD).append(label));

        this.generatedTooltip.add(Component.empty());

        if (value != null) {
            if (value.getMin().isPresent()) {
                this.generatedTooltip.add(Component.literal("- ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(minSliderName.copy().withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(String.valueOf(value.getMin().get()))
                                .withStyle(ChatFormatting.WHITE)));
            }

            if (value.getMax().isPresent()) {
                this.generatedTooltip.add(Component.literal("- ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(maxSliderName.copy().withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(String.valueOf(value.getMax().get()))
                                .withStyle(ChatFormatting.WHITE)));
            }

            if (value.getFade().isPresent()) {
                this.generatedTooltip.add(Component.literal("- ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(fadeSliderName.copy().withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(String.valueOf(value.getFade().get()))
                                .withStyle(ChatFormatting.WHITE)));
            }
        }

        this.generatedTooltip.add(Component.empty());

        StyledText[] wrappedText = RenderedStringUtils.wrapTextBySize(StyledText.fromComponent(description), 200);

        for (StyledText text : wrappedText) {
            this.generatedTooltip.add(
                    Component.empty().append(text.getComponent()).withStyle(ChatFormatting.GRAY));
        }

        this.generatedTooltip.add(Component.empty());

        this.generatedTooltip.add(Component.empty()
                .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class))
                .append(" ")
                .append(Component.translatable(
                                "screens.wynntils.map.managers.categoryManager.abstractOptionWidget.rightClickText")
                        .withStyle(ChatFormatting.GREEN)));
    }

    @Override
    protected boolean isMouseOverLabel(double mouseX, double mouseY) {
        Font font = FontRenderer.getInstance().getFont();
        int textWidth = font.width(getMessage().getString());
        int textHeight = font.lineHeight;
        int textY = getY() + (TOP_ROW_HEIGHT - textHeight) / 2;
        return MathUtils.isInside((int) mouseX, (int) mouseY, getX(), getX() + textWidth, textY, textY + textHeight);
    }

    private enum Mode {
        NEVER(Component.translatable(
                "screens.wynntils.map.managers.categoryManager.visibilityOptionWidget.mode.never")),
        ALWAYS(Component.translatable(
                "screens.wynntils.map.managers.categoryManager.visibilityOptionWidget.mode.always")),
        CUSTOM(Component.translatable(
                "screens.wynntils.map.managers.categoryManager.visibilityOptionWidget.mode.custom"));

        private final Component displayName;

        Mode(Component displayName) {
            this.displayName = displayName;
        }

        Component getDisplayName() {
            return displayName;
        }

        Mode next() {
            Mode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
}
