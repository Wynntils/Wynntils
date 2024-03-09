/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

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
import com.wynntils.models.npcdialogue.event.NpcDialogEvent;
import com.wynntils.models.npcdialogue.type.NpcDialogue;
import com.wynntils.models.wynnalphabet.WynnAlphabet;
import com.wynntils.models.wynnalphabet.type.TranscribeCondition;
import com.wynntils.utils.colors.ColorChatFormatting;
import com.wynntils.utils.type.IterationDecision;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
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
    public final Config<Boolean> showTooltip = new Config<>(false);

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

        NpcDialogue dialogue = event.getDialogue();
        if (dialogue.currentDialogue().stream()
                .anyMatch(text -> Models.WynnAlphabet.hasWynnicOrGavellian(text.getStringWithoutFormatting()))) {
            return;
        }

        boolean transcribeWynnic = Models.WynnAlphabet.shouldTranscribe(transcribeCondition.get(), WynnAlphabet.WYNNIC);
        boolean transcribeGavellian =
                Models.WynnAlphabet.shouldTranscribe(transcribeCondition.get(), WynnAlphabet.GAVELLIAN);

        if (!transcribeWynnic && !transcribeGavellian) return;

        // FIXME: Reimplement
        //        if (!showTooltip.get()) {
        //            event.setCanceled(true);
        //        }

        List<Component> transcriptedComponents = dialogue.currentDialogue().stream()
                .map(styledText ->
                        getStyledTextWithTranscription(styledText, transcribeWynnic, transcribeGavellian, true))
                .map(s -> ((Component) s.getComponent()))
                .toList();

        // FIXME: Reimplement
        //        if (showTooltip.get()) {
        //            for (Component transcriptedComponent : transcriptedComponents) {
        //                McUtils.sendMessageToClient(transcriptedComponent);
        //            }
        //        } else {
        //            Managers.TickScheduler.scheduleNextTick(() -> {
        //                NpcDialogEvent transcriptedEvent = new WynnTranscriptedNpcDialogEvent(
        //                        transcriptedComponents, event.getType(), event.isProtected());
        //                WynntilsMod.postEvent(transcriptedEvent);
        //            });
        //        }
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

            StyledTextPart newPart;

            if (transcribeGavellian || transcribeWynnic) {
                String text = showTooltip.get() ? partText : transcriptedText;
                Component hoverComponent = (npcDialogue || showTooltip.get())
                        ? Component.literal(transcriptedText)
                        : Component.translatable("feature.wynntils.transcribeMessages.transcribedFrom", partText);
                Style style = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent));

                newPart = new StyledTextPart(text, style, null, Style.EMPTY);
            } else {
                newPart = part;
            }

            changes.remove(part);
            changes.add(newPart);

            return IterationDecision.CONTINUE;
        });
    }
}
