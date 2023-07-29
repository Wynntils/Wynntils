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
import com.wynntils.screens.translation.widgets.WynnLanguageButton;
import com.wynntils.services.wynnlanguage.WynnLanguage;
import com.wynntils.utils.colors.ColorChatFormatting;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.IterationDecision;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.eventbus.api.Event;
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
    public final Config<ColorChatFormatting> wynnicColor = new Config<>(ColorChatFormatting.DARK_GREEN);

    private static final int MAX_CHAT_LENGTH = 256;
    private static final int MAX_TRANSLATABLE_NUMBER = 5000;
    private static final Pattern END_OF_HEADER_PATTERN = Pattern.compile(".*[\\]:]\\s?");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^(0|[1-9][0-9]*)$");
    private static final Pattern WYNNIC_NUMBER_PATTERN = Pattern.compile("[⑴-⑿]+");

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        if (useBrackets.get()) return;

        if (event.getScreen() instanceof ChatScreen chatScreen) {
            chatScreen.width -= 45;
            int xOffset = chatScreen.width + 1;

            addLanguageButton(chatScreen, xOffset, WynnLanguage.DEFAULT);
            xOffset += 15;

            addLanguageButton(chatScreen, xOffset, WynnLanguage.WYNNIC);
            xOffset += 15;

            addLanguageButton(chatScreen, xOffset, WynnLanguage.GAVELLIAN);
        }
    }

    @SubscribeEvent
    public void onEditBoxInsert(EditBoxInsertEvent event) {
        if (!(McUtils.mc().screen instanceof ChatScreen chatScreen)) return;
        if (useBrackets.get()) return;

        WynnLanguage selectedLanguage = Services.WynnLanguageSerivce.getSelectedLanguage();

        if (selectedLanguage == WynnLanguage.DEFAULT) return;

        List<Character> replacementList = selectedLanguage == WynnLanguage.GAVELLIAN ? Services.WynnLanguageSerivce.getGavellian() : Services.WynnLanguageSerivce.getWynnic();

        try {
            Integer.parseInt(event.getTextToWrite());

            if (selectedLanguage != WynnLanguage.WYNNIC) return;

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

        if (beforeCursor.isBlank() || !Services.WynnLanguageSerivce.getNumbers().contains(beforeCursor.charAt(beforeCursor.length() - 1))) return;

        String currentNumsStr = getWynnicNumBeforeCursor(beforeCursor);

        int wynnicNum = wynnicNumToInt(currentNumsStr);

        String backspacedNum = String.valueOf(wynnicNum);

        if (backspacedNum.length() > 1) {
            event.setCanceled(true);

            backspacedNum = backspacedNum.substring(0, backspacedNum.length() - 1);

            int index = beforeCursor.lastIndexOf(currentNumsStr);

            beforeCursor = beforeCursor.substring(0, index);

            updateInput(beforeCursor, afterCursor, Integer.parseInt(backspacedNum), chatScreen.input);
        }
    }

    @SubscribeEvent
    public void onChatSend(ChatSentEvent event) {
        if (event.getMessage().isBlank()) return;

        String message = event.getMessage();

        if (useBrackets.get() && containsBrackets(message)) {
            String updatedMessage = translateSentMessage(message, event);

            McUtils.mc().getConnection().sendChat(updatedMessage);
        }
    }

    @SubscribeEvent
    public void onCommandSent(CommandSentEvent event) {
        String message = event.getCommand();

        if (useBrackets.get() && containsBrackets(message)) {
            String updatedMessage = translateSentMessage(message, event);

            McUtils.sendCommand(updatedMessage);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ChatMessageReceivedEvent event) {
        if (!translateChat.get()) return;
        if (!hasWynnicOrGavellian(event.getStyledText().getString())) return;

        boolean translateWynnic = shouldTranslate(WynnLanguage.WYNNIC);
        boolean translateGavellian = shouldTranslate(WynnLanguage.GAVELLIAN);

        if (!translateWynnic && !translateGavellian) return;

        StyledText styledText = event.getStyledText();

        StyledText modified = getStyledTextWithTranslation(styledText, translateWynnic, translateGavellian, false);

        if (styledText.getString().equalsIgnoreCase(modified.getString())) return;

        event.setMessage(modified.getComponent());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onNpcDialogue(NpcDialogEvent event) {
        if (!translateNpcs.get()) return;
        if (!hasWynnicOrGavellian(event.getChatMessage().toString())) return;

        boolean translateWynnic = shouldTranslate(WynnLanguage.WYNNIC);
        boolean translateGavellian = shouldTranslate(WynnLanguage.GAVELLIAN);

        if (!translateWynnic && !translateGavellian) return;

        event.setCanceled(true);

        if (!event.getChatMessage().isEmpty()) {
            List<Component> translatedComponents = event.getChatMessage().stream()
                    .map(styledText -> getStyledTextWithTranslation(
                            StyledText.fromComponent(styledText), translateWynnic, translateGavellian, true))
                    .map(s -> ((Component) s.getComponent()))
                    .toList();

            Managers.TickScheduler.scheduleNextTick(() -> {
                NpcDialogEvent translatedEvent =
                        new WynnTranslatedNpcDialogEvent(translatedComponents, event.getType(), event.isProtected());
                WynntilsMod.postEvent(translatedEvent);
            });
        } else {
            NpcDialogEvent translatedEvent =
                    new WynnTranslatedNpcDialogEvent(List.of(), event.getType(), event.isProtected());
            WynntilsMod.postEvent(translatedEvent);
        }
    }

    private void addLanguageButton(ChatScreen chatScreen, int xOffset, WynnLanguage language) {
        chatScreen.addRenderableWidget(new WynnLanguageButton(xOffset, chatScreen.height - 14, 12, 12, language));
    }

    private boolean containsBrackets(String message) {
        return (message.contains("{") && message.contains("}")) || (message.contains("<") && message.contains(">"));
    }

    private String translateSentMessage(String message, Event event) {
        Pattern bracketPattern = Pattern.compile("\\{([^}]*)\\}|<([^>]*)>");

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

        event.setCanceled(true);

        StringBuilder updatedMessage = new StringBuilder(message);

        for (String wynnicText : wynnicSubstring) {
            String translatedText = getStringWithTranslation(wynnicText, WynnLanguage.WYNNIC, false, null);
            replaceTranslated(updatedMessage, "{" + wynnicText + "}", translatedText);
        }

        for (String gavellianText : gavellianSubstring) {
            String translatedText = getStringWithTranslation(gavellianText, WynnLanguage.GAVELLIAN, false, null);
            replaceTranslated(updatedMessage, "<" + gavellianText + ">", translatedText);
        }

        return updatedMessage.toString();
    }

    private void replaceTranslated(StringBuilder stringBuilder, String original, String replacement) {
        int index = stringBuilder.indexOf(original);

        while (index != -1) {
            stringBuilder.replace(index, index + original.length(), replacement);
            index = stringBuilder.indexOf(original, index + replacement.length());
        }
    }

    private boolean shouldTranslate(WynnLanguage language) {
        return switch (translateCondition.get()) {
            case NEVER -> false;
            case BOOK -> language == WynnLanguage.WYNNIC
                    ? Services.WynnLanguageSerivce.hasTranscriber(WynnLanguage.WYNNIC)
                    : Services.WynnLanguageSerivce.hasTranscriber(WynnLanguage.GAVELLIAN);
            case DISCOVERY -> language == WynnLanguage.WYNNIC
                    ? Services.WynnLanguageSerivce.completedDiscovery(WynnLanguage.WYNNIC)
                    : Services.WynnLanguageSerivce.completedDiscovery(WynnLanguage.GAVELLIAN);
            default -> true;
        };
    }

    private void handleTypedCharacter(
            String typedChar, List<Character> replacementList, EditBoxInsertEvent event, ChatScreen chatScreen) {
        int engIndex = Services.WynnLanguageSerivce.getEnglish().indexOf(typedChar.toLowerCase(Locale.ROOT));

        if (engIndex == -1) return;
        if (engIndex >= replacementList.size()) return;

        String replaced = String.valueOf(replacementList.get(engIndex));

        event.setCanceled(true);

        chatScreen.input.insertText(replaced);
    }

    private void handleTypedNumber(
            String typedNumber, List<Character> replacementList, EditBoxInsertEvent event, ChatScreen chatScreen) {
        String input = chatScreen.input.getValue();

        String beforeCursor = input.substring(0, chatScreen.input.getCursorPosition());
        String afterCursor = input.substring(chatScreen.input.getCursorPosition());

        if (input.isBlank() || !Services.WynnLanguageSerivce.getNumbers().contains(beforeCursor.charAt(beforeCursor.length() - 1))) {
            int engIndex = Services.WynnLanguageSerivce.getEnglish().indexOf(typedNumber);

            if (engIndex == -1) return;

            event.setCanceled(true);

            String replaced = String.valueOf(replacementList.get(engIndex));

            chatScreen.input.insertText(replaced);

            return;
        }

        event.setCanceled(true);

        String currentNumsStr = getWynnicNumBeforeCursor(beforeCursor);

        int index = beforeCursor.lastIndexOf(currentNumsStr);

        beforeCursor = beforeCursor.substring(0, index);

        int updateNum = calculateWynnicNum(currentNumsStr, typedNumber);

        updateInput(beforeCursor, afterCursor, updateNum, chatScreen.input);
    }

    private String getWynnicNumBeforeCursor(String beforeCursor) {
        StringBuilder currentNumsStr = new StringBuilder();

        List<Character> numbers = Services.WynnLanguageSerivce.getNumbers();

        for (int i = beforeCursor.length() - 1; i >= 0; i--) {
            if (numbers.contains(beforeCursor.charAt(i))) {
                currentNumsStr.append(beforeCursor.charAt(i));
            } else {
                break;
            }
        }

        return currentNumsStr.reverse().toString();
    }

    private void updateInput(String beforeCursor, String afterCursor, int num, EditBox chatInput) {
        String translatedNum = num > MAX_TRANSLATABLE_NUMBER ? "∞" : intToWynnicNum(num);

        String newInput = beforeCursor + translatedNum + afterCursor;

        int newCursorPos = newInput.indexOf(translatedNum) + translatedNum.length();

        chatInput.setValue(newInput);

        chatInput.moveCursorTo(newCursorPos);
    }

    private int calculateWynnicNum(String wynnicNums, String numToAdd) {
        int result = 0;

        for (char num : wynnicNums.toCharArray()) {
            if (num == Services.WynnLanguageSerivce.getOneHundered()) {
                result += 100;
            } else if (num == Services.WynnLanguageSerivce.getFifty()) {
                result += 50;
            } else if (num == Services.WynnLanguageSerivce.getTen()) {
                result += 10;
            } else {
                int wynnIndex = Services.WynnLanguageSerivce.getWynnic().indexOf(num);

                result += Integer.parseInt(Services.WynnLanguageSerivce.getEnglish().get(wynnIndex));
            }
        }

        String resultStr = String.valueOf(result);

        resultStr += numToAdd;

        return Integer.parseInt(resultStr);
    }

    private boolean hasWynnicOrGavellian(String message) {
        for (int i = message.length() - 1; i >= 0; i--) {
            char c = message.charAt(i);

            if (Services.WynnLanguageSerivce.getGavellian().contains(c) || Services.WynnLanguageSerivce.getWynnic().contains(c)) {
                return true;
            }
        }

        return false;
    }

    private StyledText getStyledTextWithTranslation(
            StyledText original, boolean translateWynnic, boolean translateGavellian, boolean npcDialogue) {
        ChatFormatting defaultColor = npcDialogue
                ? ColorChatFormatting.GREEN.getChatFormatting()
                : ColorChatFormatting.WHITE.getChatFormatting();

        return original.iterateBackwards((part, changes) -> {
            String partText = part.getString(null, PartStyle.StyleType.NONE);
            String translatedText = partText;

            if (END_OF_HEADER_PATTERN.matcher(partText).matches()) {
                return IterationDecision.BREAK;
            }

            if (translateWynnic) {
                Matcher numMatcher = WYNNIC_NUMBER_PATTERN.matcher(partText);

                if (coloredTranslations.get()) {
                    translatedText = numMatcher.replaceAll(match -> wynnicColor.get().getChatFormatting()
                            + String.valueOf(wynnicNumToInt(match.group()))
                            + ColorChatFormatting.WHITE.getChatFormatting());
                } else {
                    translatedText = numMatcher.replaceAll(match -> String.valueOf(wynnicNumToInt(match.group())));
                }

                translatedText = getStringWithTranslation(translatedText, WynnLanguage.WYNNIC, coloredTranslations.get(), defaultColor);
            }

            if (translateGavellian) {
                translatedText = getStringWithTranslation(translatedText, WynnLanguage.GAVELLIAN, coloredTranslations.get(), defaultColor);
            }

            changes.remove(part);
            StyledTextPart newPart =
                    new StyledTextPart(translatedText, part.getPartStyle().getStyle(), null, Style.EMPTY);
            changes.add(newPart);

            return IterationDecision.CONTINUE;
        });
    }

    private String getStringWithTranslation(String original, WynnLanguage language, boolean useColors, ChatFormatting defaultColor) {
        String translated = original.toLowerCase(Locale.ROOT);

        if (language == WynnLanguage.GAVELLIAN) {
            for (char character : original.toCharArray()) {
                Character replacement = Services.WynnLanguageSerivce.translateWynnic(character);

                if (!replacement.equals(character)) {
                    if (useColors) {
                        translated = translated.replace(Character.valueOf(character).toString(), wynnicColor.get().getChatFormatting() + replacement.toString() + defaultColor);
                    } else {
                        translated = translated.replace(character, replacement);
                    }
                }
            }
        } else {
            Matcher numMatcher = NUMBER_PATTERN.matcher(translated);

            translated = numMatcher.replaceAll(match -> intToWynnicNum(Integer.parseInt(match.group())));

            for (char character : original.toCharArray()) {
                Character replacement = Services.WynnLanguageSerivce.translateGavellian(character);

                if (!replacement.equals(character)) {
                    if (useColors) {
                        translated = translated.replace(Character.valueOf(character).toString(), gavellianColor.get().getChatFormatting() + replacement.toString() + defaultColor);
                    } else {
                        translated = translated.replace(character, replacement);
                    }
                }
            }
        }

        return translated;
    }

    private int wynnicNumToInt(String wynnicNum) {
        int result = 0;

        for (char num : wynnicNum.toCharArray()) {
            int numIndex = Services.WynnLanguageSerivce.getWynnic().indexOf(num);

            result += Integer.parseInt(Services.WynnLanguageSerivce.getEnglish().get(numIndex));
        }

        return result;
    }

    private String intToWynnicNum(int number) {
        StringBuilder wynnicNums = new StringBuilder();

        List<Character> wynnic = Services.WynnLanguageSerivce.getWynnic();

        int hundereds = number / 100;

        number -= (hundereds * 100);

        int fifties = number >= 50 ? 1 : 0;

        number -= (fifties * 50);

        int tens = number / 10;

        number -= (tens * 10);

        wynnicNums.append(String.valueOf(Services.WynnLanguageSerivce.getOneHundered()).repeat(Math.max(0, hundereds)));
        wynnicNums.append(String.valueOf(Services.WynnLanguageSerivce.getFifty()).repeat(Math.max(0, fifties)));
        wynnicNums.append(String.valueOf(Services.WynnLanguageSerivce.getTen()).repeat(Math.max(0, tens)));

        if (number > 0) {
            wynnicNums.append(wynnic.get(Services.WynnLanguageSerivce.getEnglish().indexOf(Integer.toString(number))));
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
