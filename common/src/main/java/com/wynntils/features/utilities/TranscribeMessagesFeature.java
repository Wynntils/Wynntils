/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.event.NpcDialogEvent;
import com.wynntils.handlers.chat.type.NpcDialogueType;
import com.wynntils.models.wynnalphabet.WynnAlphabet;
import com.wynntils.models.wynnalphabet.type.TranscribeCondition;
import com.wynntils.utils.colors.ColorChatFormatting;
import com.wynntils.utils.type.IterationDecision;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class TranscribeMessagesFeature extends Feature {
    @Persisted
    public final Config<Boolean> transcribeChat = new Config<>(true);

    @Persisted
    public final Config<Boolean> transcribeNpcs = new Config<>(true);

    @Persisted
    public final Config<TranscribeCondition> transcribeCondition = new Config<>(TranscribeCondition.ALWAYS);

    @Persisted
    public final Config<Boolean> coloredTranscriptions = new Config<>(true);

    @Persisted
    public final Config<ColorChatFormatting> gavellianColor = new Config<>(ColorChatFormatting.LIGHT_PURPLE);

    @Persisted
    public final Config<ColorChatFormatting> wynnicColor = new Config<>(ColorChatFormatting.DARK_GREEN);

    private static final Pattern END_OF_HEADER_PATTERN = Pattern.compile(".*[\\]:]\\s?");
    private static final Pattern WYNNIC_NUMBER_PATTERN = Pattern.compile("[⑴-⑿]+");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ChatMessageReceivedEvent event) {
        if (!transcribeChat.get()) return;
        if (!Models.WynnAlphabet.hasWynnicOrGavellian(event.getStyledText().getString())) return;

        boolean transcribeWynnic = Models.WynnAlphabet.shouldTranscribe(transcribeCondition.get(), WynnAlphabet.WYNNIC);
        boolean transcribeGavellian =
                Models.WynnAlphabet.shouldTranscribe(transcribeCondition.get(), WynnAlphabet.GAVELLIAN);

        if (!transcribeWynnic && !transcribeGavellian) return;

        StyledText styledText = event.getStyledText();

        StyledText modified = getStyledTextWithTranscription(styledText, transcribeWynnic, transcribeGavellian, false);

        if (styledText.getString().equalsIgnoreCase(modified.getString())) return;

        event.setMessage(modified.getComponent());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onNpcDialogue(NpcDialogEvent event) {
        if (!transcribeNpcs.get()) return;
        if (!Models.WynnAlphabet.hasWynnicOrGavellian(event.getChatMessage().toString())) return;

        boolean transcribeWynnic = Models.WynnAlphabet.shouldTranscribe(transcribeCondition.get(), WynnAlphabet.WYNNIC);
        boolean transcribeGavellian =
                Models.WynnAlphabet.shouldTranscribe(transcribeCondition.get(), WynnAlphabet.GAVELLIAN);

        if (!transcribeWynnic && !transcribeGavellian) return;

        event.setCanceled(true);

        List<Component> transcriptedComponents = event.getChatMessage().stream()
                .map(styledText -> getStyledTextWithTranscription(
                        StyledText.fromComponent(styledText), transcribeWynnic, transcribeGavellian, true))
                .map(s -> ((Component) s.getComponent()))
                .toList();

        Managers.TickScheduler.scheduleNextTick(() -> {
            NpcDialogEvent transcriptedEvent =
                    new WynnTranscriptedNpcDialogEvent(transcriptedComponents, event.getType(), event.isProtected());
            WynntilsMod.postEvent(transcriptedEvent);
        });
    }

    private StyledText getStyledTextWithTranscription(
            StyledText original, boolean transcribeWynnic, boolean transcribeGavellian, boolean npcDialogue) {
        ChatFormatting defaultColor = npcDialogue
                ? ColorChatFormatting.GREEN.getChatFormatting()
                : ColorChatFormatting.WHITE.getChatFormatting();

        return original.iterateBackwards((part, changes) -> {
            String partText = part.getString(null, PartStyle.StyleType.NONE);
            String transcriptedText = partText;

            if (END_OF_HEADER_PATTERN.matcher(partText).matches()) {
                return IterationDecision.BREAK;
            }

            if (transcribeWynnic) {
                Matcher numMatcher = WYNNIC_NUMBER_PATTERN.matcher(partText);

                if (coloredTranscriptions.get()) {
                    transcriptedText =
                            numMatcher.replaceAll(match -> wynnicColor.get().getChatFormatting()
                                    + String.valueOf(Models.WynnAlphabet.wynnicNumToInt(match.group()))
                                    + ColorChatFormatting.WHITE.getChatFormatting());
                } else {
                    transcriptedText = numMatcher.replaceAll(
                            match -> String.valueOf(Models.WynnAlphabet.wynnicNumToInt(match.group())));
                }

                transcriptedText = Models.WynnAlphabet.transcribeMessageFromWynnAlphabet(
                        transcriptedText,
                        WynnAlphabet.WYNNIC,
                        coloredTranscriptions.get(),
                        wynnicColor.get().getChatFormatting(),
                        defaultColor);
            }

            if (transcribeGavellian) {
                transcriptedText = Models.WynnAlphabet.transcribeMessageFromWynnAlphabet(
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
}
