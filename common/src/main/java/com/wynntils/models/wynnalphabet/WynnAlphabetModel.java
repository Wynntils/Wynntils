/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnalphabet;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.models.wynnalphabet.type.TranscribeCondition;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class WynnAlphabetModel extends Model {
    private static final int FIFTY_INDEX = 10;
    private static final int MAX_TRANSCRIBABLE_NUMBER = 5000;
    private static final int ONE_HUNDRED_INDEX = 11;
    private static final int TEN_INDEX = 9;
    private static final List<Character> englishCharacters = List.of(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
            'v', 'w', 'x', 'y', 'z', '.', '!', '?');
    private static final List<Integer> englishNumbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 50, 100);
    private static final List<Character> gavellianCharacters = List.of(
            'ⓐ', 'ⓑ', 'ⓒ', 'ⓓ', 'ⓔ', 'ⓕ', 'ⓖ', 'ⓗ', 'ⓘ', 'ⓙ', 'ⓚ', 'ⓛ', 'ⓜ', 'ⓝ', 'ⓞ', 'ⓟ', 'ⓠ', 'ⓡ', 'ⓢ', 'ⓣ', 'ⓤ',
            'ⓥ', 'ⓦ', 'ⓧ', 'ⓨ', 'ⓩ');
    private static final List<Character> wynnicCharacters = List.of(
            '⒜', '⒝', '⒞', '⒟', '⒠', '⒡', '⒢', '⒣', '⒤', '⒥', '⒦', '⒧', '⒨', '⒩', '⒪', '⒫', '⒬', '⒭', '⒮', '⒯', '⒰',
            '⒱', '⒲', '⒳', '⒴', '⒵', '０', '１', '２');
    private static final List<Character> wynnicNumbers =
            List.of('⑴', '⑵', '⑶', '⑷', '⑸', '⑹', '⑺', '⑻', '⑼', '⑽', '⑾', '⑿');

    private static final Pattern WYNNIC_CHARACTER_PATTERN = Pattern.compile("[⒜-⒵０-２]+");
    private static final Pattern WYNNIC_NUMBER_PATTERN = Pattern.compile("[⑴-⑿]+");
    private static final Pattern GAVELLIAN_CHARACTER_PATTERN = Pattern.compile("[ⓐ-ⓩ]+");

    private static final Map<Character, Character> englishToGavellianMap = new HashMap<>();
    private static final Map<Character, Character> englishToWynnicMap = new HashMap<>();
    private static final Map<Character, Character> gavellianToEnglishMap = new HashMap<>();
    private static final Map<Character, Character> wynnicToEnglishMap = new HashMap<>();
    private static final Pattern BRACKET_PATTERN = Pattern.compile("(\\[\\[.*\\]\\])|(<<.*>>)");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final StyledText GAVELLIAN_TRANSCRIBER = StyledText.fromString("§rHigh Gavellian Transcriber");
    private static final StyledText WYNNIC_TRANSCRIBER = StyledText.fromString("§fAncient Wynnic Transcriber");

    private WynnAlphabet selectedAlphabet = WynnAlphabet.DEFAULT;

    public WynnAlphabetModel() {
        super(List.of());

        createTranscribableMaps();
    }

    public StyledTextPart transcribeMessageFromWynnAlphabet(
            StyledTextPart originalPart,
            WynnAlphabet alphabet,
            boolean useColors,
            ChatFormatting colorToUse,
            boolean originalTextAsTooltip) {
        String originalString = originalPart.getString(null, StyleType.NONE);
        StringBuilder transcriptedStringBuilder = new StringBuilder(originalString.length());

        // If the message is a wynnic number, we transcribe it to an english number
        if (WYNNIC_NUMBER_PATTERN.matcher(originalString).matches()) {
            transcriptedStringBuilder.append(wynnicNumToInt(originalString));
        } else {
            // Otherwise, we transcribe the message to the selected alphabet
            for (char character : originalString.toCharArray()) {
                Character replacement = alphabet == WynnAlphabet.GAVELLIAN
                        ? Models.WynnAlphabet.transcribeGavellianToEnglish(character)
                        : Models.WynnAlphabet.transcribeWynnicToEnglish(character);

                transcriptedStringBuilder.append(replacement);
            }
        }

        PartStyle partStyle = originalPart.getPartStyle();

        if (useColors && !originalTextAsTooltip) {
            partStyle = partStyle.withColor(colorToUse);
        }

        String transcriptedString = transcriptedStringBuilder.toString();

        // Add the hover event to the transcribed message
        Component hoverComponent = originalTextAsTooltip
                ? Component.literal(transcriptedString)
                : Component.translatable(
                        "feature.wynntils.transcribeMessages.transcribedFrom",
                        StringUtils.capitalizeFirst(alphabet.toString().toLowerCase(Locale.ROOT)),
                        originalString);
        partStyle = partStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent));

        return new StyledTextPart(
                originalTextAsTooltip ? originalString : transcriptedString, partStyle.getStyle(), null, Style.EMPTY);
    }

    public String transcribeBracketedText(String message) {
        Pattern bracketPattern = Pattern.compile("\\[\\[([^\\]]*)\\]\\]|<<([^>]*)>>");

        List<String> wynnicSubstring = new ArrayList<>();
        List<String> gavellianSubstring = new ArrayList<>();

        Matcher matcher = bracketPattern.matcher(message);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                wynnicSubstring.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                gavellianSubstring.add(matcher.group(2));
            }
        }

        if (wynnicSubstring.isEmpty() && gavellianSubstring.isEmpty()) return message;

        StringBuilder updatedMessage = new StringBuilder(message);

        for (String wynnicText : wynnicSubstring) {
            String transcriptedText = Models.WynnAlphabet.transcribeMessageToWynnAlphabet(
                    wynnicText.toLowerCase(Locale.ROOT), WynnAlphabet.WYNNIC);
            replaceTranscribed(updatedMessage, "[[" + wynnicText + "]]", transcriptedText);
        }

        for (String gavellianText : gavellianSubstring) {
            String transcriptedText = Models.WynnAlphabet.transcribeMessageToWynnAlphabet(
                    gavellianText.toLowerCase(Locale.ROOT), WynnAlphabet.GAVELLIAN);
            replaceTranscribed(updatedMessage, "<<" + gavellianText + ">>", transcriptedText);
        }

        return updatedMessage.toString();
    }

    public int calculateWynnicNum(String wynnicNums, int numToAdd) {
        int result = 0;

        for (char num : wynnicNums.toCharArray()) {
            if (num == getOneHundred()) {
                result += 100;
            } else if (num == getFifty()) {
                result += 50;
            } else if (num == getTen()) {
                result += 10;
            } else {
                int wynnIndex = wynnicNumbers.indexOf(num);

                result += englishNumbers.get(wynnIndex);
            }
        }

        String resultStr = String.valueOf(result);

        resultStr += String.valueOf(numToAdd);

        return Integer.parseInt(resultStr);
    }

    public String transcribeMessageToWynnAlphabet(String original, WynnAlphabet alphabet) {
        String transcripted = original;

        if (alphabet == WynnAlphabet.GAVELLIAN) {
            for (char character : original.toCharArray()) {
                Character replacement = Models.WynnAlphabet.transcribeEnglishToGavellian(character);

                if (!replacement.equals(character)) {
                    transcripted = transcripted.replace(character, replacement);
                }
            }
        } else {
            Matcher numMatcher = NUMBER_PATTERN.matcher(transcripted);

            transcripted = numMatcher.replaceAll(match -> {
                int numToTranscript = Integer.parseInt(match.group());

                if (numToTranscript > MAX_TRANSCRIBABLE_NUMBER) {
                    return "∞";
                } else {
                    return Models.WynnAlphabet.intToWynnicNum(numToTranscript);
                }
            });

            for (char character : original.toCharArray()) {
                Character replacement = Models.WynnAlphabet.transcribeEnglishToWynnic(character);

                if (!replacement.equals(character)) {
                    transcripted = transcripted.replace(character, replacement);
                }
            }
        }

        return transcripted;
    }

    public int wynnicNumToInt(String wynnicNum) {
        int result = 0;

        for (char num : wynnicNum.toCharArray()) {
            int numIndex = wynnicNumbers.indexOf(num);

            result += englishNumbers.get(numIndex);
        }

        return result;
    }

    public String intToWynnicNum(int number) {
        StringBuilder wynnicNums = new StringBuilder();

        int hundreds = number / 100;

        number -= (hundreds * 100);

        int fifties = number >= 50 ? 1 : 0;

        number -= (fifties * 50);

        int tens = number / 10;

        number -= (tens * 10);

        wynnicNums.append(String.valueOf(getOneHundred()).repeat(Math.max(0, hundreds)));
        wynnicNums.append(String.valueOf(getFifty()).repeat(Math.max(0, fifties)));
        wynnicNums.append(String.valueOf(getTen()).repeat(Math.max(0, tens)));

        if (number > 0) {
            wynnicNums.append(wynnicNumbers.get(englishNumbers.indexOf(number)));
        }

        return wynnicNums.toString();
    }

    public boolean shouldTranscribe(TranscribeCondition condition, WynnAlphabet alphabet) {
        return switch (condition) {
            case NEVER -> false;
            case TRANSCRIBER ->
                alphabet == WynnAlphabet.WYNNIC
                        ? hasTranscriber(WynnAlphabet.WYNNIC)
                        : hasTranscriber(WynnAlphabet.GAVELLIAN);
            default -> true;
        };
    }

    public boolean hasWynnicOrGavellian(String message) {
        for (int i = message.length() - 1; i >= 0; i--) {
            char c = message.charAt(i);

            if (gavellianCharacters.contains(c) || wynnicCharacters.contains(c) || wynnicNumbers.contains(c)) {
                return true;
            }
        }

        return false;
    }

    public boolean containsBrackets(String message) {
        return BRACKET_PATTERN.matcher(message).find();
    }

    public Matcher getWynnicCharacterMatcher(String message) {
        return WYNNIC_CHARACTER_PATTERN.matcher(message);
    }

    public Matcher getWynnicNumberMatcher(String message) {
        return WYNNIC_NUMBER_PATTERN.matcher(message);
    }

    public Matcher getGavellianCharacterMatcher(String message) {
        return GAVELLIAN_CHARACTER_PATTERN.matcher(message);
    }

    private void createTranscribableMaps() {
        for (int i = 0; i < gavellianCharacters.size(); i++) {
            gavellianToEnglishMap.put(gavellianCharacters.get(i), englishCharacters.get(i));
        }

        for (int i = 0; i < wynnicCharacters.size(); i++) {
            wynnicToEnglishMap.put(wynnicCharacters.get(i), englishCharacters.get(i));
        }

        for (int i = 0; i < gavellianCharacters.size(); i++) {
            englishToGavellianMap.put(englishCharacters.get(i), gavellianCharacters.get(i));
        }

        for (int i = 0; i < wynnicCharacters.size(); i++) {
            englishToWynnicMap.put(englishCharacters.get(i), wynnicCharacters.get(i));
        }
    }

    private void replaceTranscribed(StringBuilder stringBuilder, String original, String replacement) {
        int index = stringBuilder.indexOf(original);

        while (index != -1) {
            stringBuilder.replace(index, index + original.length(), replacement);
            index = stringBuilder.indexOf(original, index + replacement.length());
        }
    }

    private Character transcribeGavellianToEnglish(Character characterToTranscribe) {
        return gavellianToEnglishMap.getOrDefault(characterToTranscribe, characterToTranscribe);
    }

    private Character transcribeWynnicToEnglish(Character characterToTranscribe) {
        return wynnicToEnglishMap.getOrDefault(characterToTranscribe, characterToTranscribe);
    }

    private Character transcribeEnglishToGavellian(Character characterToTranscribe) {
        return englishToGavellianMap.getOrDefault(characterToTranscribe, characterToTranscribe);
    }

    private Character transcribeEnglishToWynnic(Character characterToTranscribe) {
        return englishToWynnicMap.getOrDefault(characterToTranscribe, characterToTranscribe);
    }

    private boolean hasTranscriber(WynnAlphabet transcriberToFind) {
        Inventory inventory = McUtils.inventory();

        for (int slotNum = 0; slotNum < Inventory.INVENTORY_SIZE; slotNum++) {
            ItemStack itemStack = inventory.getItem(slotNum);

            if (transcriberToFind == WynnAlphabet.WYNNIC) {
                if (StyledText.fromComponent(itemStack.getHoverName()).equals(WYNNIC_TRANSCRIBER)) {
                    return true;
                }
            } else {
                if (StyledText.fromComponent(itemStack.getHoverName()).equals(GAVELLIAN_TRANSCRIBER)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void setSelectedAlphabet(WynnAlphabet selectedAlphabet) {
        this.selectedAlphabet = selectedAlphabet;
    }

    public WynnAlphabet getSelectedAlphabet() {
        return selectedAlphabet;
    }

    public List<Character> getGavellianCharacters() {
        return gavellianCharacters;
    }

    public List<Character> getWynnicCharacters() {
        return wynnicCharacters;
    }

    public List<Character> getEnglishCharacters() {
        return englishCharacters;
    }

    public List<Integer> getEnglishNumbers() {
        return englishNumbers;
    }

    public List<Character> getWynnicNumbers() {
        return wynnicNumbers;
    }

    public Character getFifty() {
        return wynnicNumbers.get(FIFTY_INDEX);
    }

    public Character getOneHundred() {
        return wynnicNumbers.get(ONE_HUNDRED_INDEX);
    }

    public Character getTen() {
        return wynnicNumbers.get(TEN_INDEX);
    }
}
