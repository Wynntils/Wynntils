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
import com.wynntils.services.chat.WrappingChatComponent;
import com.wynntils.utils.ListUtils;
import com.wynntils.utils.TaskUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;

public final class ChatPageDetector {
    private static final int MIN_MATCHING_LINES = 5;
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
    }

    public void onTick() {
        tasksAtNextTick.forEach(Runnable::run);
        tasksAtNextTick.clear();
    }

    public boolean processIncomingChatMessage(SystemMessageEvent.ChatReceivedEvent event) {
        Component message = event.getMessage();
        StyledText styledText = StyledText.fromComponent(message);

        int lineCount = StyledTextUtils.getLineCount(styledText);
        synchronized (this) {
            if (pageFinishedTask == null) {
                // Normal single line chat messages will just be passed through
                if (lineCount == 1) return false;

                // Wait a reasonable amount of time for all messages in the page to arrive
                pageFinishedTask = TaskUtils.schedule(
                        this::onPotentialPageFinished, NORMAL_PAGE_WAIT, java.util.concurrent.TimeUnit.MILLISECONDS);
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
                pageFinishedTask = TaskUtils.schedule(
                        this::onPotentialPageFinished, PARTIAL_PAGE_WAIT, java.util.concurrent.TimeUnit.MILLISECONDS);
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
        // We deeem it to be a partial page if there are less than 4 messages, and none of them are single-line,
        // and they all match the beginning of the page background. This is not perfect, but it is an
        // acceptable heuristic that will catch most of these odd cases.
        if (collectedMessages.size() >= 4) return false;
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
        if (collectedMessages.size() < 4) return false;
        for (Component message : collectedMessages) {
            StyledText styledText = StyledText.fromComponent(message);

            if (StyledTextUtils.getLineCount(styledText) == 1) return false;
        }
        return true;
    }

    public List<Deque<Component>> splitMultiplePages(Deque<Component> collectedMessages) {
        // If there is not at least 4 messages, there cannot be multiple pages
        if (collectedMessages.size() < 4) return List.of(new ArrayDeque<>(collectedMessages));

        // We might have two pages in a row where the first is background, and the
        // second is foreground, so we need to ignore colors and styles when comparing,
        // thus the need for a parallel stripped list.
        List<Component> formattedList = new ArrayList<>(collectedMessages);
        List<String> strippedList = collectedMessages.stream()
                .map(c -> StyledText.fromComponent(c).getStringWithoutFormatting())
                .toList();
        // We use this as a marker to determine where a new page starts
        List<String> strippedBackground = List.copyOf(strippedList.subList(0, 4));

        List<Deque<Component>> separatedPages = new ArrayList<>();
        int collectedMessagesCount = collectedMessages.size();

        int i = 0;
        while (i < collectedMessagesCount) {
            // Start by copying the background
            Deque<Component> page = new ArrayDeque<>(formattedList.subList(i, i + 4));
            i += 4;

            while (i < collectedMessagesCount) {
                boolean nextIsBackground = (i <= (collectedMessagesCount - 4))
                        && (ListUtils.countMatchingElements(strippedBackground, 0, strippedList, i) >= 4);
                if (nextIsBackground) break;

                // Add content line
                page.add(formattedList.get(i));
                i++;
            }
            separatedPages.add(page);
        }
        return separatedPages;
    }

    private void handlePage(Deque<Component> collectedMessages) {
        Pair<List<StyledText>, List<StyledText>> page = splitPage(collectedMessages);
        List<StyledText> background = page.a();
        List<StyledText> pageContent = page.b();

        handleBackground(background, collectedMessages.size());

        enqueueSendPageContent(pageContent, false);
    }

    private Pair<List<StyledText>, List<StyledText>> splitPage(Deque<Component> collectedMessages) {
        // The first four messages are always background, and the rest is page content
        List<StyledText> background = collectedMessages.stream()
                .limit(4)
                .flatMap(this::splitIntoLines)
                .toList();
        List<StyledText> pageContent =
                collectedMessages.stream().skip(4).flatMap(this::splitIntoLines).toList();

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
            if (numCollectedMessages == 4) {
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

        for (int pos = 0; pos < oldSize; pos++) {
            int matchCount = ListUtils.countMatchingElements(oldBackground, pos, newBackground, 0);
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
                WynntilsMod.warn("Line mismatch in foreground replacements, skipping");
                return List.of();
            }
            // Store in reverse order to match chat history later on
            replacements.addFirst(Pair.of(sentBackgroundLines.get(i), foreground.get(lastBackgroundStartPos + i)));
        }
        return replacements;
    }

    private static void processChatComponentReplacements(
            ChatComponent chatComponent, List<Pair<StyledText, StyledText>> replacements) {
        List<GuiMessage> allMessages = chatComponent.allMessages;
        List<Pair<StyledText, StyledText>> remainingReplacements = new LinkedList<>(replacements);

        // Go through all messages from newest to oldest
        for (int i = 0; i < allMessages.size() && !remainingReplacements.isEmpty(); i++) {
            GuiMessage guiMessage = allMessages.get(i);
            Component content = guiMessage.content();
            StyledText styledText = StyledText.fromComponent(content);

            // Check if this message matches any remaining replacement
            for (int j = 0; j < remainingReplacements.size(); j++) {
                Pair<StyledText, StyledText> replacement = remainingReplacements.get(j);
                if (styledText.equals(replacement.a())) {
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

        chatComponent.refreshTrimmedMessages();
    }

    private void enqueueSendDelayedChat(Component message) {
        tasksAtNextTick.add(new SendDelayedChatTask(message));
    }

    private void enqueueSendBackgroundLine(StyledText message) {
        tasksAtNextTick.add(new SendBackgroundLineTask(message));
    }

    private void enqueueSendForegroundReplacements(
            List<StyledText> background, List<StyledText> oldBackground, List<StyledText> sentLines) {
        // Tell the chat to update the previous background
        // messages to show with the proper colors, now that we know them.

        List<Pair<StyledText, StyledText>> replacements =
                calculateForegroundReplacements(oldBackground, background, sentLines);
        tasksAtNextTick.add(new SendForegroundReplacementsTask(replacements));
    }

    private void enqueueSendPageContent(List<StyledText> pageContent, boolean lastPage) {
        this.pageContent = pageContent;

        tasksAtNextTick.add(new SendPageContentTask(pageContent, lastPage));
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
            ChatComponent chatComponent = McUtils.mc().gui.getChat();
            if (chatComponent instanceof WrappingChatComponent wrappingChatComponent) {
                chatComponent = wrappingChatComponent.getOriginalChatComponent();
            }
            processChatComponentReplacements(chatComponent, replacements);

            Services.ChatTab.forEachChatComponent(c -> processChatComponentReplacements(c, replacements));
        }
    }

    private static final class SendPageContentTask implements Runnable {
        private final List<StyledText> pageContent;
        private final boolean lastPage;

        private SendPageContentTask(List<StyledText> pageContent, boolean lastPage) {
            this.pageContent = pageContent;
            this.lastPage = lastPage;
        }

        @Override
        public void run() {
            Handlers.Chat.handlePage(pageContent, lastPage);
        }
    }
}
