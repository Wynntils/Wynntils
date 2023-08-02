/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
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
import com.wynntils.models.wynnlanguage.WynnAlphabet;
import com.wynntils.screens.transcription.widgets.WynnAlphabetButton;
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
public class GavellianAndWynnicTranscriptionFeature extends Feature {
    @RegisterConfig
    public final Config<Boolean> transcriptChat = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> transcriptNpcs = new Config<>(true);

    @RegisterConfig
    public final Config<TranscriptCondition> transcriptCondition =
            new Config<>(TranscriptCondition.ALWAYS);

    @RegisterConfig
    public final Config<Boolean> useBrackets = new Config<>(false);

    @RegisterConfig
    public final Config<Boolean> coloredTranscriptions = new Config<>(true);

    @RegisterConfig
    public final Config<ColorChatFormatting> gavellianColor = new Config<>(ColorChatFormatting.LIGHT_PURPLE);

    @RegisterConfig
    public final Config<ColorChatFormatting> wynnicColor = new Config<>(ColorChatFormatting.DARK_GREEN);

    private static final int MAX_CHAT_LENGTH = 256;
    // Numbers higher than this will be replaced with "∞"
    private static final int MAX_TRANSCRIPTABLE_NUMBER = 5000;
    private static final Pattern END_OF_HEADER_PATTERN = Pattern.compile(".*[\\]:]\\s?");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern WYNNIC_NUMBER_PATTERN = Pattern.compile("[⑴-⑿]+");

    // If possible, having the selected language be set back to default when opening chat screen from the command
    // keybind would be good, but chatScreen.input is null here
    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        if (useBrackets.get()) return;

        if (event.getScreen() instanceof ChatScreen chatScreen) {
            chatScreen.width -= 45;
            int xOffset = chatScreen.width + 1;

            addLanguageButton(chatScreen, xOffset, WynnAlphabet.DEFAULT);
            xOffset += 15;

            addLanguageButton(chatScreen, xOffset, WynnAlphabet.WYNNIC);
            xOffset += 15;

            addLanguageButton(chatScreen, xOffset, WynnAlphabet.GAVELLIAN);
        }
    }

    @SubscribeEvent
    public void onEditBoxInsert(EditBoxInsertEvent event) {
        if (!(McUtils.mc().screen instanceof ChatScreen chatScreen)) return;
        if (useBrackets.get()) return;
        if (event.getTextToWrite().isBlank()) return;

        WynnAlphabet selectedLanguage = Models.WynnLanguage.getSelectedLanguage();

        if (selectedLanguage == WynnAlphabet.DEFAULT) return;

        // Can't use parseInt here as '０', '１', '２' are used for '.', '!', '?' in Wynnic
        Matcher numMatcher = NUMBER_PATTERN.matcher(event.getTextToWrite());

        if (numMatcher.matches()) {
            if (selectedLanguage != WynnAlphabet.WYNNIC) return;

            handleTypedNumber(
                    Integer.parseInt(event.getTextToWrite()),
                    Models.WynnLanguage.getWynnicNumbers(),
                    event,
                    chatScreen);
        } else {
            List<Character> replacementList = selectedLanguage == WynnAlphabet.GAVELLIAN
                    ? Models.WynnLanguage.getGavellianCharacters()
                    : Models.WynnLanguage.getWynnicCharacters();

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

        if (beforeCursor.isBlank()
                || !Models.WynnLanguage.getWynnicNumbers().contains(beforeCursor.charAt(beforeCursor.length() - 1)))
            return;

        String currentNumsStr = getWynnicNumBeforeCursor(beforeCursor);

        int wynnicNum = Models.WynnLanguage.wynnicNumToInt(currentNumsStr);

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
            String updatedMessage = transcriptSentMessage(message, event);

            updatedMessage = updatedMessage.substring(0, Math.min(updatedMessage.length(), MAX_CHAT_LENGTH));

            McUtils.mc().getConnection().sendChat(updatedMessage);
        }
    }

    @SubscribeEvent
    public void onCommandSent(CommandSentEvent event) {
        String command = event.getCommand();

        if (useBrackets.get() && containsBrackets(command)) {
            String updatedCommand = transcriptSentMessage(command, event);

            updatedCommand = updatedCommand.substring(0, Math.min(updatedCommand.length(), MAX_CHAT_LENGTH));

            McUtils.sendCommand(updatedCommand);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ChatMessageReceivedEvent event) {
        if (!transcriptChat.get()) return;
        if (!Models.WynnLanguage.hasWynnicOrGavellian(event.getStyledText().getString())) return;

        boolean transcriptWynnic =
                Models.WynnLanguage.shouldTranscript(transcriptCondition.get(), WynnAlphabet.WYNNIC);
        boolean transcriptGavellian =
                Models.WynnLanguage.shouldTranscript(transcriptCondition.get(), WynnAlphabet.GAVELLIAN);

        if (!transcriptWynnic && !transcriptGavellian) return;

        StyledText styledText = event.getStyledText();

        StyledText modified =
                getStyledTextWithTranscription(styledText, transcriptWynnic, transcriptGavellian, false);

        if (styledText.getString().equalsIgnoreCase(modified.getString())) return;

        event.setMessage(modified.getComponent());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onNpcDialogue(NpcDialogEvent event) {
        if (!transcriptNpcs.get()) return;
        if (!Models.WynnLanguage.hasWynnicOrGavellian(event.getChatMessage().toString())) return;

        boolean transcriptWynnic =
                Models.WynnLanguage.shouldTranscript(transcriptCondition.get(), WynnAlphabet.WYNNIC);
        boolean transcriptGavellian =
                Models.WynnLanguage.shouldTranscript(transcriptCondition.get(), WynnAlphabet.GAVELLIAN);

        if (!transcriptWynnic && !transcriptGavellian) return;

        event.setCanceled(true);

        if (!event.getChatMessage().isEmpty()) {
            List<Component> transcriptedComponents = event.getChatMessage().stream()
                    .map(styledText -> getStyledTextWithTranscription(
                            StyledText.fromComponent(styledText), transcriptWynnic, transcriptGavellian, true))
                    .map(s -> ((Component) s.getComponent()))
                    .toList();

            Managers.TickScheduler.scheduleNextTick(() -> {
                NpcDialogEvent transcriptedEvent = new WynnTranscriptedNpcDialogEvent(
                        transcriptedComponents, event.getType(), event.isProtected());
                WynntilsMod.postEvent(transcriptedEvent);
            });
        } else {
            NpcDialogEvent transcriptedEvent =
                    new WynnTranscriptedNpcDialogEvent(List.of(), event.getType(), event.isProtected());
            WynntilsMod.postEvent(transcriptedEvent);
        }
    }

    private void addLanguageButton(ChatScreen chatScreen, int xOffset, WynnAlphabet language) {
        chatScreen.addRenderableWidget(new WynnAlphabetButton(xOffset, chatScreen.height - 14, 12, 12, language));
    }

    private boolean containsBrackets(String message) {
        return (message.contains("{") && message.contains("}")) || (message.contains("<") && message.contains(">"));
    }

    private String transcriptSentMessage(String message, Event event) {
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
            String transcriptedText = Models.WynnLanguage.getSentMessageWithTranscription(
                    wynnicText.toLowerCase(Locale.ROOT), WynnAlphabet.WYNNIC);
            replaceTranscripted(updatedMessage, "{" + wynnicText + "}", transcriptedText);
        }

        for (String gavellianText : gavellianSubstring) {
            String transcriptedText = Models.WynnLanguage.getSentMessageWithTranscription(
                    gavellianText.toLowerCase(Locale.ROOT), WynnAlphabet.GAVELLIAN);
            replaceTranscripted(updatedMessage, "<" + gavellianText + ">", transcriptedText);
        }

        return updatedMessage.toString();
    }

    private void replaceTranscripted(StringBuilder stringBuilder, String original, String replacement) {
        int index = stringBuilder.indexOf(original);

        while (index != -1) {
            stringBuilder.replace(index, index + original.length(), replacement);
            index = stringBuilder.indexOf(original, index + replacement.length());
        }
    }

    private void handleTypedCharacter(
            String typedChar, List<Character> replacementList, EditBoxInsertEvent event, ChatScreen chatScreen) {
        int engIndex = Models.WynnLanguage.getEnglishCharacters()
                .indexOf(typedChar.toLowerCase(Locale.ROOT).charAt(0));

        if (engIndex == -1) return;
        if (engIndex >= replacementList.size()) return;

        String replaced = String.valueOf(replacementList.get(engIndex));

        event.setCanceled(true);

        chatScreen.input.insertText(replaced);
    }

    private void handleTypedNumber(
            int typedNumber, List<Character> replacementList, EditBoxInsertEvent event, ChatScreen chatScreen) {
        String input = chatScreen.input.getValue();

        String beforeCursor = input.substring(0, chatScreen.input.getCursorPosition());
        String afterCursor = input.substring(chatScreen.input.getCursorPosition());

        if (input.isBlank()
                || !Models.WynnLanguage.getWynnicNumbers().contains(beforeCursor.charAt(beforeCursor.length() - 1))) {
            int numIndex = Models.WynnLanguage.getEnglishNumbers().indexOf(typedNumber);

            if (numIndex == -1) return;

            event.setCanceled(true);

            String replaced = String.valueOf(replacementList.get(numIndex));

            chatScreen.input.insertText(replaced);

            return;
        }

        event.setCanceled(true);

        String currentNumsStr = getWynnicNumBeforeCursor(beforeCursor);

        int index = beforeCursor.lastIndexOf(currentNumsStr);

        beforeCursor = beforeCursor.substring(0, index);

        int updateNum = Models.WynnLanguage.calculateWynnicNum(currentNumsStr, typedNumber);

        updateInput(beforeCursor, afterCursor, updateNum, chatScreen.input);
    }

    private String getWynnicNumBeforeCursor(String beforeCursor) {
        StringBuilder currentNumsStr = new StringBuilder();

        List<Character> numbers = Models.WynnLanguage.getWynnicNumbers();

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
        String transcriptedNum = num > MAX_TRANSCRIPTABLE_NUMBER ? "∞" : Models.WynnLanguage.intToWynnicNum(num);

        String newInput = beforeCursor + transcriptedNum + afterCursor;

        if (newInput.length() > MAX_CHAT_LENGTH) return;

        int newCursorPos = beforeCursor.length() + transcriptedNum.length();

        chatInput.setValue(newInput);

        chatInput.moveCursorTo(newCursorPos);
    }

    private StyledText getStyledTextWithTranscription(
            StyledText original, boolean transcriptWynnic, boolean transcriptGavellian, boolean npcDialogue) {
        ChatFormatting defaultColor = npcDialogue
                ? ColorChatFormatting.GREEN.getChatFormatting()
                : ColorChatFormatting.WHITE.getChatFormatting();

        return original.iterateBackwards((part, changes) -> {
            String partText = part.getString(null, PartStyle.StyleType.NONE);
            String transcriptedText = partText;

            if (END_OF_HEADER_PATTERN.matcher(partText).matches()) {
                return IterationDecision.BREAK;
            }

            if (transcriptWynnic) {
                Matcher numMatcher = WYNNIC_NUMBER_PATTERN.matcher(partText);

                if (coloredTranscriptions.get()) {
                    transcriptedText =
                            numMatcher.replaceAll(match -> wynnicColor.get().getChatFormatting()
                                    + String.valueOf(Models.WynnLanguage.wynnicNumToInt(match.group()))
                                    + ColorChatFormatting.WHITE.getChatFormatting());
                } else {
                    transcriptedText = numMatcher.replaceAll(
                            match -> String.valueOf(Models.WynnLanguage.wynnicNumToInt(match.group())));
                }

                transcriptedText = Models.WynnLanguage.getStringWithTranscription(
                        transcriptedText,
                        WynnAlphabet.WYNNIC,
                        coloredTranscriptions.get(),
                        wynnicColor.get().getChatFormatting(),
                        defaultColor);
            }

            if (transcriptGavellian) {
                transcriptedText = Models.WynnLanguage.getStringWithTranscription(
                        transcriptedText,
                        WynnAlphabet.GAVELLIAN,
                        coloredTranscriptions.get(),
                        gavellianColor.get().getChatFormatting(),
                        defaultColor);
            }

            changes.remove(part);
            StyledTextPart newPart =
                    new StyledTextPart(transcriptedText, part.getPartStyle().getStyle(), null, Style.EMPTY);
            changes.add(newPart);

            return IterationDecision.CONTINUE;
        });
    }

    private static class WynnTranscriptedNpcDialogEvent extends NpcDialogEvent {
        protected WynnTranscriptedNpcDialogEvent(List<Component> chatMsg, NpcDialogueType type, boolean isProtected) {
            super(chatMsg, type, isProtected);
        }
    }

    public enum TranscriptCondition {
        ALWAYS,
        DISCOVERY,
        TRANSCRIBER,
        NEVER
    }
}
