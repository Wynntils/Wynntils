/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.models.activities.event.ActivityTrackerUpdatedEvent;
import com.wynntils.models.activities.event.ActivityUpdatedEvent;
import com.wynntils.models.activities.quests.QuestInfo;
import com.wynntils.models.activities.type.ActivitySortOrder;
import com.wynntils.models.activities.type.ActivityStatus;
import com.wynntils.screens.activities.widgets.DialogueHistoryButton;
import com.wynntils.screens.activities.widgets.QuestButton;
import com.wynntils.screens.activities.widgets.QuestInfoButton;
import com.wynntils.screens.base.WynntilsListScreen;
import com.wynntils.screens.base.widgets.BackButton;
import com.wynntils.screens.base.widgets.FilterButton;
import com.wynntils.screens.base.widgets.PageSelectorButton;
import com.wynntils.screens.base.widgets.ReloadButton;
import com.wynntils.screens.base.widgets.SortOrderWidget;
import com.wynntils.screens.base.widgets.SortableActivityScreen;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class WynntilsQuestBookScreen extends WynntilsListScreen<QuestInfo, QuestButton>
        implements SortableActivityScreen {
    private QuestInfo trackingRequested = null;
    private boolean showQuests = true;
    private boolean showMiniQuests = false;
    private ActivitySortOrder activitySortOrder = ActivitySortOrder.LEVEL;

    private final List<FilterButton> filterButtons = new ArrayList<>();

    private WynntilsQuestBookScreen() {
        super(Component.translatable("screens.wynntils.wynntilsQuestBook.name"));

        // Only register this once
        WynntilsMod.registerEventListener(this);
    }

    public static Screen create() {
        return new WynntilsQuestBookScreen();
    }

    @Override
    public void onClose() {
        WynntilsMod.unregisterEventListener(this);
        super.onClose();
    }

    /** This is called on every resize. Re-registering widgets are required, re-creating them is not.
     * */
    @Override
    protected void doInit() {
        Models.Quest.rescanQuestBook(true, true);

        filterButtons.clear();

        super.doInit();

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW.width() / 2,
                Texture.BACK_ARROW.height(),
                WynntilsMenuScreen.create()));

        filterButtons.add(new FilterButton(
                55,
                142,
                30,
                30,
                Texture.QUESTS_ICON,
                false,
                List.of(Component.literal("[>] ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.translatable("screens.wynntils.wynntilsQuestBook.showQuests.name")
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(ChatFormatting.GREEN))),
                () -> {
                    showQuests = !showQuests;
                    reloadElements();
                },
                () -> showQuests));
        filterButtons.add(new FilterButton(
                90,
                142,
                30,
                30,
                Texture.SIGN,
                false,
                List.of(Component.literal("[>] ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.translatable("screens.wynntils.wynntilsQuestBook.showMiniQuests.name")
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(ChatFormatting.GREEN))),
                () -> {
                    showMiniQuests = !showMiniQuests;
                    reloadElements();
                },
                () -> showMiniQuests));

        for (FilterButton filterButton : filterButtons) {
            this.addRenderableWidget(filterButton);
        }

        this.addRenderableWidget(new ReloadButton(
                Texture.QUEST_BOOK_BACKGROUND.width() - 21,
                11,
                (int) (Texture.RELOAD_BUTTON.width() / 2 / 1.7f),
                (int) (Texture.RELOAD_BUTTON.height() / 1.7f),
                "quest",
                () -> Models.Quest.rescanQuestBook(showQuests, showMiniQuests)));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 50 - Texture.FORWARD_ARROW.width() / 2,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() - 50,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                true,
                this));
        this.addRenderableWidget(new DialogueHistoryButton(
                (int) (Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30),
                15,
                Texture.DIALOGUE_BUTTON.width(),
                Texture.DIALOGUE_BUTTON.height()));
        this.addRenderableWidget(new QuestInfoButton(
                (int) (Texture.QUEST_BOOK_BACKGROUND.width() / 4f),
                12,
                Texture.QUESTS_BUTTON.width(),
                Texture.QUESTS_BUTTON.height()));

        this.addRenderableWidget(new SortOrderWidget(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 1,
                11,
                (int) (Texture.SORT_DISTANCE.width() / 1.7f),
                (int) (Texture.SORT_DISTANCE.height() / 2 / 1.7f),
                this));

        reloadElements();
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = getTranslationX();
        final float translationY = getTranslationY();
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsQuestBook.quests"));

        renderVersion(poseStack);

        renderWidgets(poseStack, mouseX, mouseY, partialTick);

        if (elements.isEmpty()) {
            renderNoQuestsHelper(poseStack);
        }

        renderDescription(poseStack);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        poseStack.popPose();

        renderTooltip(poseStack, mouseX, mouseY);
    }

    @SubscribeEvent
    public void onQuestsReloaded(ActivityUpdatedEvent event) {
        if (!event.getActivityType().isQuest()) return;

        this.setQuests(getSortedQuests());
        setTrackingRequested(null);
        reloadElements();
    }

    @SubscribeEvent
    public void onTrackedActivityUpdate(ActivityTrackerUpdatedEvent event) {
        // Reload so we have the proper order
        if (McUtils.mc().screen == this) {
            this.reloadElements();
        }
    }

    // FIXME: We only need this hack to stop the screen from closing when tracking Quest.
    //        Adding a proper way to add quests with scripted container queries would mean this can get removed.
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onMenuClose(MenuEvent.MenuClosedEvent event) {
        if (McUtils.mc().screen != this) return;

        event.setCanceled(true);
    }

    private static void renderNoQuestsHelper(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.wynntilsQuestBook.tryReload")),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f + 15f,
                        Texture.QUEST_BOOK_BACKGROUND.width() - 15f,
                        0,
                        Texture.QUEST_BOOK_BACKGROUND.height(),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30f,
                        CommonColors.BLACK,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE);
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        List<Component> tooltipLines = List.of();

        if (this.hovered instanceof QuestButton questButton) {
            QuestInfo questInfo = questButton.getQuestInfo();

            tooltipLines = QuestInfo.generateTooltipForQuest(questInfo);

            tooltipLines.add(Component.literal(""));

            if (questInfo.isTrackable()) {
                if (questInfo.equals(Models.Activity.getTrackedQuestInfo())) {
                    tooltipLines.add(Component.literal("Left click to stop tracking it!")
                            .withStyle(ChatFormatting.RED)
                            .withStyle(ChatFormatting.BOLD));
                } else {
                    tooltipLines.add(Component.literal("Left click to track it!")
                            .withStyle(ChatFormatting.GREEN)
                            .withStyle(ChatFormatting.BOLD));
                }
            }

            tooltipLines.add(Component.literal("Middle click to view on map!")
                    .withStyle(ChatFormatting.YELLOW)
                    .withStyle(ChatFormatting.BOLD));
            tooltipLines.add(Component.literal("Right to open on the wiki!")
                    .withStyle(ChatFormatting.GOLD)
                    .withStyle(ChatFormatting.BOLD));
        }

        if (this.hovered instanceof DialogueHistoryButton) {
            tooltipLines = List.of(
                    Component.literal("[>] ")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.translatable("screens.wynntils.wynntilsQuestBook.dialogueHistory.name")
                                    .withStyle(ChatFormatting.BOLD)
                                    .withStyle(ChatFormatting.GOLD)),
                    Component.translatable("screens.wynntils.wynntilsQuestBook.dialogueHistory.description")
                            .withStyle(ChatFormatting.GRAY),
                    Component.literal(""),
                    Component.translatable("screens.wynntils.wynntilsMenu.leftClickToSelect")
                            .withStyle(ChatFormatting.GREEN));
        }

        if (this.hovered instanceof QuestInfoButton) {
            tooltipLines = new ArrayList<>();

            tooltipLines.add(Component.translatable("screens.wynntils.wynntilsQuestBook.questInfo.name"));

            addQuestProgressTooltipLines(tooltipLines, false);

            tooltipLines.add(Component.literal(""));
            tooltipLines.add(Component.translatable("screens.wynntils.wynntilsQuestBook.miniQuestInfo.name"));

            addQuestProgressTooltipLines(tooltipLines, true);
        }

        if (tooltipLines.isEmpty()) {
            super.renderTooltip(poseStack, mouseX, mouseY);
            return;
        }

        RenderUtils.drawTooltipAt(
                poseStack,
                mouseX,
                mouseY,
                100,
                tooltipLines,
                FontRenderer.getInstance().getFont(),
                true);
    }

    private void renderDescription(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.wynntilsQuestBook.description1")),
                        20,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 10,
                        80,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        TextShadow.NONE);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.wynntilsQuestBook.description2")),
                        20,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 10,
                        105,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        TextShadow.NONE);
    }

    @Override
    protected void reloadElementsList(String searchText) {
        List<QuestInfo> newQuests = getSortedQuests();
        elements = newQuests.stream()
                .filter(questInfo -> StringUtils.partialMatch(questInfo.getName(), searchText))
                .collect(Collectors.toList());

        this.maxPage = Math.max(
                0,
                (elements.size() / getElementsPerPage() + (elements.size() % getElementsPerPage() != 0 ? 1 : 0)) - 1);
    }

    private List<QuestInfo> getSortedQuests() {
        return Models.Quest.getSortedQuests(activitySortOrder, showQuests, showMiniQuests);
    }

    private void setQuests(List<QuestInfo> quests) {
        this.elements = new ArrayList<>(quests);
        this.maxPage = Math.max(
                0,
                (elements.size() / getElementsPerPage() + (elements.size() % getElementsPerPage() != 0 ? 1 : 0)) - 1);
    }

    public void setTrackingRequested(QuestInfo questInfo) {
        this.trackingRequested = questInfo;
    }

    public QuestInfo getTrackingRequested() {
        return trackingRequested;
    }

    @Override
    protected QuestButton getButtonFromElement(int i) {
        int offset = i % getElementsPerPage();
        return new QuestButton(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 15,
                offset * 13 + 25,
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 - 37,
                9,
                elements.get(i),
                this);
    }

    @Override
    public ActivitySortOrder getActivitySortOrder() {
        return activitySortOrder;
    }

    @Override
    public void setActivitySortOrder(ActivitySortOrder newSortOrder) {
        if (newSortOrder == null) {
            throw new IllegalStateException("Tried to set null activity sort order");
        }

        this.activitySortOrder = newSortOrder;
        setQuests(getSortedQuests());
        this.setCurrentPage(0);
    }

    private void addQuestProgressTooltipLines(List<Component> tooltipLines, boolean miniQuestMode) {
        List<QuestInfo> elements = this.elements.stream()
                .filter(questInfo -> questInfo.isMiniQuest() == miniQuestMode)
                .toList();

        for (int i = 1; i <= 100; i += 25) {
            int minLevel = i;
            int maxLevel = i + 24;

            long count = elements.stream()
                    .filter(questInfo -> questInfo.getSortLevel() >= minLevel && questInfo.getSortLevel() <= maxLevel)
                    .count();
            long completedCount = elements.stream()
                    .filter(questInfo -> questInfo.getStatus() == ActivityStatus.COMPLETED
                            && questInfo.getSortLevel() >= minLevel
                            && questInfo.getSortLevel() <= maxLevel)
                    .count();

            tooltipLines.add(Component.literal("- Lv. " + minLevel + "-" + maxLevel)
                    .append(Component.literal(" [" + completedCount + "/" + count + "]")
                            .withStyle(ChatFormatting.GRAY))
                    .append(" ")
                    .append(RenderedStringUtils.getPercentageComponent((int) completedCount, (int) count, 5)));
        }

        long count = elements.stream()
                .filter(questInfo -> questInfo.getSortLevel() >= 101)
                .count();
        long completedCount;

        if (count > 0) {
            completedCount = elements.stream()
                    .filter(questInfo ->
                            questInfo.getStatus() == ActivityStatus.COMPLETED && questInfo.getSortLevel() >= 101)
                    .count();
            tooltipLines.add(Component.literal("- Lv. 101+")
                    .append(Component.literal(" [" + completedCount + "/" + count + "]")
                            .withStyle(ChatFormatting.GRAY))
                    .append(" ")
                    .append(RenderedStringUtils.getPercentageComponent((int) completedCount, (int) count, 5)));
        }

        count = elements.size();
        completedCount = elements.stream()
                .filter(questInfo -> questInfo.getStatus() == ActivityStatus.COMPLETED)
                .count();

        tooltipLines.add(Component.literal(""));
        tooltipLines.add(Component.literal(miniQuestMode ? "Total Mini-Quests: " : "Total Quests: ")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal("[" + completedCount + "/" + count + "]")
                        .withStyle(ChatFormatting.DARK_AQUA)));
        tooltipLines.add(RenderedStringUtils.getPercentageComponent((int) completedCount, (int) count, 15));
    }
}
