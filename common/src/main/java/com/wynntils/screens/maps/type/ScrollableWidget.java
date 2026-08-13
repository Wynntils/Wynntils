package com.wynntils.screens.maps.type;

public interface ScrollableWidget<T> {
    int getHeight();
    T getValue();
    void setValue(T newValue);
    OptionCategory getCategory();
    boolean isVisible();
}