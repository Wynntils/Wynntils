/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.overlays.objects;

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

public class OverlayPosition {
    private static final Pattern OVERLAY_POSITION_PATTERN =
            Pattern.compile("OverlayPosition\\[x=([0-9.]+),y=([0-9.]+),width=(\\d+),height=(\\d+),toggled=(\\w+)]");

    // Position along the x and y-axis as a percentage of the total screen width
    private float x;
    private float y;

    private int width;
    private int height;

    // Cached value for the x and y-axis position according to the current screen size
    private float drawingX;
    private float drawingY;

    private boolean toggled;

    public OverlayPosition(float x, float y, int width, int height, boolean toggled) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.toggled = toggled;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getCurrentX() {
        this.refresh();

        return drawingX;
    }

    public float getCurrentY() {
        this.refresh();

        return drawingY;
    }

    private void refresh() {
        Window screen = McUtils.mc().getWindow();

        drawingX = x * screen.getWidth();
        drawingY = y * screen.getHeight();
    }

    public boolean isToggled() {
        return toggled;
    }

    // Returns this to allow for method chaining
    public OverlayPosition setToggled(boolean toggled) {
        this.toggled = toggled;
        return this;
    }

    public OverlayPosition show() {
        setToggled(true);
        return this;
    }

    public OverlayPosition hide() {
        setToggled(false);
        return this;
    }

    public static OverlayPosition fromString(String string) {
        string = string.trim();
        Matcher stringMatcher = OVERLAY_POSITION_PATTERN.matcher(string);

        if (!stringMatcher.matches()) {
            Reference.LOGGER.error(string + "is not a valid OverlayPosition");
            return null;
        }

        return new OverlayPosition(
                Float.parseFloat(stringMatcher.group(1)),
                Float.parseFloat(stringMatcher.group(2)),
                Integer.parseInt(stringMatcher.group(3)),
                Integer.parseInt(stringMatcher.group(4)),
                Boolean.parseBoolean(stringMatcher.group(5)));
    }

    @Override
    public String toString() {
        return "OverlayPosition[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + ",toggled=" + toggled
                + "]";
    }

    public static class OverlayPositionSerializer
            implements JsonSerializer<OverlayPosition>, JsonDeserializer<OverlayPosition> {
        @Override
        public OverlayPosition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return OverlayPosition.fromString(json.getAsString());
        }

        @Override
        public JsonElement serialize(OverlayPosition src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.toString());
        }
    }
}
