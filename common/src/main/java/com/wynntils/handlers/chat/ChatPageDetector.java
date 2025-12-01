/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.SystemMessageEvent;
import com.wynntils.utils.ListUtils;
import com.wynntils.utils.TaskUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import net.minecraft.client.GuiMessage;
import net.minecraft.network.chat.Component;

public final class ChatPageDetector {
    private static final int PAGE_BACKGROUND_MESSAGES = 4;
    private static final int MIN_MATCHING_LINES = 5;
    private static final int MAX_DIFFERING_LINES = 5;
    private static final int NORMAL_PAGE_WAIT = 20;
    private static final int PARTIAL_PAGE_WAIT = 60;

    // These must only be accessed while synchronized on 'this'
    private int lastPartialLinesCount = 0;
    private Future<?> pageFinishedTask = null;
    private Deque<Component> collectedMessages = new ArrayDeque<>();

    private List<Runnable> tasksAtNextTick = new ArrayList<>();
    private List<StyledText> pageBackground = null;
    private List<StyledText> pageContent = List.of();
    private List<StyledText> sentBackgroundLines = new ArrayList<>();
    private List<StyledText> lastForegroundLines = null;

    public boolean isInPageMode() {
        return pageBackground != null;
    }

    public List<StyledText> getPageContent() {
        return pageContent;
    }

    public void reset() {
        // Reset in case of world state change
        collectedMessages = new ArrayDeque<>();
        pageBackground = null;
        pageContent = List.of();
        sentBackgroundLines = new ArrayList<>();
        Handlers.Chat.handlePage(List.of());
    }

    public void onTick() {
        ArrayList<Runnable> tasksToRun;
        synchronized (this) {
            tasksToRun = new ArrayList<>(tasksAtNextTick);
            tasksAtNextTick.clear();
        }
        tasksToRun.forEach(Runnable::run);
    }

    public boolean processIncomingChatMessage(SystemMessageEvent.ChatReceivedEvent event) {
        Component message = event.getMessage();
        StyledText styledText = StyledText.fromComponent(message);

        int lineCount = StyledTextUtils.getLineCount(styledText);
        synchronized (this) {
            if (pageFinishedTask == null) {
                if (lineCount == 1) {
                    // Normal single line chat messages will just be passed through
                    if (!pageContent.isEmpty() && !Handlers.Chat.isLocalMessage()) {
                        // We think we are in page mode, and this is not a locally sent message,
                        // so we have messed up. Delete the current content page and tell the page
                        // processor to remove the page.
                        reset();
                    }

                    return false;
                }

                // Wait a reasonable amount of time for all messages in the page to arrive
                pageFinishedTask =
                        TaskUtils.schedule(this::onPotentialPageFinished, NORMAL_PAGE_WAIT, TimeUnit.MILLISECONDS);
            }

            // Once we started collecting messages, collect them all, so as not to change order
            // in chat, even if they are single line
            collectedMessages.addLast(message);
        }
        event.setCanceled(true);
        return true;
    }

    private void onPotentialPageFinished() {
        Deque<Component> pageMessages;

        synchronized (this) {
            // Do we have a partial page that might get more messages?
            if (isPartialPage(collectedMessages) && collectedMessages.size() != lastPartialLinesCount) {
                // Make sure we don't end up back here if no new messages arrive
                lastPartialLinesCount = collectedMessages.size();
                // In this case, the rest of the page will likely be sent the next tick,
                // so we need to wait a longer time than normal.
                pageFinishedTask =
                        TaskUtils.schedule(this::onPotentialPageFinished, PARTIAL_PAGE_WAIT, TimeUnit.MILLISECONDS);
                return;
            }
            lastPartialLinesCount = 0;

            // Reset message collection
            pageMessages = collectedMessages;
            collectedMessages = new ArrayDeque<>();
            pageFinishedTask = null;
        }

        // We've held back some multi-line messages. They can either be a page, or not.
        if (!isPage(pageMessages)) {
            // Not a page, so we just send them out the way they were
            for (Component message : pageMessages) {
                enqueueSendDelayedChat(message);
            }
        } else {
            // We might have multiple pages; split them up and handle each of them
            splitMultiplePages(pageMessages).forEach(this::handlePage);
        }
    }

    private boolean isPartialPage(Deque<Component> collectedMessages) {
        // We deem it to be a partial page if there are less than 4 messages, and none of them are single-line,
        // and they all match the beginning of the page background. This is not perfect, but it is an
        // acceptable heuristic that will catch most of these odd cases.
        if (collectedMessages.size() >= PAGE_BACKGROUND_MESSAGES) return false;
        if (pageBackground == null) return false;

        List<StyledText> partialPage = new ArrayList<>();
        for (Component message : collectedMessages) {
            StyledText styledText = StyledText.fromComponent(message);

            List<StyledText> lines = List.of(styledText.split("\n", true));
            // If single-line items are included, it's not a partial page
            if (lines.size() == 1) return false;

            partialPage.addAll(lines);
        }

        int matchingLines = ListUtils.countMatchingElements(pageBackground, 0, partialPage, 0);
        return matchingLines == partialPage.size();
    }

    private boolean isPage(Deque<Component> collectedMessages) {
        // It is a page if there are at least 4 messages, and none of the messages are
        // single-line
        if (collectedMessages.size() < PAGE_BACKGROUND_MESSAGES) return false;
        for (Component message : collectedMessages) {
            StyledText styledText = StyledText.fromComponent(message);

            if (StyledTextUtils.getLineCount(styledText) == 1) return false;
        }
        return true;
    }

    public List<Deque<Component>> splitMultiplePages(Deque<Component> collectedMessages) {
        // If there is not at least 4 messages, there cannot be multiple pages
        if (collectedMessages.size() < PAGE_BACKGROUND_MESSAGES) return List.of(new ArrayDeque<>(collectedMessages));

        int lastPageStart = 0;
        List<Component> messages = new ArrayList<>(collectedMessages);
        List<Deque<Component>> separatedPages = new ArrayList<>();

        int i = 0;
        while (i < messages.size()) {
            // Start by copying the background
            Deque<Component> page = new ArrayDeque<>(messages.subList(i, i + PAGE_BACKGROUND_MESSAGES));
            i += PAGE_BACKGROUND_MESSAGES;

            while (i < messages.size()) {
                if (isPageStart(messages, lastPageStart, i)) {
                    lastPageStart = i;
                    break;
                }

                // Add content line
                page.add(messages.get(i));
                i++;
            }
            separatedPages.add(page);
        }
        return separatedPages;
    }

    private boolean isPageStart(List<Component> messageList, int oldPageIndex, int startIndex) {
        // Check if there are at least 4 messages remaining
        if (startIndex > messageList.size() - PAGE_BACKGROUND_MESSAGES) return false;

        // If the first 4 messages are equal, then it's a page start
        if (ListUtils.countMatchingElements(messageList, oldPageIndex, messageList, startIndex)
                >= PAGE_BACKGROUND_MESSAGES) {
            return true;
        }

        // It could be the case that there have appeared a few new lines, possibly pushing
        // old lines out of the way. To handle this, we try to find a matching segment
        // of lines, allowing for a few lines difference at the start and end.

        // Split all components into lines
        List<StyledText> referenceLines =
                messageList.subList(oldPageIndex, oldPageIndex + PAGE_BACKGROUND_MESSAGES).stream()
                        .flatMap(this::splitIntoLines)
                        .toList();
        List<StyledText> compareLines = messageList.subList(startIndex, startIndex + PAGE_BACKGROUND_MESSAGES).stream()
                .flatMap(this::splitIntoLines)
                .toList();
        int numReferenceLines = referenceLines.size();

        // Try to find a matching segment allowing for up to MAX_DIFFERING_LINES lines difference
        // at the beginning and end
        int minRequiredMatches = numReferenceLines - 2 * MAX_DIFFERING_LINES;

        for (int refStart = 0; refStart <= MAX_DIFFERING_LINES; refStart++) {
            for (int i = 0; i < numReferenceLines - refStart; i++) {
                if (i >= minRequiredMatches) {
                    return true;
                }
                if (i >= compareLines.size()) {
                    break;
                }
                if (!referenceLines
                        .get(refStart + i)
                        .getStringWithoutFormatting()
                        .equals(compareLines.get(i).getStringWithoutFormatting())) {
                    break;
                }
            }
        }

        return false;
    }

    private void handlePage(Deque<Component> collectedMessages) {
        Pair<List<StyledText>, List<StyledText>> page = splitPage(collectedMessages);
        List<StyledText> background = page.a();
        List<StyledText> pageContent = page.b();

        handleBackground(background, collectedMessages.size());

        enqueueSendPageContent(pageContent);
    }

    private Pair<List<StyledText>, List<StyledText>> splitPage(Deque<Component> collectedMessages) {
        // The first four messages are always background, and the rest is page content
        List<StyledText> background = collectedMessages.stream()
                .limit(PAGE_BACKGROUND_MESSAGES)
                .flatMap(this::splitIntoLines)
                .toList();
        List<StyledText> pageContent = collectedMessages.stream()
                .skip(PAGE_BACKGROUND_MESSAGES)
                .flatMap(this::splitIntoLines)
                .toList();

        return Pair.of(background, pageContent);
    }

    private Stream<StyledText> splitIntoLines(Component message) {
        StyledText styledText = StyledText.fromComponent(message);
        List<StyledText> lines = List.of(styledText.split("\n", true));
        return lines.stream();
    }

    private void handleBackground(List<StyledText> background, int numCollectedMessages) {
        if (lastForegroundLines != null) {
            int matchingLines = ListUtils.countMatchingElements(lastForegroundLines, 0, background, 0);
            if (matchingLines == background.size()) {
                // We got a repeated foreground screen. This apparently happens from time to time.
                // Just ignore it.
                return;
            }
        }
        lastForegroundLines = null;

        List<StyledText> oldBackground = pageBackground;
        pageBackground = background;

        // If this is the first background page, we can't calculate a diff
        if (oldBackground == null) return;

        List<StyledText> newBackgroundMessages = getMessageDiff(oldBackground, background);
        if (newBackgroundMessages != null) {
            for (StyledText message : newBackgroundMessages) {
                // Handle new messages that arrived in the background
                sentBackgroundLines.add(message);
                enqueueSendBackgroundLine(message);
            }
        } else {
            // We failed to calculate a diff.
            if (numCollectedMessages == PAGE_BACKGROUND_MESSAGES) {
                // This was the last page, and the "background" is actually a redraw
                // of the foreground. The page mode is now finished.
                List<StyledText> sentLines = List.copyOf(sentBackgroundLines);

                pageBackground = null;
                pageContent = List.of();
                sentBackgroundLines.clear();
                lastForegroundLines = background;

                enqueueSendForegroundReplacements(background, oldBackground, sentLines);
            } else {
                // We could not calculate a diff, and it is not a foreground page. This is bad.
                // To not lose any messages, just resend all.
                WynntilsMod.error("Could not calculate updated background messages");
                for (StyledText message : background) {
                    // Handle new messages that arrived in the background
                    sentBackgroundLines.add(message);
                    enqueueSendBackgroundLine(message);
                }
                // Also, since we've probably messed up royally by now, let's just restart
                reset();
            }
        }
    }

    /**
     * Returns the new lines at the end of 'newBackground' that are not present in 'previousBackground'.
     * Tries all possible alignments of newBackground in previousBackground, requiring at least MIN_MATCHING_LINES.
     * If no sufficient match is found, returns the entire newBackground.
     */
    private List<StyledText> getMessageDiff(List<StyledText> oldBackground, List<StyledText> newBackground) {
        if (newBackground.isEmpty()) return List.of();
        if (oldBackground.isEmpty()) return newBackground;

        int oldSize = oldBackground.size();
        int newSize = newBackground.size();

        // Sometimes Wynn includes links and hovers, and sometimes it does not. Strip them
        // make a proper comparison.
        List<StyledText> strippedOldBackground = StyledTextUtils.stripEventsAndLinks(oldBackground);
        List<StyledText> strippedNewBackground = StyledTextUtils.stripEventsAndLinks(newBackground);

        for (int pos = 0; pos < oldSize; pos++) {
            int matchCount = ListUtils.countMatchingElements(strippedOldBackground, pos, strippedNewBackground, 0);
            int remainingOld = oldSize - pos;

            if (matchCount == remainingOld && matchCount >= MIN_MATCHING_LINES) {
                // All remaining lines in previousBackground matched with start of newBackground
                if (newSize > remainingOld) {
                    return newBackground.subList(remainingOld, newSize);
                } else {
                    return List.of();
                }
            }
        }

        // No sufficient match found
        return null;
    }

    private List<Pair<StyledText, StyledText>> calculateForegroundReplacements(
            List<StyledText> lastBackground, List<StyledText> foreground, List<StyledText> sentBackgroundLines) {
        List<Pair<StyledText, StyledText>> replacements = new LinkedList<>();
        int lastBackgroundStartPos = lastBackground.size() - sentBackgroundLines.size();
        if (lastBackground.size() != foreground.size()) {
            WynntilsMod.warn("Page size mismatch in foreground replacements, skipping");
            return List.of();
        }
        for (int i = 0; i < sentBackgroundLines.size(); i++) {
            if (!lastBackground
                    .get(lastBackgroundStartPos + i)
                    .getString()
                    .equals(sentBackgroundLines.get(i).getString())) {
                WynntilsMod.warn(
                        "Line mismatch in foreground replacements, skipping line: " + sentBackgroundLines.get(i));
                continue;
            }
            // Store in reverse order to match chat history later on
            replacements.addFirst(Pair.of(sentBackgroundLines.get(i), foreground.get(lastBackgroundStartPos + i)));
        }
        return replacements;
    }

    private static void processChatComponentReplacements(
            List<GuiMessage> allMessages, List<Pair<StyledText, StyledText>> replacements) {
        List<Pair<StyledText, StyledText>> remainingReplacements = new LinkedList<>(replacements);

        // Go through all messages from newest to oldest
        for (int i = 0; i < allMessages.size() && !remainingReplacements.isEmpty(); i++) {
            GuiMessage guiMessage = allMessages.get(i);
            Component content = guiMessage.content();
            StyledText styledText = StyledText.fromComponent(content);

            // Check if this message matches any remaining replacement
            for (int j = 0; j < remainingReplacements.size(); j++) {
                Pair<StyledText, StyledText> replacement = remainingReplacements.get(j);
                if (styledText.getString().equals(replacement.a().getString())) {
                    // Found a match - apply the replacement
                    Component newContent = replacement.b().getComponent();
                    GuiMessage newMessage = new GuiMessage(
                            guiMessage.addedTime(), newContent, guiMessage.signature(), guiMessage.tag());
                    allMessages.set(i, newMessage);

                    // Remove this replacement and all preceding ones
                    for (int k = 0; k <= j; k++) {
                        remainingReplacements.removeFirst();
                    }
                    break;
                }
            }
        }
    }

    private void enqueueSendDelayedChat(Component message) {
        synchronized (this) {
            tasksAtNextTick.add(new SendDelayedChatTask(message));
        }
    }

    private void enqueueSendBackgroundLine(StyledText message) {
        synchronized (this) {
            tasksAtNextTick.add(new SendBackgroundLineTask(message));
        }
    }

    private void enqueueSendForegroundReplacements(
            List<StyledText> background, List<StyledText> oldBackground, List<StyledText> sentLines) {
        // Tell the chat to update the previous background
        // messages to show with the proper colors, now that we know them.

        List<Pair<StyledText, StyledText>> replacements =
                calculateForegroundReplacements(oldBackground, background, sentLines);
        synchronized (this) {
            tasksAtNextTick.add(new SendForegroundReplacementsTask(replacements));
        }
    }

    private void enqueueSendPageContent(List<StyledText> pageContent) {
        this.pageContent = pageContent;

        synchronized (this) {
            tasksAtNextTick.add(new SendPageContentTask(pageContent));
        }
    }

    private static final class SendDelayedChatTask implements Runnable {
        private final Component message;

        private SendDelayedChatTask(Component message) {
            this.message = message;
        }

        @Override
        public void run() {
            Handlers.Chat.sendDelayedChat(message);
        }
    }

    private static final class SendBackgroundLineTask implements Runnable {
        private final StyledText message;

        private SendBackgroundLineTask(StyledText message) {
            this.message = message;
        }

        @Override
        public void run() {
            Handlers.Chat.handleBackgroundLine(message);
        }
    }

    private static final class SendForegroundReplacementsTask implements Runnable {
        private List<Pair<StyledText, StyledText>> replacements;

        private SendForegroundReplacementsTask(List<Pair<StyledText, StyledText>> replacements) {
            this.replacements = replacements;
        }

        @Override
        public void run() {
            Services.ChatTab.modifyChatHistory(
                    allMessages -> processChatComponentReplacements(allMessages, replacements));
        }
    }

    private static final class SendPageContentTask implements Runnable {
        private final List<StyledText> pageContent;

        private SendPageContentTask(List<StyledText> pageContent) {
            this.pageContent = pageContent;
        }

        @Override
        public void run() {
            Handlers.Chat.handlePage(pageContent);
        }
    }
}
