/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.options;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.services.mapdata.attributes.DefaultMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractOptionWidget<T> extends AbstractWidget {
    protected final OptionCategory category;
    protected final T defaultValue;
    protected T value;
    protected T loadedValue;
    protected Component description;
    protected List<Component> generatedTooltip = new ArrayList<>();
    protected final Function<MapAttributes, Optional<T>> valueGetter;

    private Optional<T> ownValue;
    protected Optional<T> inheritedValue;

    protected boolean inherited;

    protected AbstractOptionWidget(
            Component label,
            Component description,
            int height,
            OptionCategory category,
            Function<MapAttributes, Optional<T>> valueGetter) {
        super(0, 0, 0, height, label);
        this.description = description;
        this.category = category;
        this.valueGetter = valueGetter;

        if (valueGetter != null) {
            this.defaultValue = valueGetter.apply(DefaultMapAttributes.INSTANCE).get();
            this.value = defaultValue;
        } else {
            this.defaultValue = null;
            this.value = null;
        }
    }

    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        value = newValue;
    }

    public void resetToDefault() {
        T newValue = inheritedValue.orElse(defaultValue);
        setValue(newValue);
        generateTooltip();
    }

    public boolean isOverridden() {
        T effectiveInherited =
                (inheritedValue != null && inheritedValue.isPresent()) ? inheritedValue.get() : defaultValue;
        return !Objects.equals(value, effectiveInherited);
    }

    public OptionCategory getCategory() {
        return category;
    }

    public void setInherited(boolean value) {
        this.inherited = value;
    }

    public boolean isInherited() {
        return inherited;
    }

    public boolean isChanged() {
        return !Objects.equals(value, loadedValue);
    }

    public boolean isMouseOverTextBox(double mouseX, double mouseY) {
        return false;
    }

    public TextInputBoxWidget getTextInputBoxWidget(double mouseX, double mouseY) {
        return null;
    }

    public List<Component> getTooltipLines(double mouseX, double mouseY) {
        if (isMouseOverLabel(mouseX, mouseY)) {
            return Collections.unmodifiableList(this.generatedTooltip);
        }

        return new ArrayList<>();
    }

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

    public void updateFromAttributes(
            Optional<MapAttributes> ownAttributes, Optional<MapAttributes> resolvedAttributes) {
        ownValue = ownAttributes.flatMap(valueGetter);
        inheritedValue = resolvedAttributes.flatMap(valueGetter);

        if (ownValue.isPresent()) {
            loadedValue = ownValue.get();
            setValue(ownValue.get());
            setInherited(false);
            generateTooltip();
            return;
        }

        if (inheritedValue.isPresent()) {
            loadedValue = inheritedValue.get();
            setValue(inheritedValue.get());
            setInherited(true);
            generateTooltip();
            return;
        }

        loadedValue = defaultValue;
        setValue(defaultValue);
        setInherited(true);
        generateTooltip();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && isMouseOverLabel(event.x(), event.y())) {
            resetToDefault();
            this.playDownSound(McUtils.mc().getSoundManager());
            return true;
        }
        return false;
    }

    protected boolean isMouseOverLabel(double mouseX, double mouseY) {
        Font font = FontRenderer.getInstance().getFont();
        int textWidth = font.width(getMessage().getString());
        int textHeight = font.lineHeight;
        int textY = getY() + (this.height - textHeight) / 2;

        return MathUtils.isInside((int) mouseX, (int) mouseY, getX(), getX() + textWidth, textY, textY + textHeight);
    }
}
