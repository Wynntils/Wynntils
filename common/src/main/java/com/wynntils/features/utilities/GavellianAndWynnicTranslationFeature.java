/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.event.NpcDialogEvent;
import com.wynntils.handlers.chat.type.NpcDialogueType;
import com.wynntils.mc.event.ChatScreenKeyTypedEvent;
import com.wynntils.mc.event.ChatSentEvent;
import com.wynntils.mc.event.CommandSentEvent;
import com.wynntils.mc.event.EditBoxInsertEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.screens.translation.WynnLanguage;
import com.wynntils.screens.translation.widgets.WynnLanguageButton;
import com.wynntils.utils.colors.ColorChatFormatting;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.IterationDecision;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.UTILITIES)
public class GavellianAndWynnicTranslationFeature extends Feature {
    @RegisterConfig
    public final Config<Boolean> translateChat = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> translateNpcs = new Config<>(true);

    @RegisterConfig
    public final Config<TranslateCondition> translateCondition = new Config<>(TranslateCondition.ALWAYS);

    @RegisterConfig
    public final Config<Boolean> useBrackets = new Config<>(false);

    @RegisterConfig
    public final Config<Boolean> coloredTranslations = new Config<>(true);

    @RegisterConfig
    public final Config<ColorChatFormatting> gavellianColor = new Config<>(ColorChatFormatting.LIGHT_PURPLE);

    @RegisterConfig
    public final Config<ColorChatFormatting> wynnicColor = new Config<>(ColorChatFormatting.GREEN);

    private static final int FIFTY_INDEX = 36;
    private static final int ONE_HUNDERED_INDEX = 37;
    private static final int ONE_INDEX = 26;
    private static final int MAX_TRANSLATABLE_NUMBER = 5000;
    private static final int TEN_INDEX = 35;

    private static final List<String> english = List.of(
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
            "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "50", "100", ".", "!", "?");
    private static final List<Character> gavellian = List.of(
            'ⓐ', 'ⓑ', 'ⓒ', 'ⓓ', 'ⓔ', 'ⓕ', 'ⓖ', 'ⓗ', 'ⓘ', 'ⓙ', 'ⓚ', 'ⓛ', 'ⓜ', 'ⓝ', 'ⓞ', 'ⓟ', 'ⓠ', 'ⓡ', 'ⓢ', 'ⓣ', 'ⓤ',
            'ⓥ', 'ⓦ', 'ⓧ', 'ⓨ', 'ⓩ');
    private static final List<Character> wynnic = List.of(
            '⒜', '⒝', '⒞', '⒟', '⒠', '⒡', '⒢', '⒣', '⒤', '⒥', '⒦', '⒧', '⒨', '⒩', '⒪', '⒫', '⒬', '⒭', '⒮', '⒯', '⒰',
            '⒱', '⒲', '⒳', '⒴', '⒵', '⑴', '⑵', '⑶', '⑷', '⑸', '⑹', '⑺', '⑻', '⑼', '⑽', '⑾', '⑿', '０', '１', '２');
    private static final List<Character> numbers = wynnic.subList(ONE_INDEX, ONE_HUNDERED_INDEX + 1);
    private static final Pattern END_OF_HEADER_PATTERN = Pattern.compile(".*[\\]:]\\s?");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^(0|[1-9][0-9]*)$");
    private static final Pattern WYNNIC_NUMBER_PATTERN = Pattern.compile("[⑴-⑿]+");

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        if (useBrackets.get()) return;

        if (event.getScreen() instanceof ChatScreen chatScreen) {
            chatScreen.width -= 45;
            int xOffset = chatScreen.width + 1;

            chatScreen.addRenderableWidget(
                    new WynnLanguageButton(xOffset, chatScreen.height - 14, 12, 12, WynnLanguage.DEFAULT));

            xOffset += 15;

            chatScreen.addRenderableWidget(
                    new WynnLanguageButton(xOffset, chatScreen.height - 14, 12, 12, WynnLanguage.WYNNIC));

            xOffset += 15;

            chatScreen.addRenderableWidget(
                    new WynnLanguageButton(xOffset, chatScreen.height - 14, 12, 12, WynnLanguage.GAVELLIAN));
        }
    }

    @SubscribeEvent
    public void onEditBoxInsert(EditBoxInsertEvent event) {
        if (!(McUtils.mc().screen instanceof ChatScreen chatScreen)) return;
        if (Services.WynnLanguageSerivce.getSelectedLanguage() == WynnLanguage.DEFAULT) return;
        if (useBrackets.get()) return;

        List<Character> replacementList =
                Services.WynnLanguageSerivce.getSelectedLanguage() == WynnLanguage.GAVELLIAN ? gavellian : wynnic;

        try {
            Integer.parseInt(event.getTextToWrite());

            if (Services.WynnLanguageSerivce.getSelectedLanguage() != WynnLanguage.WYNNIC) return;

            handleTypedNumber(event.getTextToWrite(), replacementList, event, chatScreen);
        } catch (NumberFormatException ex) {
            handleTypedCharacter(event.getTextToWrite(), replacementList, event, chatScreen);
        }
    }

    @SubscribeEvent
    public void onChatScreenKeyTyped(ChatScreenKeyTypedEvent event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_BACKSPACE) return;
        if (!(McUtils.mc().screen instanceof ChatScreen chatScreen)) return;
        if (!chatScreen.input.getHighlighted().isBlank()) return;

        String beforeCursor = chatScreen.input.getValue().substring(0, chatScreen.input.getCursorPosition());
        String afterCursor = chatScreen.input.getValue().substring(chatScreen.input.getCursorPosition());

        if (beforeCursor.isBlank() || !numbers.contains(beforeCursor.charAt(beforeCursor.length() - 1))) return;

        StringBuilder currentNumsStr = new StringBuilder();

        for (int i = beforeCursor.length() - 1; i >= 0; i--) {
            if (numbers.contains(beforeCursor.charAt(i))) {
                currentNumsStr.append(beforeCursor.charAt(i));
            } else {
                break;
            }
        }

        currentNumsStr.reverse();

        int wynnicNum = wynnicNumToInt(currentNumsStr.toString());

        String backspacedNum = String.valueOf(wynnicNum);

        if (backspacedNum.length() > 1) {
            event.setCanceled(true);

            backspacedNum = backspacedNum.substring(0, backspacedNum.length() - 1);

            int index = beforeCursor.lastIndexOf(currentNumsStr.toString());

            beforeCursor = beforeCursor.substring(0, index);

            String translatedNum = intToWynnicNum(Integer.parseInt(backspacedNum));

            String newInput = beforeCursor + translatedNum + afterCursor;

            int newCursorPos = newInput.indexOf(translatedNum) + translatedNum.length();

            chatScreen.input.setValue(newInput);

            chatScreen.input.moveCursorTo(newCursorPos);
        }
    }

    private void handleTypedCharacter(
            String typedChar, List<Character> replacementList, EditBoxInsertEvent event, ChatScreen chatScreen) {
        int engIndex = english.indexOf(typedChar.toLowerCase(Locale.ROOT));

        if (engIndex == -1) return;
        if (engIndex >= replacementList.size()) return;

        String replaced = String.valueOf(replacementList.get(engIndex));

        event.setCanceled(true);

        chatScreen.input.insertText(replaced);
    }

    private void handleTypedNumber(
            String typedNumber, List<Character> replacementList, EditBoxInsertEvent event, ChatScreen chatScreen) {
        event.setCanceled(true);

        String input = chatScreen.input.getValue();

        String beforeCursor = input.substring(0, chatScreen.input.getCursorPosition());
        String afterCursor = input.substring(chatScreen.input.getCursorPosition());

        if (input.isBlank() || !numbers.contains(beforeCursor.charAt(beforeCursor.length() - 1))) {
            int engIndex = english.indexOf(typedNumber);

            String replaced = String.valueOf(replacementList.get(engIndex));

            chatScreen.input.insertText(replaced);

            return;
        }

        StringBuilder currentNumsStr = new StringBuilder();

        for (int i = beforeCursor.length() - 1; i >= 0; i--) {
            if (numbers.contains(beforeCursor.charAt(i))) {
                currentNumsStr.append(beforeCursor.charAt(i));
            } else {
                break;
            }
        }

        currentNumsStr.reverse();

        int index = beforeCursor.lastIndexOf(currentNumsStr.toString());

        beforeCursor = beforeCursor.substring(0, index);

        int updateNum = calculateWynnicNum(currentNumsStr.toString(), typedNumber);

        String translatedNum = updateNum > MAX_TRANSLATABLE_NUMBER ? "∞" : intToWynnicNum(updateNum);

        String newInput = beforeCursor + translatedNum + afterCursor;

        int newCursorPos = newInput.indexOf(translatedNum) + translatedNum.length();

        chatScreen.input.setValue(newInput);

        chatScreen.input.moveCursorTo(newCursorPos);
    }

    private int calculateWynnicNum(String wynnicNums, String numToAdd) {
        int result = 0;

        for (char num : wynnicNums.toCharArray()) {
            if (num == wynnic.get(ONE_HUNDERED_INDEX)) {
                result += 100;
            } else if (num == wynnic.get(FIFTY_INDEX)) {
                result += 50;
            } else if (num == wynnic.get(TEN_INDEX)) {
                result += 10;
            } else {
                int wynnIndex = wynnic.indexOf(num);

                result += Integer.parseInt(english.get(wynnIndex));
            }
        }

        String resultStr = String.valueOf(result);

        resultStr += numToAdd;

        return Integer.parseInt(resultStr);
    }

    @SubscribeEvent
    public void onChatSend(ChatSentEvent event) {
        if (event.getMessage().isBlank()) return;

        String message = event.getMessage();

        if (useBrackets.get()
                && ((message.contains("{") && message.contains("}"))
                        || (message.contains("<") && message.contains(">")))) {
            Pattern pattern = Pattern.compile("\\{([^}]*)\\}|<([^>]*)>");

            List<String> wynnicSubstring = new ArrayList<>();
            List<String> gavellianSubstring = new ArrayList<>();

            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                if (matcher.group(1) != null) {
                    wynnicSubstring.add(matcher.group(1));
                } else if (matcher.group(2) != null) {
                    gavellianSubstring.add(matcher.group(2));
                }
            }

            if (wynnicSubstring.isEmpty() && gavellianSubstring.isEmpty()) return;

            event.setCanceled(true);

            for (String wynnicText : wynnicSubstring) {
                String translatedText = getStringWithTranslation(wynnicText, WynnLanguage.WYNNIC);
                message = message.replace("{" + wynnicText + "}", translatedText);
            }

            for (String gavellianText : gavellianSubstring) {
                String translatedText = getStringWithTranslation(gavellianText, WynnLanguage.GAVELLIAN);
                message = message.replace("<" + gavellianText + ">", translatedText);
            }

            McUtils.mc().getConnection().sendChat(message);
        }
    }

    @SubscribeEvent
    public void onCommandSent(CommandSentEvent e) {
        String message = e.getCommand();

        if (useBrackets.get()
                && ((message.contains("{") && message.contains("}"))
                        || (message.contains("<") && message.contains(">")))) {
            Pattern pattern = Pattern.compile("\\{([^}]*)\\}|<([^>]*)>");

            List<String> wynnicSubstring = new ArrayList<>();
            List<String> gavellianSubstring = new ArrayList<>();

            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                if (matcher.group(1) != null) {
                    wynnicSubstring.add(matcher.group(1));
                } else if (matcher.group(2) != null) {
                    gavellianSubstring.add(matcher.group(2));
                }
            }

            if (wynnicSubstring.isEmpty() && gavellianSubstring.isEmpty()) return;

            e.setCanceled(true);

            for (String wynnicText : wynnicSubstring) {
                String translatedText = getStringWithTranslation(wynnicText, WynnLanguage.WYNNIC);
                message = message.replace("{" + wynnicText + "}", translatedText);
            }

            for (String gavellianText : gavellianSubstring) {
                String translatedText = getStringWithTranslation(gavellianText, WynnLanguage.GAVELLIAN);
                message = message.replace("<" + gavellianText + ">", translatedText);
            }

            McUtils.sendCommand(message);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ChatMessageReceivedEvent e) {
        if (!translateChat.get()) return;
        if (!hasWynnicOrGavellian(e.getStyledText().getString())) return;

        boolean translateWynnic = true;
        boolean translateGavellian = true;

        switch (translateCondition.get()) {
            case NEVER -> {
                return;
            }
            case BOOK -> {
                translateWynnic = Services.WynnLanguageSerivce.hasTranscriber(WynnLanguage.WYNNIC);
                translateGavellian = Services.WynnLanguageSerivce.hasTranscriber(WynnLanguage.GAVELLIAN);
            }
            case DISCOVERY -> {
                translateWynnic = Services.WynnLanguageSerivce.completedDiscovery(WynnLanguage.WYNNIC);
                translateGavellian = Services.WynnLanguageSerivce.completedDiscovery(WynnLanguage.GAVELLIAN);
            }
        }

        if (!translateWynnic && !translateGavellian) return;

        StyledText styledText = e.getStyledText();

        StyledText modified = getStyledTextWithTranslation(styledText, translateWynnic, translateGavellian, false);

        if (styledText.getString().equalsIgnoreCase(modified.getString())) return;

        e.setMessage(modified.getComponent());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onNpcDialogue(NpcDialogEvent e) {
        if (!translateNpcs.get()) return;
        if (!hasWynnicOrGavellian(e.getChatMessage().toString())) return;

        boolean translateWynnic = true;
        boolean translateGavellian = true;

        switch (translateCondition.get()) {
            case NEVER -> {
                return;
            }
            case BOOK -> {
                translateWynnic = Services.WynnLanguageSerivce.hasTranscriber(WynnLanguage.WYNNIC);
                translateGavellian = Services.WynnLanguageSerivce.hasTranscriber(WynnLanguage.GAVELLIAN);
            }
            case DISCOVERY -> {
                translateWynnic = Services.WynnLanguageSerivce.completedDiscovery(WynnLanguage.WYNNIC);
                translateGavellian = Services.WynnLanguageSerivce.completedDiscovery(WynnLanguage.GAVELLIAN);
            }
        }

        if (!translateWynnic && !translateGavellian) return;

        if (e instanceof WynnTranslatedNpcDialogEvent || e instanceof TranslationFeature.TranslatedNpcDialogEvent)
            return;

        e.setCanceled(true);

        if (!e.getChatMessage().isEmpty()) {
            boolean finalTranslateWynnic = translateWynnic;
            boolean finalTranslateGavellian = translateGavellian;

            List<Component> translatedComponents = e.getChatMessage().stream()
                    .map(component -> wrapCoding(StyledText.fromComponent(component)))
                    .map(styledText -> getStyledTextWithTranslation(
                            StyledText.fromString(styledText), finalTranslateWynnic, finalTranslateGavellian, true))
                    .map(styledText -> unwrapCoding(styledText.getString()))
                    .map(s -> ((Component) s.getComponent()))
                    .toList();

            Managers.TickScheduler.scheduleNextTick(() -> {
                NpcDialogEvent translatedEvent =
                        new WynnTranslatedNpcDialogEvent(translatedComponents, e.getType(), e.isProtected());
                WynntilsMod.postEvent(translatedEvent);
            });
        } else {
            NpcDialogEvent translatedEvent = new WynnTranslatedNpcDialogEvent(List.of(), e.getType(), e.isProtected());
            WynntilsMod.postEvent(translatedEvent);
        }
    }

    private boolean hasWynnicOrGavellian(String message) {
        for (int i = message.length() - 1; i >= 0; i--) {
            char c = message.charAt(i);

            if (gavellian.contains(c) || wynnic.contains(c)) {
                return true;
            }
        }

        return false;
    }

    private StyledText unwrapCoding(String origCoded) {
        return StyledText.fromString(origCoded.replaceAll("\\{ ?§ ?([0-9a-fklmnor]) ?\\}", "§$1"));
    }

    private String wrapCoding(StyledText origCoded) {
        return origCoded.getString().replaceAll("(§[0-9a-fklmnor])", "{$1}");
    }

    private StyledText getStyledTextWithTranslation(
            StyledText original, boolean translateWynnic, boolean translateGavellian, boolean npcDialogue) {
        ChatFormatting defaultColor = npcDialogue
                ? ColorChatFormatting.GREEN.getChatFormatting()
                : ColorChatFormatting.WHITE.getChatFormatting();

        return original.iterateBackwards((part, changes) -> {
            String partText = part.getString(null, PartStyle.StyleType.NONE);

            if (END_OF_HEADER_PATTERN.matcher(partText).matches()) {
                return IterationDecision.BREAK;
            }

            if (translateWynnic) {
                Matcher numMatcher = WYNNIC_NUMBER_PATTERN.matcher(partText);

                if (coloredTranslations.get()) {
                    partText = numMatcher.replaceAll(match -> wynnicColor.get().getChatFormatting()
                            + String.valueOf(wynnicNumToInt(match.group()))
                            + ColorChatFormatting.WHITE.getChatFormatting());
                } else {
                    partText = numMatcher.replaceAll(match -> String.valueOf(wynnicNumToInt(match.group())));
                }

                for (int i = 0; i < wynnic.size(); i++) {
                    Character wynnicCharacter = wynnic.get(i);
                    String englishCharacter = english.get(i);

                    if (coloredTranslations.get()) {
                        partText = partText.replace(
                                wynnicCharacter.toString(),
                                wynnicColor.get().getChatFormatting() + englishCharacter + defaultColor);
                    } else {
                        partText = partText.replace(wynnicCharacter.toString(), englishCharacter);
                    }
                }
            }

            if (translateGavellian) {
                for (int i = 0; i < gavellian.size(); i++) {
                    Character gavellianCharacter = gavellian.get(i);
                    String englishCharacter = english.get(i);

                    if (coloredTranslations.get()) {
                        partText = partText.replace(
                                gavellianCharacter.toString(),
                                gavellianColor.get().getChatFormatting() + englishCharacter + defaultColor);
                    } else {
                        partText = partText.replace(gavellianCharacter.toString(), englishCharacter);
                    }
                }
            }

            changes.remove(part);
            StyledTextPart newPart =
                    new StyledTextPart(partText, part.getPartStyle().getStyle(), null, Style.EMPTY);
            changes.add(newPart);

            return IterationDecision.CONTINUE;
        });
    }

    private String getStringWithTranslation(String original, WynnLanguage language) {
        String translated = original.toLowerCase(Locale.ROOT);

        if (language == WynnLanguage.GAVELLIAN) {
            for (int i = 0; i < gavellian.size(); i++) {
                Character gavellianCharacter = gavellian.get(i);
                String englishCharacter = english.get(i);

                translated = translated.replace(englishCharacter, gavellianCharacter.toString());
            }
        } else {
            Matcher numMatcher = NUMBER_PATTERN.matcher(translated);

            translated = numMatcher.replaceAll(match -> intToWynnicNum(Integer.parseInt(match.group())));

            for (int i = 0; i < wynnic.size(); i++) {
                Character wynnicCharacter = wynnic.get(i);
                String englishCharacter = english.get(i);

                translated = translated.replace(englishCharacter, wynnicCharacter.toString());
            }
        }

        return translated;
    }

    private int wynnicNumToInt(String wynnicNum) {
        int result = 0;

        for (char num : wynnicNum.toCharArray()) {
            int numIndex = wynnic.indexOf(num);

            result += Integer.parseInt(english.get(numIndex));
        }

        return result;
    }

    private String intToWynnicNum(int number) {
        StringBuilder wynnicNums = new StringBuilder();

        int hundereds = number / 100;

        number -= (hundereds * 100);

        int fifties = number >= 50 ? 1 : 0;

        number -= (fifties * 50);

        int tens = number / 10;

        number -= (tens * 10);

        wynnicNums.append(String.valueOf(wynnic.get(ONE_HUNDERED_INDEX)).repeat(Math.max(0, hundereds)));
        wynnicNums.append(String.valueOf(wynnic.get(FIFTY_INDEX)).repeat(Math.max(0, fifties)));
        wynnicNums.append(String.valueOf(wynnic.get(TEN_INDEX)).repeat(Math.max(0, tens)));

        if (number > 0) {
            wynnicNums.append(wynnic.get(english.indexOf(Integer.toString(number))));
        }

        return wynnicNums.toString();
    }

    private static class WynnTranslatedNpcDialogEvent extends NpcDialogEvent {
        protected WynnTranslatedNpcDialogEvent(List<Component> chatMsg, NpcDialogueType type, boolean isProtected) {
            super(chatMsg, type, isProtected);
        }
    }

    public enum TranslateCondition {
        ALWAYS,
        DISCOVERY,
        BOOK,
        NEVER
    }
}
