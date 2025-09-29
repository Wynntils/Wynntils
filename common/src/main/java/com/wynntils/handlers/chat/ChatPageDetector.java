/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.SystemMessageEvent;
import com.wynntils.utils.ListUtils;
import com.wynntils.utils.TaskUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import net.minecraft.network.chat.Component;

public final class ChatPageDetector {
    private static final int MIN_MATCHING_LINES = 5;

    private List<Runnable> tasksAtNextTick = new ArrayList<>();
    private Future<?> pageFinishedTask;
    private Deque<Component> collectedMessages = new LinkedList<>();
    private List<StyledText> pageBackground = null;
    private List<StyledText> pageContent = List.of();
    private List<StyledText> sentBackgroundLines = new ArrayList<>();

    public boolean isInPageMode() {
        return pageBackground != null;
    }

    public List<StyledText> getPageContent() {
        return pageContent;
    }

    public void reset() {
        // Reset in case of world state change
        collectedMessages = new LinkedList<>();
        pageBackground = null;
        pageContent = List.of();
        sentBackgroundLines = new ArrayList<>();
    }

    public void onTick() {
        tasksAtNextTick.forEach(Runnable::run);
        tasksAtNextTick.clear();
    }

    public void handleIncomingChatMessage(SystemMessageEvent.ChatReceivedEvent event) {
        Component message = event.getMessage();
        StyledText styledText = StyledText.fromComponent(message);

        if (pageFinishedTask == null) {
            // Normal single line chat messages will just be passed through
            if (StyledTextUtils.getLineCount(styledText) == 1) return;

            // Sometimes a new page is started in as little as 45 ms after the previous one.
            // But the maximum time between two messages in a screen I have seen is 1 ms.
            // So lets say we wait 5 ms and then we either have a screen or a message we need to pass through.

            pageFinishedTask =
                    TaskUtils.schedule(this::onPotentialPageFinished, 5, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        // Once we started collecting messages, collect them all, so as not to change order
        // in chat, even if they are single line
        collectedMessages.addLast(message);
        event.setCanceled(true);
    }

    private void onPotentialPageFinished() {
        pageFinishedTask = null;
        Deque<Component> pageMessages = collectedMessages;
        collectedMessages = new LinkedList<>();

        // We've held back some multi-line messages. They can either be a page, or not.
        if (!isPage(pageMessages)) {
            // Not a page, so we just send them out the way they were
            for (Component message : pageMessages) {
                enqueueSendDelayedChat(message);
            }
        } else {
            handlePageOrPages(pageMessages);
        }
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

    private void handlePageOrPages(Deque<Component> collectedMessages) {
        if (collectedMessages.size() <= 6) {
            handlePage(collectedMessages);
        } else {
            // We might have gotten two pages in a row without a tick inbetween
            List<Component> messages = new ArrayList<>(collectedMessages);
            int matchCount = ListUtils.countMatchingElements(messages, 0, messages, collectedMessages.size() / 2);
            if (matchCount == collectedMessages.size() / 2) {
                // We have two identical halves, so just keep one half
                Deque<Component> oneHalf = new LinkedList<>(messages.subList(0, collectedMessages.size() / 2));
                handlePage(oneHalf);
            } else {
                if (matchCount == 4) {
                    Deque<Component> firstHalf = new LinkedList<>(messages.subList(0, collectedMessages.size() / 2));
                    Deque<Component> secondHalf =
                            new LinkedList<>(messages.subList(collectedMessages.size() / 2, collectedMessages.size()));
                    handlePage(firstHalf);
                    handlePage(secondHalf);
                } else {
                    // This is weird, but maybe we just have a lot of page content messages?
                    handlePage(collectedMessages);
                }
            }
        }
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
        List<StyledText> background = new ArrayList<>();
        List<StyledText> pageContent = new ArrayList<>();
        int count = 0;
        for (Component message : collectedMessages) {
            StyledText styledText = StyledText.fromComponent(message);

            List<StyledText> lines = List.of(styledText.split("\n", true));
            // Unfortunately, Wynn sends text alternatingly with and without click links and hover,
            // and to be able to compare them properly, we need to strip those out.
            List<StyledText> strippedLines = StyledTextUtils.stripEventsAndLinks(lines);

            if (count < 4) {
                background.addAll(strippedLines);
            } else {
                pageContent.addAll(strippedLines);
            }
            count++;
        }
        return Pair.of(background, pageContent);
    }

    private void handleBackground(List<StyledText> background, int numCollectedMessages) {
        List<StyledText> oldBackground = pageBackground;
        pageBackground = background;

        // If this is the first background page, we can't calculate a diff
        if (oldBackground == null) return;

        List<StyledText> newBackgroundMessages = getMessageDiff(oldBackground, background);
        if (newBackgroundMessages == null) {
            // We failed to calculate a diff.
            if (numCollectedMessages == 4) {
                // This was the last page, and the "background" is actually a redraw
                // of the foreground. The page mode is now finished.
                List<StyledText> sentLines = sentBackgroundLines;

                pageBackground = null;
                pageContent = List.of();
                sentBackgroundLines.clear();

                enqueueSendPageContent(List.of(), true);
                enqueueSendForegroundReplacements(background, oldBackground, sentLines);
                return;
            } else {
                // We could not calculate a diff, and it is not a foreground page. This is bad.
                // To not lose any messages, just resend all.
                WynntilsMod.error("Could not calculate updated background messages");
            }
        }

        for (StyledText message : newBackgroundMessages) {
            // Handle new messages that arrived in the background
            sentBackgroundLines.add(message);
            enqueueSendBackgroundLine(message);
        }
    }

    private List<Pair<StyledText, StyledText>> calculateForegroundReplacements(
            List<StyledText> lastBackground, List<StyledText> foreground, List<StyledText> sentBackgroundLines) {
        List<Pair<StyledText, StyledText>> replacements = new ArrayList<>();
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
            replacements.add(Pair.of(sentBackgroundLines.get(i), foreground.get(lastBackgroundStartPos + i)));
        }
        return replacements;
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
            // FIXME: Implement
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
