/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.arguments;

import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.NamedValue;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.type.Time;
import java.util.List;

public class Argument<T> {
    protected static final List<Class<?>> SUPPORTED_ARGUMENT_TYPES = List.of(
            String.class,
            Boolean.class,
            Integer.class,
            Long.class,
            Double.class,
            Number.class,
            CustomColor.class,
            CappedValue.class,
            RangedValue.class,
            NamedValue.class,
            Location.class,
            Time.class);

    private final String name;
    private final Class<T> type;
    private final T defaultValue;

    private T value;

    public Argument(String name, Class<T> type, T defaultValue) {
        this(name, type, defaultValue, true);
    }

    protected Argument(String name, Class<T> type, T defaultValue, boolean check) {
        if (check) {
            if (!SUPPORTED_ARGUMENT_TYPES.contains(type)) {
                throw new IllegalArgumentException("Unsupported argument type: " + type);
            }
        }

        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @SuppressWarnings("unchecked")
    protected void setValue(Object value) {
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException("Value is not of type " + type.getSimpleName() + ".");
        }

        this.value = (T) value;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    public T getValue() {
        return this.value == null ? this.defaultValue : this.value;
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }

    protected <U> U getValueChecked(Class<U> assumedType) {
        if (!assumedType.equals(type)) {
            throw new IllegalStateException(
                    "Argument is a " + type.getSimpleName() + ", not a " + assumedType.getSimpleName() + ".");
        }

        return assumedType.cast(getValue());
    }

    public Boolean getBooleanValue() {
        return getValueChecked(Boolean.class);
    }

    public Integer getIntegerValue() {
        if (this.type == Number.class) {
            return getValueChecked(Number.class).intValue();
        }

        return getValueChecked(Integer.class);
    }

    public Long getLongValue() {
        if (this.type == Number.class) {
            return getValueChecked(Number.class).longValue();
        }

        return getValueChecked(Long.class);
    }

    public Double getDoubleValue() {
        if (this.type == Number.class) {
            return getValueChecked(Number.class).doubleValue();
        }

        return getValueChecked(Double.class);
    }

    public CappedValue getCappedValue() {
        return getValueChecked(CappedValue.class);
    }

    public CustomColor getColorValue() {
        return getValueChecked(CustomColor.class);
    }

    public RangedValue getRangedValue() {
        return getValueChecked(RangedValue.class);
    }

    public NamedValue getNamedValue() {
        return getValueChecked(NamedValue.class);
    }

    public Location getLocation() {
        return getValueChecked(Location.class);
    }

    public Time getTime() {
        return (Time) this.getValue();
    }

    public String getStringValue() {
        return getValueChecked(String.class);
    }

    protected <U> List<U> getList(Class<U> assumedType) {
        // To store a list, ListArgument must be used
        throw new IllegalStateException("Argument is not a List.");
    }

    public List<Boolean> getBooleanList() {
        return getList(Boolean.class);
    }

    public List<Number> getNumberList() {
        return getList(Number.class);
    }

    public List<String> getStringList() {
        return getList(String.class);
    }
}
