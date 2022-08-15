/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.objects;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

public class CustomColor {
    public static final CustomColor NONE = new CustomColor(-1, -1, -1, -1);

    private static final Pattern HEX_PATTERN = Pattern.compile("#?([0-9a-fA-F]{6})");
    private static final Pattern STRING_PATTERN = Pattern.compile("rgba\\((\\d+),(\\d+),(\\d+),(\\d+)\\)");

    public int r, g, b, a;

    public CustomColor(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public CustomColor(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public CustomColor(float r, float g, float b) {
        this(r, g, b, 1f);
    }

    public CustomColor(float r, float g, float b, float a) {
        this.r = (int) (r * 255);
        this.g = (int) (g * 255);
        this.b = (int) (b * 255);
        this.a = (int) (a * 255);
    }

    public CustomColor(String toParse) {
        String noSpace = toParse.replaceAll(" ", "");

        CustomColor parseTry = CustomColor.fromString(noSpace);

        if (parseTry == CustomColor.NONE) {
            parseTry = CustomColor.fromHexString(noSpace);

            if (parseTry == CustomColor.NONE) {
                throw new RuntimeException("Failed to parse CustomColor");
            }
        }

        this.r = parseTry.r;
        this.g = parseTry.g;
        this.b = parseTry.b;
        this.a = parseTry.a;
    }

    public CustomColor(CustomColor color) {
        this(color.r, color.g, color.b, color.a);
    }

    public static CustomColor fromChatFormatting(ChatFormatting cf) {
        return fromInt(cf.getColor() | 0xFF000000);
    }

    /** 0xAARRGGBB format */
    public static CustomColor fromInt(int num) {
        return new CustomColor(num >> 16 & 255, num >> 8 & 255, num & 255, num >> 24 & 255);
    }

    /** "#rrggbb" or "rrggbb" */
    public static CustomColor fromHexString(String hex) {
        Matcher hexMatcher = HEX_PATTERN.matcher(hex.trim());

        // invalid format
        if (!hexMatcher.matches()) return CustomColor.NONE;

        // parse hex
        return fromInt(Integer.parseInt(hexMatcher.group(1), 16)).setAlpha(255);
    }

    /** "rgba(r,g,b,a)" format as defined in toString() */
    public static CustomColor fromString(String string) {
        Matcher stringMatcher = STRING_PATTERN.matcher(string.trim());

        // invalid format
        if (!stringMatcher.matches()) return CustomColor.NONE;

        return new CustomColor(
                Integer.parseInt(stringMatcher.group(1)),
                Integer.parseInt(stringMatcher.group(2)),
                Integer.parseInt(stringMatcher.group(3)),
                Integer.parseInt(stringMatcher.group(4)));
    }

    public CustomColor setAlpha(int a) {
        this.a = a;
        return this;
    }

    public CustomColor setAlpha(float a) {
        this.a = (int) (a * 255);
        return this;
    }

    public CustomColor withAlpha(int a) {
        return new CustomColor(this).setAlpha(a);
    }

    public CustomColor withAlpha(float a) {
        return new CustomColor(this).setAlpha(a);
    }

    /** 0xAARRGGBB format */
    public int asInt() {
        int a = Math.min(this.a, 255);
        int r = Math.min(this.r, 255);
        int g = Math.min(this.g, 255);
        int b = Math.min(this.b, 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** #rrggbb format */
    public String toHexString() {
        String hex = Integer.toHexString(this.asInt());
        // whether alpha portion is 1 digit or 2
        hex = (hex.length() > 7) ? hex.substring(2) : hex.substring(1);
        hex = "#" + hex;

        return hex;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CustomColor color)) return false;

        // colors are equal as long as rgba values match
        return (this.r == color.r && this.g == color.g && this.b == color.b && this.a == color.a);
    }

    @Override
    public String toString() {
        return "rgba(" + r + "," + g + "," + b + "," + a + ")";
    }

    public static class CustomColorSerializer implements JsonSerializer<CustomColor>, JsonDeserializer<CustomColor> {
        @Override
        public CustomColor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return CustomColor.fromString(json.getAsString());
        }

        @Override
        public JsonElement serialize(CustomColor src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.toString());
        }
    }
}
