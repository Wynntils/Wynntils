/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.options;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.services.mapdata.attributes.DefaultMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public abstract class AbstractOptionWidget<T> extends AbstractWidget {
    protected final OptionCategory category;
    protected final T defaultValue;
    protected T value;
    private T loadedValue;
    protected Component description;
    protected List<Component> generatedTooltip = new ArrayList<>();

    protected boolean inherited;

    private final Function<MapAttributes, Optional<T>> valueGetter;

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

    public TextInputBoxWidget getTextInputBoxWidget() {
        return null;
    }

    public List<Component> getTooltipLines(double mouseX, double mouseY) {
        int textWidth = FontRenderer.getInstance().getFont().width(getMessage().getString());
        int textHeight = FontRenderer.getInstance().getFont().lineHeight;
        int heightStart = getY() + (this.height - textHeight) / 2;

        boolean isTextHovered = MathUtils.isInside(
                (int) mouseX, (int) mouseY, getX(), getX() + textWidth, heightStart, heightStart + textHeight);

        if (isTextHovered) {
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
    }

    public void updateFromAttributes(
            Optional<MapAttributes> ownAttributes, Optional<MapAttributes> resolvedAttributes) {
        Optional<T> ownValue = ownAttributes.flatMap(valueGetter);
        Optional<T> inheritedValue = resolvedAttributes.flatMap(valueGetter);

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
}
