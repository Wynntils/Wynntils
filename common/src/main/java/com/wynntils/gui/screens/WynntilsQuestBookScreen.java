/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.widgets.BackButton;
import com.wynntils.gui.widgets.DialogueHistoryButton;
import com.wynntils.gui.widgets.PageSelectorButton;
import com.wynntils.gui.widgets.QuestBookSearchWidget;
import com.wynntils.gui.widgets.QuestButton;
import com.wynntils.gui.widgets.QuestInfoButton;
import com.wynntils.gui.widgets.ReloadButton;
import com.wynntils.gui.widgets.TextInputBoxWidget;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.event.QuestBookReloadedEvent;
import com.wynntils.wynn.model.questbook.QuestBookManager;
import com.wynntils.wynn.model.questbook.QuestInfo;
import com.wynntils.wynn.model.questbook.QuestStatus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class WynntilsQuestBookScreen extends WynntilsMenuPagedScreenBase implements SearchableScreen {
    private static final int QUESTS_PER_PAGE = 13;
    private static final List<Component> RELOAD_TOOLTIP = List.of(
            new TranslatableComponent("screens.wynntils.wynntilsQuestBook.reload.name").withStyle(ChatFormatting.WHITE),
            new TranslatableComponent("screens.wynntils.wynntilsQuestBook.reload.description")
                    .withStyle(ChatFormatting.GRAY));

    private Widget hovered = null;
    private final QuestBookSearchWidget searchWidget;
    private int currentPage = 0;
    private int maxPage = 0;
    private List<QuestInfo> quests = new ArrayList<>();
    private List<QuestButton> questButtons = new ArrayList<>();
    private QuestInfo tracked = null;
    private boolean miniQuestMode = false;

    public WynntilsQuestBookScreen() {
        super(new TranslatableComponent("screens.wynntils.wynntilsQuestBook.name"));

        // Do not lose search info on re-init
        this.searchWidget = new QuestBookSearchWidget(
                (int) (Texture.QUEST_BOOK_BACKGROUND.width() / 2f + 15),
                0,
                Texture.QUEST_BOOK_SEARCH.width(),
                Texture.QUEST_BOOK_SEARCH.height(),
                this::updateQuestsFilter,
                this);

        // Only register this once
        WynntilsMod.registerEventListener(this);
    }

    @Override
    public void onClose() {
        McUtils.mc().keyboardHandler.setSendRepeatsToGui(false);
        WynntilsMod.unregisterEventListener(this);
        super.onClose();
    }

    /** This is called on every resize. Re-registering widgets are required, re-creating them is not.
     * */
    @Override
    protected void init() {
        McUtils.mc().keyboardHandler.setSendRepeatsToGui(true);

        QuestBookManager.rescanQuestBook();

        this.addRenderableWidget(searchWidget);

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW.width() / 2,
                Texture.BACK_ARROW.height(),
                new WynntilsMenuScreen()));

        this.addRenderableWidget(new ReloadButton(
                Texture.QUEST_BOOK_BACKGROUND.width() - 21,
                11,
                (int) (Texture.RELOAD_BUTTON.width() / 2 / 1.7f),
                (int) (Texture.RELOAD_BUTTON.height() / 1.7f),
                () -> {
                    if (miniQuestMode) {
                        QuestBookManager.queryMiniQuests();
                    } else {
                        QuestBookManager.rescanQuestBook();
                    }
                }));
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
                Texture.QUESTS_BUTTON.height(),
                this));

        reloadQuestButtons();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = getTranslationX();
        final float translationY = getTranslationY();
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsQuestBook.quests"));

        renderVersion(poseStack);

        renderButtons(poseStack, mouseX, mouseY, partialTick);

        if (quests.isEmpty()) {
            renderNoQuestsHelper(poseStack);
        }

        renderDescription(poseStack);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        poseStack.popPose();

        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        final float translationX = getTranslationX();
        final float translationY = getTranslationY();

        for (GuiEventListener child : new ArrayList<>(this.children())) {
            if (child.isMouseOver(mouseX - translationX, mouseY - translationY)) {
                child.mouseClicked(mouseX - translationX, mouseY - translationY, button);
            }
        }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            McUtils.mc().setScreen(null);
            return true;
        }

        if (searchWidget != null) {
            return searchWidget.keyPressed(keyCode, scanCode, modifiers);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchWidget != null) {
            return searchWidget.charTyped(codePoint, modifiers);
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        setCurrentPage(getCurrentPage() + (delta > 0 ? -1 : 1));

        return true;
    }

    @SubscribeEvent
    public void onQuestsReloaded(QuestBookReloadedEvent.QuestsReloaded event) {
        if (miniQuestMode) return;

        this.setQuests(QuestBookManager.getQuests());

        for (QuestInfo quest : quests) {
            if (!quest.isTracked()) {
                continue;
            }

            tracked = quest;
            return;
        }
    }

    @SubscribeEvent
    public void onQuestsReloaded(QuestBookReloadedEvent.MiniQuestsReloaded event) {
        if (!miniQuestMode) return;

        this.setQuests(QuestBookManager.getMiniQuests());

        for (QuestInfo quest : quests) {
            if (!quest.isTracked()) {
                continue;
            }

            tracked = quest;
            return;
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
                        I18n.get("screens.wynntils.wynntilsQuestBook.tryReload"),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f + 15f,
                        Texture.QUEST_BOOK_BACKGROUND.width() - 15f,
                        0,
                        Texture.QUEST_BOOK_BACKGROUND.height(),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30f,
                        CommonColors.BLACK,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Middle,
                        FontRenderer.TextShadow.NONE);
    }

    private void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        List<Component> tooltipLines = List.of();

        if (this.hovered instanceof ReloadButton) {
            tooltipLines = RELOAD_TOOLTIP;
        }

        if (this.hovered instanceof QuestButton questButton) {
            QuestInfo questInfo = questButton.getQuestInfo();

            tooltipLines = QuestInfo.getTooltipLinesForQuest(questInfo);

            tooltipLines.add(new TextComponent(""));

            if (questInfo.getStatus() != QuestStatus.CANNOT_START) {
                if (this.tracked == questInfo) {
                    tooltipLines.add(new TextComponent("Left click to unpin it!")
                            .withStyle(ChatFormatting.RED)
                            .withStyle(ChatFormatting.BOLD));
                } else {
                    tooltipLines.add(new TextComponent("Left click to pin it!")
                            .withStyle(ChatFormatting.GREEN)
                            .withStyle(ChatFormatting.BOLD));
                }
            }

            tooltipLines.add(new TextComponent("WIP: Middle click to view on map!")
                    .withStyle(ChatFormatting.YELLOW)
                    .withStyle(ChatFormatting.BOLD));
            tooltipLines.add(new TextComponent("Right to open on the wiki!")
                    .withStyle(ChatFormatting.GOLD)
                    .withStyle(ChatFormatting.BOLD));
        }

        if (this.hovered instanceof DialogueHistoryButton) {
            tooltipLines = List.of(
                    new TextComponent("[>] ")
                            .withStyle(ChatFormatting.GOLD)
                            .append(new TranslatableComponent("screens.wynntils.wynntilsQuestBook.dialogueHistory.name")
                                    .withStyle(ChatFormatting.BOLD)
                                    .withStyle(ChatFormatting.GOLD)),
                    new TranslatableComponent("screens.wynntils.wynntilsQuestBook.dialogueHistory.description")
                            .withStyle(ChatFormatting.GRAY),
                    new TextComponent(""),
                    new TranslatableComponent("screens.wynntils.wynntilsMenu.leftClickToSelect")
                            .withStyle(ChatFormatting.GREEN));
        }

        if (this.hovered instanceof QuestInfoButton) {
            tooltipLines = new ArrayList<>();

            if (miniQuestMode) {
                tooltipLines.add(new TranslatableComponent("screens.wynntils.wynntilsQuestBook.miniQuestInfo.name"));
            } else {
                tooltipLines.add(new TranslatableComponent("screens.wynntils.wynntilsQuestBook.questInfo.name"));
            }

            for (int i = 1; i <= 100; i += 25) {
                int minLevel = i;
                int maxLevel = i + 24;

                long count = quests.stream()
                        .filter(questInfo -> questInfo.getLevel() >= minLevel && questInfo.getLevel() <= maxLevel)
                        .count();
                long completedCount = quests.stream()
                        .filter(questInfo -> questInfo.getStatus() == QuestStatus.COMPLETED
                                && questInfo.getLevel() >= minLevel
                                && questInfo.getLevel() <= maxLevel)
                        .count();

                tooltipLines.add(new TextComponent("- Lv. " + minLevel + "-" + maxLevel)
                        .append(new TextComponent(" [" + completedCount + "/" + count + "]")
                                .withStyle(ChatFormatting.GRAY))
                        .append(" ")
                        .append(getPercentageComponent((int) completedCount, (int) count, 5)));
            }

            long count = quests.stream()
                    .filter(questInfo -> questInfo.getLevel() >= 101)
                    .count();
            long completedCount = quests.stream()
                    .filter(questInfo -> questInfo.getStatus() == QuestStatus.COMPLETED && questInfo.getLevel() >= 101)
                    .count();
            tooltipLines.add(new TextComponent("- Lv. 101+")
                    .append(new TextComponent(" [" + completedCount + "/" + count + "]").withStyle(ChatFormatting.GRAY))
                    .append(" ")
                    .append(getPercentageComponent((int) completedCount, (int) count, 5)));

            count = quests.size();
            completedCount = quests.stream()
                    .filter(questInfo -> questInfo.getStatus() == QuestStatus.COMPLETED)
                    .count();

            tooltipLines.add(new TextComponent(""));
            tooltipLines.add(new TextComponent("Total Quests: ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(new TextComponent("[" + completedCount + "/" + count + "]")
                            .withStyle(ChatFormatting.DARK_AQUA)));
            tooltipLines.add(getPercentageComponent((int) completedCount, (int) count, 15));
            tooltipLines.add(new TextComponent(""));

            if (!this.miniQuestMode) {
                tooltipLines.add(new TranslatableComponent("screens.wynntils.wynntilsQuestBook.questInfo.click")
                        .withStyle(ChatFormatting.GREEN));
            } else {
                tooltipLines.add(new TranslatableComponent("screens.wynntils.wynntilsQuestBook.miniQuestInfo.click")
                        .withStyle(ChatFormatting.GREEN));
            }
        }

        if (tooltipLines.isEmpty()) return;

        RenderUtils.drawTooltipAt(
                poseStack,
                mouseX,
                mouseY,
                100,
                tooltipLines,
                FontRenderer.getInstance().getFont(),
                true);
    }

    protected void renderDescription(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        I18n.get("screens.wynntils.wynntilsQuestBook.description1"),
                        20,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 10,
                        80,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        FontRenderer.TextShadow.NONE);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        I18n.get("screens.wynntils.wynntilsQuestBook.description2"),
                        20,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 10,
                        170,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        FontRenderer.TextShadow.NONE);
    }

    private void renderButtons(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.hovered = null;

        final float translationX = getTranslationX();
        final float translationY = getTranslationY();

        for (Widget renderable : new ArrayList<>(this.renderables)) {
            renderable.render(poseStack, (int) (mouseX - translationX), (int) (mouseY - translationY), partialTick);

            if (renderable instanceof AbstractButton button) {
                if (button.isMouseOver(mouseX - translationX, mouseY - translationY)) {
                    this.hovered = button;
                }
            }
        }
    }

    private void reloadQuestButtons() {
        for (QuestButton questButton : questButtons) {
            this.removeWidget(questButton);
        }

        questButtons.clear();

        final int start = Math.max(0, currentPage * QUESTS_PER_PAGE);
        for (int i = start; i < Math.min(quests.size(), start + QUESTS_PER_PAGE); i++) {
            int offset = i % QUESTS_PER_PAGE;
            QuestButton questButton = new QuestButton(
                    Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 15,
                    offset * 13 + 25,
                    Texture.QUEST_BOOK_BACKGROUND.width() / 2 - 37,
                    9,
                    quests.get(i),
                    this);
            questButtons.add(questButton);
            this.addRenderableWidget(questButton);
        }
    }

    private void updateQuestsFilter(String searchText) {
        this.setQuests(QuestBookManager.getQuests().stream()
                .filter(questInfo -> StringUtils.partialMatch(questInfo.getName(), searchText))
                .toList());
    }

    private Component getPercentageComponent(int count, int totalCount, int tickCount) {
        int percentage = Math.round((float) count / totalCount * 100);
        ChatFormatting foregroundColor;
        ChatFormatting braceColor;

        if (percentage < 25) {
            braceColor = ChatFormatting.DARK_RED;
            foregroundColor = ChatFormatting.RED;
        } else if (percentage < 75) {
            braceColor = ChatFormatting.GOLD;
            foregroundColor = ChatFormatting.YELLOW;
        } else {
            braceColor = ChatFormatting.DARK_GREEN;
            foregroundColor = ChatFormatting.GREEN;
        }

        StringBuilder insideText = new StringBuilder(foregroundColor.toString());
        insideText.append("|".repeat(tickCount)).append(percentage).append("%").append("|".repeat(tickCount));
        int insertAt =
                Math.min(insideText.length(), Math.round((insideText.length() - 2) * (float) count / totalCount) + 2);
        insideText.insert(insertAt, ChatFormatting.DARK_GRAY);

        return new TextComponent("[")
                .withStyle(braceColor)
                .append(new TextComponent(insideText.toString()))
                .append(new TextComponent("]").withStyle(braceColor));
    }

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return this.searchWidget;
    }

    // Dummy impl
    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {}

    public void setQuests(List<QuestInfo> quests) {
        this.quests = quests;
        this.maxPage = (quests.size() / QUESTS_PER_PAGE + (quests.size() % QUESTS_PER_PAGE != 0 ? 1 : 0)) - 1;
        this.setCurrentPage(0);
    }

    public void setTracked(QuestInfo tracked) {
        this.tracked = tracked;
    }

    public QuestInfo getTracked() {
        return tracked;
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void setCurrentPage(int currentPage) {
        this.currentPage = MathUtils.clamp(currentPage, 0, maxPage);
        reloadQuestButtons();
    }

    @Override
    public int getMaxPage() {
        return maxPage;
    }

    public boolean isMiniQuestMode() {
        return miniQuestMode;
    }

    public void setMiniQuestMode(boolean miniQuestMode) {
        this.miniQuestMode = miniQuestMode;

        this.setQuests(List.of());

        if (this.miniQuestMode) {
            QuestBookManager.queryMiniQuests();
        } else {
            QuestBookManager.rescanQuestBook();
        }
    }
}
