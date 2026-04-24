/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class DialogueUtils {
    private static final int maxTextWidth = 234;
    private static final char specialChar = '\uDAFF';
    private static final char zeroWidthChar = '\uE000';
    private static final char normalStartPosChar = '\uDF8C';

    /**
     * what to do if the text is too long for the hud? Cut off or append?
     * */
    private static final boolean append = true;

    private static final FontDescription[] font_body = new FontDescription[5];
    private static final Style[] style_body = new Style[5];
    private static final FontDescription[] font_choice = new FontDescription[4];
    private static final FontDescription font_nameplate =
            new FontDescription.Resource(Identifier.parse("minecraft:hud/dialogue/text/nameplate"));
    private static final FontDescription font_picture =
            new FontDescription.Resource(Identifier.parse("minecraft:hud/dialogue/portrait"));
    private static final FontDescription font_press_shift =
            new FontDescription.Resource(Identifier.parse("minecraft:hud/dialogue/text/control"));

    static {
        for (int i = 0; i < font_body.length; i++) {
            font_body[i] =
                    new FontDescription.Resource(Identifier.parse("minecraft:hud/dialogue/text/wynncraft/body_" + i));
            style_body[i] = Style.EMPTY.withFont(font_body[i]);
        }
        for (int i = 0; i < font_choice.length; i++) {
            font_choice[i] =
                    new FontDescription.Resource(Identifier.parse("minecraft:hud/dialogue/text/wynncraft/choice_" + i));
        }
    }

    /**
     * Extracts the actual dialogue text from a {@link Component}
     * (the root component of the Dialogue HUD).
     * <p>
     * It searches for the sibling that uses the dialogue font
     * ({@code "hud/dialogue/text/wynncraft/body_0"}) and returns its cleaned content. All special
     * markers are removed and replaced by normal spaces. (Keeps Colors as color codes with {@code "§x"})
     * </p>
     * And some more usefull values.
     * @param component the dialogue component to read from
     * @return the cleaned dialogue text wrapped in {@link Content}.
     */
    public static Content getDialogueContent(Component component, boolean keepColors) {
        Content out = new Content();

        for (Component sibling : component.getSiblings()) {
            if (sibling.getStyle().getFont().equals(font_body[0])) {
                out.setText(getCleanText(sibling, keepColors));
                out.setCleanText(keepColors ? getCleanText(sibling, false) : out.getText());
                out.setStartPos(sibling.getString(2));
            } else if (sibling.getStyle().getFont().equals(font_nameplate)) {
                // could be usefull for later or some APIs / Mod-Addons
                out.setName(getCleanText(sibling, false));
            } else if (sibling.getStyle().getFont().equals(font_picture)) {
                String value = sibling.getString();
                if (value.length() == 5) {
                    // Example: "\uDAFF\uDF68\uE1C3\uDB00\uDC67"
                    // The first 2 are the positioning, the third is the pic itelf and the last two are width modifiers
                    // look in wynnpack at minecraft:font/hud/dialogue/portrait.json
                    value = value.substring(2, value.length() - 2);
                    out.setPortrait(value);
                }
            } else if (sibling.getStyle().getFont().equals(font_press_shift)) {
                out.setConfirmationless();
            } else {
                // minecraft:hud/dialogue/text/wynncraft/choice_X
                for (int i = 0; i < font_choice.length; i++) {
                    if (sibling.getStyle().getFont().equals(font_choice[i])) {
                        out.getChoices()[i] = getCleanText(sibling, false);
                        break;
                    }
                }
            }
        }
        return out;
    }

    /**
     * Get clean Text out of the siblings from the dialogue component <br />
     * Example: <br />
     * "󏾌It's a base filled with sky󏼝pirates!󐁓" -> "It's a base filled with sky pirates!" <br />
     *
     * @param keepColors should Chatformating like §3§r from Component.style be included in the Text?
     * @return a String with all special chars starting with {@code '/uDAFF'} and double {@code ' '} removed
     * */
    public static String getCleanText(Component sibling, boolean keepColors) {
        String text = keepColors ? StyledText.fromComponent(sibling).getString() : sibling.getString();

        if (text.length() <= 4) {
            return "";
        }

        text = text.substring(2, text.length() - 2);
        text = text.trim();
        StringBuilder output = new StringBuilder();
        boolean skipNext = false;
        char lastChar = 0;

        // looks worse than Pattern.compile, but is faster
        // "/uDAFF/u1234" -> " " and "  " -> " "
        for (char c : text.toCharArray()) {
            if (skipNext) {
                skipNext = false;
                continue;
            }
            if (c == specialChar) {
                if (lastChar != ' ') {
                    output.append(" ");
                    lastChar = ' ';
                }
                skipNext = true;
                continue;
            }
            if (c == ' ' && lastChar == ' ') {
                continue;
            }
            lastChar = c;
            output.append(c);
        }
        return output.toString();
    }

    /**
     * Inserts the dialogue body text in a given {@link Component}
     * while preserving all other siblings (e.g. headers, icons, background, coordinates, etc.).
     * <p>
     * <ul>
     *   <li>The translated {@code text} is automatically wrapped into a maximum of 5 lines
     *       using {@link #splitTextIntoLines(String, int)}.</li>
     *   <li>Every line and the final message are passed through {@code manageWidth(...)} to ensure
     *       correct rendering alignment.</li>
     * </ul>
     * <p>
     * All other siblings are copied unchanged.
     *
     * @param component the original dialogue {@link Component} (the
     *                  root component received from the {@link net.minecraft.network.protocol.game.ClientboundSystemChatPacket})
     * @param text      the raw dialogue text to be inserted (will be split into max. 5 lines)
     * @param startPos  the special char, that indicates the rendering start position
     *                  (with or without portrait has a different starting point).
     * @return a new {@link MutableComponent} with the dialogue text replaced
     * and all other parts preserved or null if input was null
     */
    public static MutableComponent insertDialogueText(Component component, String text, String startPos) {
        if (component == null) {
            return null;
        }
        MutableComponent modified = Component.literal("");

        // split a long text into max 5 lines with the right Render-Width
        int adjustWidth = (int) startPos.charAt(1) - (int) normalStartPosChar;
        ArrayList<String> lines = splitTextIntoLines(text, adjustWidth);

        // Insert text to Dialogue Hud
        // But only replace everything under "hud/dialogue/text/wynncraft/body_0"
        for (Component sib : component.getSiblings()) {
            if (sib.getStyle().getFont().equals(font_body[0])) {
                // start formatting character
                MutableComponent newMsg =
                        Component.literal(startPos).withStyle(sib.getStyle()).withStyle(style_body[0]);

                // add the lines
                MutableComponent line;
                MutableComponent temp;
                for (int i = 0; i < lines.size() && i < 5; i++) {
                    line = Component.empty();
                    temp = StyledText.fromString(lines.get(i)).getComponent();
//                    line = Component.literal(lines.get(i)).withStyle(style_body[i]);

                    for (Component part : temp.getSiblings())  {
                        if (part instanceof MutableComponent mutableContent) {
                            line.append(mutableContent.withStyle(style_body[i]));
                        }
                    }

                    line.withStyle(style_body[i]);
                    line = manageWidth(line);

                    newMsg.append(line);
                }

                newMsg = manageWidth(newMsg);
                modified.append(newMsg);
            } else {
                modified.append(sib.copy());
            }
        }
        return modified;
    }

    /**
     * Splits a long text string into multiple lines based on the render width
     * calculated by Minecraft's text renderer using the provided {@link Style}.
     * <p>
     * It prefers to break lines at spaces (word boundaries)
     * whenever possible. The output is limited to a maximum of 5 lines; any
     * remaining text after the 5th line is appended to the last line.
     *
     * @param textLong the long text to be wrapped into lines
     * @param style    the {@link Style} used for width calculation
     *                 (font, formatting, etc.)
     * @return an {@link ArrayList} containing the resulting lines
     *         (never more than 5 entries)
     */
    private static ArrayList<String> splitTextIntoLines(String textLong, int adjustWidth) {
        ArrayList<String> lines = new ArrayList<>();
        StyleState styleState = new StyleState();
        String remaining = textLong;

        while (!remaining.isEmpty()) {
            // don't make more than 5 lines!
            if (lines.size() == 5) {
                /*
                 * FIXME: fix the problem if text is too long.
                 *  Keep in mind, this method will get called multiple times, because the packet changes when "Press Shift to..." is blinking
                 *  Maybe someone can make a custom overlay instead of using the wynncraft overlay?
                 * */
                // cut off or append the remaining text
                if (append) {
                    lines.set(4, lines.get(4) + " " + remaining);
                }
                break;
            }

            // if remaining is short enough, add it and all is done
            if (getRenderWidth(Component.literal(remaining).withStyle(style_body[0])) <= maxTextWidth - adjustWidth) {
                lines.add(styleState.getPrefix() + remaining);
                break;
            }

            int splitIndex = findBestSplitIndex(remaining, style_body[0], maxTextWidth - adjustWidth);

            // safety: never split 0 or broken
            if (splitIndex <= 0) {
                splitIndex = 1;
            }

            String line = remaining.substring(0, splitIndex).stripTrailing();
            line = styleState.getPrefix() + line;
            styleState.consume(line);
            lines.add(line);

            remaining = remaining.substring(splitIndex).stripLeading();
        }
        return lines;
    }

    private static int findBestSplitIndex(String text, Style style, int maxWidth) {
        int low = 0;
        int high = text.length();

        while (low < high) {
            int mid = (low + high + 1) >>> 1;
            String candidate = text.substring(0, mid);

            int width = getRenderWidth(Component.literal(candidate).withStyle(style));
            if (width <= maxWidth) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }

        int bestFit = low;

        // try to split at last space
        int lastSpace = text.lastIndexOf(' ', bestFit);
        if (lastSpace > 0) {
            return lastSpace;
        }

        // no space found, so hard split
        return bestFit;
    }

    /**
     * Returns the rendered width (in pixels) of the given text {@link Component}
     * using Minecraft's GUI font.
     *
     * @param component the component to measure
     * @return the width in pixels
     */
    public static int getRenderWidth(Component component) {
        net.minecraft.client.gui.Font font = McUtils.mc().gui.getFont();
        return font.width(component);
    }

    /**
     * Adjusts the provided {@link MutableComponent} by appending special Unicode characters
     * ensuring the total rendered width of the resulting component becomes exactly 0.
     * <ul>
     *   <li>{@code "/uDAFF"} + offset from {@code "/uE000"} for negative pixel widths</li>
     *   <li>Regular spaces (each 4 pixels wide in the default font) for positive compensation</li>
     * </ul>
     *
     * @param component the text component whose width should be neutralized
     * @return the original component with the width-correction suffix appended (same instance)
     */
    public static MutableComponent manageWidth(MutableComponent component) {
        int width = getRenderWidth(component);

        if (width == 0) {
            return component;
        }

        String specialChars;
        if (width > 0) {
            // Positive width -> cancel it with a single negative-width character
            specialChars = specialChar + "" + (char) ((int) zeroWidthChar - width);
        } else {
            // Negative width -> add positive width via spaces and a possible negative remainder
            int needed = -width;
            int spaces = (needed + 3) / 4;
            int modulo = needed % 4;

            StringBuilder sb = new StringBuilder(" ".repeat(spaces));

            if (modulo != 0) {
                int negativeCompensation = 4 - modulo;
                sb.append(specialChar).append((char) ((int) zeroWidthChar - negativeCompensation));
            }
            specialChars = sb.toString();
        }

        MutableComponent appendment = Component.literal(specialChars);
        appendment.setStyle(component.getStyle());
        return component.append(appendment);
    }

    /**
     * This subclass is to append the {@code §<x>} chatcolors and formating style to another line.
     * */
    private static final class StyleState {
        private String color = null;
        private boolean bold;
        private boolean italic;
        private boolean underline;
        private boolean strikethrough;
        private boolean obfuscated;

        public String getPrefix() {
            StringBuilder sb = new StringBuilder(12);
            if (color != null) sb.append('§').append(color);
            if (bold) sb.append("§l");
            if (italic) sb.append("§o");
            if (underline) sb.append("§n");
            if (strikethrough) sb.append("§m");
            if (obfuscated) sb.append("§k");
            return sb.toString();
        }

        public void consume(String line) {
            color = null;
            bold = false;
            italic = false;
            underline = false;
            strikethrough = false;
            obfuscated = false;

            for (int i = line.length() - 2; i >= 0; i--) {
                if (line.charAt(i) != '§') continue;

                char code = Character.toLowerCase(line.charAt(i + 1));

                if (code == 'r') {
                    break;
                }
                if (isColorCode(code)) {
                    color = code + "";
                    break;
                }

                if (code == '#') {  // §#f2d7fdff
                    if (line.length() < i + 10) continue;
                    color = line.substring(i + 1, i + 10);
                    break;
                }

                switch (code) {
                    case 'l' -> bold = true;
                    case 'o' -> italic = true;
                    case 'n' -> underline = true;
                    case 'm' -> strikethrough = true;
                    case 'k' -> obfuscated = true;
                }
            }
        }

        private boolean isColorCode(char c) {
            return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
        }
    }

    public static final class Content {
        private String text;
        private String cleanText;
        private String name;
        private String startPos;
        private String portrait;
        private boolean confirmationless = true;
        private final String[] choices = new String[4];

        private Content() {}

        /**
         * If enabled, {@link #getText()} can contain chat formating codes like §0-9, §a-f or §#HEX-Color
         * @return clean getText without formatings
         * */
        public String getCleanText() {
            return cleanText;
        }

        private void setCleanText(String cleanText) {
            this.cleanText = cleanText;
        }

        /**
         * If enabled, can contain chat formating codes like §0-9, §a-f or §#HEX-Color <br />
         * use {@link #getCleanText()} for Text without formatings. <br />
         * use {@code StyledText.fromString(text).getComponent()} to format
         * */
        public String getText() {
            return text;
        }

        private void setText(String text) {
            this.text = text;
        }

        public String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        public String getStartPos() {
            return startPos;
        }

        private void setStartPos(String startPos) {
            this.startPos = startPos;
        }

        public boolean hashPortrait() {
            return portrait != null;
        }

        public String getPortrait() {
            return portrait;
        }

        private void setPortrait(String portrait) {
            this.portrait = portrait;
        }

        public boolean isConfirmationless() {
            return confirmationless;
        }

        private void setConfirmationless() {
            this.confirmationless = true;
        }

        public boolean hasChoices() {
            return !Arrays.stream(getChoices()).allMatch(Objects::isNull);
        }

        public String[] getChoices() {
            return choices;
        }

        @Override
        public String toString() {
            return "Content{" +
                    "text='" + text + '\'' +
                    ", cleanText='" + cleanText + '\'' +
                    ", name='" + name + '\'' +
                    ", startPos='" + startPos + '\'' +
                    ", portrait='" + portrait + '\'' +
                    ", confirmationless=" + confirmationless +
                    ", choices=" + Arrays.toString(choices) +
                    '}';
        }
    }
}
