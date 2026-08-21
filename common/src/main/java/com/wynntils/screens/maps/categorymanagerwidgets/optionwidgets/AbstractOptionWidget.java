package com.wynntils.screens.maps.categorymanagerwidgets.optionwidgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.type.OptionCategory;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractOptionWidget<T> extends AbstractWidget {
    protected final OptionCategory category;
    protected final T defaultValue;
    protected T value;
    private T loadedValue;
    private Optional<T> inheritedValue = Optional.empty();

    protected boolean inherited;

    private final Function<MapAttributes, Optional<T>> valueGetter;


    protected AbstractOptionWidget(
            String label,
            int height,
            OptionCategory category,
            T defaultValue,
            Function<MapAttributes, Optional<T>> valueGetter) {
        super(0, 0, 0, height, Component.literal(label));
        this.category = category;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.valueGetter = valueGetter;
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

    public void updateFromAttributes(Optional<MapAttributes> ownAttributes, Optional<MapAttributes> resolvedAttributes) {
        Optional<T> ownValue = ownAttributes.flatMap(valueGetter);
        inheritedValue = resolvedAttributes.flatMap(valueGetter);

        if (ownValue.isPresent()) {
            loadedValue = ownValue.get();
            setValue(ownValue.get());
            setInherited(false);
            return;
        }

        if (inheritedValue.isPresent()) {
            loadedValue = inheritedValue.get();
            setValue(inheritedValue.get());
            setInherited(true);
            return;
        }

        loadedValue = defaultValue;
        setValue(defaultValue);
        setInherited(true);
    }
}