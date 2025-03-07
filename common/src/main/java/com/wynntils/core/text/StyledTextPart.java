/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text;

import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public final class StyledTextPart {
    private static final Pattern CLASS_PATTERN = Pattern.compile("class=['\"](.*?)['\"]");
    private static final Pattern STYLE_PATTERN = Pattern.compile("style=['\"](.*?)['\"]");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(<span[^>]*>|</span>|[^<]+)");
    private final String text;
    private final PartStyle style;

    private final StyledText parent;

    public StyledTextPart(String text, Style style, StyledText parent, Style parentStyle) {
        this.parent = parent;
        this.text = text;

        // Must be done last
        this.style = PartStyle.fromStyle(style, this, parentStyle);
    }

    StyledTextPart(StyledTextPart part, StyledText parent) {
        this.text = part.text;
        this.style = new PartStyle(part.style, this);
        this.parent = parent;
    }

    private StyledTextPart(StyledTextPart part, PartStyle style, StyledText parent) {
        this.text = part.text;
        this.style = style;
        this.parent = parent;
    }

    // This factory is used to create a StyledTextPart from a component that has formatting codes
    // It is separate from the constructor because this only needs to be applied in cases there the text could have
    // formatting codes
    static List<StyledTextPart> fromCodedString(String codedString, Style style, StyledText parent, Style parentStyle) {
        // When we have a style, but the text has formatting codes,
        // we need to apply the formatting codes to the style
        // This means that the actual style applies first; then the formatting codes
        List<StyledTextPart> parts = new ArrayList<>();

        Style currentStyle = style;
        StringBuilder currentString = new StringBuilder();

        boolean nextIsFormatting = false;
        StringBuilder hexColorFormatting = new StringBuilder();

        // []
        boolean clickEventPrefix = false;
        // <>
        boolean hoverEventPrefix = false;
        String eventIndexString = "";

        for (char current : codedString.toCharArray()) {
            if (nextIsFormatting) {
                nextIsFormatting = false;

                // Only parse events, if we have a parent
                if (parent != null) {
                    if (current == '[') {
                        clickEventPrefix = true;
                        continue;
                    }
                    if (current == '<') {
                        hoverEventPrefix = true;
                        continue;
                    }
                }

                // It looks like we have a hex color code
                if (current == '#') {
                    hexColorFormatting.append(current);
                    continue;
                }

                ChatFormatting formatting = ChatFormatting.getByCode(current);

                if (formatting == null) {
                    currentString.append(ChatFormatting.PREFIX_CODE);
                    currentString.append(current);
                    continue;
                }

                // If we already had some text with the current style
                // Append it before modifying the style
                if (!currentString.isEmpty()) {
                    if (style != Style.EMPTY) {
                        // We might have lost an event, so we need to add it back
                        currentStyle = currentStyle
                                .withClickEvent(style.getClickEvent())
                                .withHoverEvent(style.getHoverEvent());
                    }
                    // But if the style is empty, we might have parsed events from the string itself

                    parts.add(new StyledTextPart(currentString.toString(), currentStyle, null, parentStyle));

                    // reset string
                    // style is not reset, because we want to keep the formatting
                    currentString = new StringBuilder();
                }

                // Color formatting resets the style
                if (formatting.isColor()) {
                    currentStyle = Style.EMPTY.withColor(formatting);
                    continue;
                }

                currentStyle = currentStyle.applyFormat(formatting);

                continue;
            }

            // If we are parsing an event, handle it
            if (clickEventPrefix || hoverEventPrefix) {
                if (Character.isDigit(current)) {
                    eventIndexString += current;
                    continue;
                }

                // This is set to true if we have overwritten the current style's event
                Style oldStyle = null;

                if (clickEventPrefix && current == ']') {
                    ClickEvent clickEvent = parent.getClickEvent(Integer.parseInt(eventIndexString));

                    if (clickEvent != null) {
                        oldStyle = currentStyle;

                        currentStyle = currentStyle.withClickEvent(clickEvent);
                        clickEventPrefix = false;
                        eventIndexString = "";
                    }
                }

                if (hoverEventPrefix && current == '>') {
                    HoverEvent hoverEvent = parent.getHoverEvent(Integer.parseInt(eventIndexString));

                    if (hoverEvent != null) {
                        oldStyle = currentStyle;

                        currentStyle = currentStyle.withHoverEvent(hoverEvent);
                        hoverEventPrefix = false;
                        eventIndexString = "";
                    }
                }

                if (oldStyle != null) {
                    // If we already had some text with the current style
                    // Append it before modifying the style
                    if (!currentString.isEmpty()) {
                        if (style != Style.EMPTY) {
                            // We might have lost an event, so we need to add it back
                            // (theoretically this case can't happen at this location)
                            currentStyle = currentStyle
                                    .withClickEvent(style.getClickEvent())
                                    .withHoverEvent(style.getHoverEvent());
                        }
                        // But if the style is empty, we might have parsed events from the string itself

                        parts.add(new StyledTextPart(currentString.toString(), oldStyle, null, parentStyle));

                        // reset string
                        // style is not reset, because we want to keep the formatting
                        currentString = new StringBuilder();
                    }

                    // Even if we did not add a new part, we've parsed an event
                    continue;
                }

                // The event was not formatted properly, so add it as a string
                currentString.append(clickEventPrefix ? '[' : '<');
                currentString.append(eventIndexString);
                currentString.append(current);

                // Reset the related variables
                clickEventPrefix = false;
                hoverEventPrefix = false;
                eventIndexString = "";
                continue;
            }

            if (!hexColorFormatting.isEmpty()) {
                hexColorFormatting.append(current);

                // StyledText#getString() always uses full hex representation,
                // if the color is not a ChatFormatting color (#rrggbbaa)
                if (hexColorFormatting.length() == 9) {
                    CustomColor customColor = CustomColor.fromHexString(hexColorFormatting.toString());

                    // If the color is invalid, we just append the hex formatting as text
                    if (customColor == CustomColor.NONE) {
                        currentString.append(hexColorFormatting);
                    } else if (!currentString.isEmpty()) {
                        // If we already had some text with the current style
                        // Append it before modifying the style
                        if (style != Style.EMPTY) {
                            // We might have lost an event, so we need to add it back
                            currentStyle = currentStyle
                                    .withClickEvent(style.getClickEvent())
                                    .withHoverEvent(style.getHoverEvent());
                        }
                        // But if the style is empty, we might have parsed events from the string itself

                        parts.add(new StyledTextPart(currentString.toString(), currentStyle, null, parentStyle));

                        // reset string
                        // style is not reset, because we want to keep the formatting
                        currentString = new StringBuilder();
                    }

                    currentStyle = currentStyle.withColor(customColor.asInt());
                    hexColorFormatting = new StringBuilder();
                }

                continue;
            }

            if (current == ChatFormatting.PREFIX_CODE) {
                nextIsFormatting = true;
                continue;
            }

            currentString.append(current);
        }

        // Check if we have some text left
        if (!currentString.isEmpty()) {
            if (style != Style.EMPTY) {
                // We might have lost an event, so we need to add it back
                currentStyle =
                        currentStyle.withClickEvent(style.getClickEvent()).withHoverEvent(style.getHoverEvent());
            }
            parts.add(new StyledTextPart(currentString.toString(), currentStyle, null, parentStyle));
        }

        return parts;
    }

    // This will convert HTML text to StyledTextParts based on the rules from the API documentation
    // located at https://docs.wynncraft.com/docs/#api-markup-parser
    static List<StyledTextPart> fromHtmlString(String htmlString) {
        if (htmlString.trim().equals("</br>")) {
            return List.of(new StyledTextPart("", Style.EMPTY, null, Style.EMPTY));
        }

        List<StyledTextPart> parts = new ArrayList<>();
        Deque<Style> styles = new ArrayDeque<>();
        styles.push(Style.EMPTY);
        Style currentStyle = Style.EMPTY;

        StringBuilder plainText = new StringBuilder();

        Matcher matcher = TOKEN_PATTERN.matcher(htmlString);

        while (matcher.find()) {
            String token = matcher.group();

            if (token.startsWith("<span")) {
                if (!plainText.isEmpty()) {
                    parts.add(new StyledTextPart(plainText.toString(), currentStyle, null, Style.EMPTY));
                    plainText = new StringBuilder();
                }

                Style parentStyle = styles.peek();
                Style newStyle = Style.EMPTY.withColor(parentStyle.getColor());

                Matcher classMatcher = CLASS_PATTERN.matcher(token);
                if (classMatcher.find()) {
                    String fontName = classMatcher.group(1).trim();
                    ResourceLocation font =
                            switch (fontName) {
                                case "font-ascii", "font-default" -> ResourceLocation.withDefaultNamespace("default");
                                case "font-common" -> ResourceLocation.withDefaultNamespace("common");
                                case "font-five" -> ResourceLocation.withDefaultNamespace("language/five");
                                case "font-wynnic" -> ResourceLocation.withDefaultNamespace("language/wynnic");
                                case "font-high_gavelian" ->
                                    ResourceLocation.withDefaultNamespace("language/high_gavelian");
                                default -> {
                                    WynntilsMod.warn("Unknown font in HTML parsing: " + fontName);
                                    yield ResourceLocation.withDefaultNamespace("default");
                                }
                            };
                    newStyle = newStyle.withFont(font);
                }

                Matcher styleMatcher = STYLE_PATTERN.matcher(token);
                if (styleMatcher.find()) {
                    String style = styleMatcher.group(1);

                    for (String styleEntry : style.split(";")) {
                        styleEntry = styleEntry.trim();

                        if (styleEntry.isEmpty()) continue;

                        String[] stylePair = styleEntry.split(":");
                        String styleKey = stylePair[0].trim();
                        String styleValue = stylePair[1].trim();

                        switch (styleKey) {
                            case "text-decoration" -> {
                                if (styleValue.equals("underline")) {
                                    newStyle = newStyle.withUnderlined(true);
                                } else if (styleValue.equals("line-through")) {
                                    newStyle = newStyle.withStrikethrough(true);
                                } else {
                                    WynntilsMod.warn("Unknown text decoration type in HTML parsing: " + styleValue);
                                }
                            }
                            case "font-style" -> {
                                if (styleValue.equals("italic")) {
                                    newStyle = newStyle.withItalic(true);
                                } else {
                                    WynntilsMod.warn("Unknown font style type in HTML parsing: " + styleValue);
                                }
                            }
                            case "font-weight" -> {
                                if (styleValue.equals("bolder")) {
                                    newStyle = newStyle.withBold(true);
                                } else {
                                    WynntilsMod.warn("Unknown font weight type in HTML parsing: " + styleValue);
                                }
                            }
                            case "color" -> {
                                newStyle = newStyle.withColor(
                                        CustomColor.fromHexString(styleValue).asInt());
                            }
                            case "margin-left" -> {
                                if (styleValue.equals("7.5px")) {
                                    // FIXME: Currently a guess, there are no items with this margin but it is
                                    // a possible value according to the docs
                                    plainText.append("À");
                                } else if (styleValue.equals("20px")) {
                                    plainText.append("ÀÀÀÀ");
                                } else {
                                    WynntilsMod.warn("Unexpected margin-left in HTML parsing: " + styleValue);
                                }
                            }
                            default -> {
                                WynntilsMod.warn("Unknown style type in HTML parsing: " + styleKey);
                            }
                        }
                    }
                }

                styles.push(newStyle);
                currentStyle = newStyle;
            } else if (token.startsWith("</span>")) {
                if (!plainText.isEmpty()) {
                    parts.add(new StyledTextPart(plainText.toString(), currentStyle, null, Style.EMPTY));
                    plainText = new StringBuilder();
                }

                if (!styles.isEmpty()) {
                    styles.pop();
                }

                Style parentStyle = styles.isEmpty() ? Style.EMPTY : styles.peek();
                if (!parentStyle.equals(currentStyle)) {
                    parts.add(new StyledTextPart("", parentStyle, null, Style.EMPTY));
                }

                currentStyle = parentStyle;
            } else {
                plainText.append(token);
            }
        }

        if (!plainText.isEmpty()) {
            parts.add(new StyledTextPart(plainText.toString(), currentStyle, null, Style.EMPTY));
        }

        return parts;
    }

    public String getString(PartStyle previousStyle, PartStyle.StyleType type) {
        return style.asString(previousStyle, type) + text;
    }

    public StyledText getParent() {
        return parent;
    }

    public PartStyle getPartStyle() {
        return style;
    }

    public StyledTextPart withStyle(PartStyle style) {
        return new StyledTextPart(this, style, parent);
    }

    public StyledTextPart withStyle(Function<PartStyle, PartStyle> function) {
        return withStyle(function.apply(style));
    }

    public MutableComponent getComponent() {
        return Component.literal(text).withStyle(style.getStyle());
    }

    StyledTextPart asNormalized() {
        return new StyledTextPart(WynnUtils.normalizeBadString(text), style.getStyle(), parent, null);
    }

    StyledTextPart stripLeading() {
        return new StyledTextPart(text.stripLeading(), style.getStyle(), parent, null);
    }

    StyledTextPart stripTrailing() {
        return new StyledTextPart(text.stripTrailing(), style.getStyle(), parent, null);
    }

    boolean isEmpty() {
        return text.isEmpty();
    }

    boolean isBlank() {
        return text.isBlank();
    }

    public int length() {
        return text.length();
    }

    @Override
    public String toString() {
        return "StyledTextPart[" + "text=" + text + ", " + "style=" + style + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StyledTextPart that = (StyledTextPart) o;
        return Objects.equals(text, that.text) && Objects.equals(style, that.style);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, style);
    }
}
