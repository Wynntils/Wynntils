package com.wynntils.screens.maps.type;

import com.wynntils.screens.base.widgets.TextInputBoxWidget;

public interface ScrollableWidget<T> {
    int getHeight();
    T getValue();
    void setValue(T newValue);
    OptionCategory getCategory();
    boolean isVisible();
    default boolean isMouseOverTextBox(double mouseX, double mouseY) {
        return false;
    };
    default TextInputBoxWidget getTextInputBoxWidget() {
        return null;
    };
}