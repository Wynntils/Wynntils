/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.objects;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.Reference;
import com.wynntils.mc.utils.McUtils;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Overlay {
    private static final Pattern OVERLAY_PATTERN =
            Pattern.compile("Overlay\\[x=([0-9.]+),y=([0-9.]+),width=(\\d+),height=(\\d+),enabled=(\\w+)]");

    // Position along the x and y-axis as a percentage of the total screen width
    private double x;
    private double y;

    private int width;
    private int height;

    // Cached value for the x and y-axis position according to the current screen size
    private double drawingX;
    private double drawingY;

    private boolean enabled;

    public Overlay(double x, double y, int width, int height, boolean enabled) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.enabled = enabled;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getCurrentX() {
        this.refresh();

        return drawingX;
    }

    public double getCurrentY() {
        this.refresh();

        return drawingY;
    }

    private void refresh() {
        Window screen = McUtils.mc().getWindow();

        drawingX = x * screen.getWidth();
        drawingY = y * screen.getHeight();
    }

    public boolean isEnabled() {
        return enabled;
    }

    // Returns this to allow for method chaining
    public Overlay setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Overlay show() {
        setEnabled(true);
        return this;
    }

    public Overlay hide() {
        setEnabled(false);
        return this;
    }

    public static Overlay fromString(String string) {
        string = string.trim();
        Matcher stringMatcher = OVERLAY_PATTERN.matcher(string);

        if (!stringMatcher.matches()) {
            Reference.LOGGER.error(string + "is not a valid Overlay");
            return null;
        }

        return new Overlay(
                Float.parseFloat(stringMatcher.group(1)),
                Float.parseFloat(stringMatcher.group(2)),
                Integer.parseInt(stringMatcher.group(3)),
                Integer.parseInt(stringMatcher.group(4)),
                Boolean.parseBoolean(stringMatcher.group(5)));
    }

    @Override
    public String toString() {
        return "Overlay[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + ",enabled=" + enabled + "]";
    }

    public static class OverlayPositionSerializer implements JsonSerializer<Overlay>, JsonDeserializer<Overlay> {
        @Override
        public Overlay deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return Overlay.fromString(json.getAsString());
        }

        @Override
        public JsonElement serialize(Overlay src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.toString());
        }
    }
}
