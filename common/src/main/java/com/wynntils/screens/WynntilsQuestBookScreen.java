/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.widgets.BackButton;
import com.wynntils.screens.widgets.PageSelectorButton;
import com.wynntils.screens.widgets.QuestBookSearchWidget;
import com.wynntils.screens.widgets.QuestButton;
import com.wynntils.screens.widgets.ReloadQuestsButton;
import com.wynntils.screens.widgets.TextInputBoxWidget;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.Pair;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.model.CharacterManager;
import com.wynntils.wynn.model.questbook.QuestBookManager;
import com.wynntils.wynn.model.questbook.QuestInfo;
import com.wynntils.wynn.model.questbook.QuestStatus;
import com.wynntils.wynn.objects.ProfessionInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class WynntilsQuestBookScreen extends Screen implements SearchableScreen {
    private static final int QUESTS_PER_PAGE = 13;
    private static final List<Component> RELOAD_TOOLTIP = List.of(
            new TranslatableComponent("screens.wynntils.wynntilsQuestBook.reload.name").withStyle(ChatFormatting.WHITE),
            new TranslatableComponent("screens.wynntils.wynntilsQuestBook.reload.description")
                    .withStyle(ChatFormatting.GRAY));

    private Widget hovered = null;
    private final QuestBookSearchWidget searchWidget;
    private int currentPage = 0;
    private int maxPage = 0;
    private List<QuestInfo> quests;
    private List<QuestButton> questButtons = new ArrayList<>();

    public WynntilsQuestBookScreen() {
        super(new TranslatableComponent("screens.wynntils.wynntilsQuestBook.name"));

        this.searchWidget = new QuestBookSearchWidget(
                (int) (Texture.QUEST_BOOK_BACKGROUND.width() / 2f + 15),
                0,
                Texture.QUEST_BOOK_SEARCH.width(),
                Texture.QUEST_BOOK_SEARCH.height(),
                this::updateQuestsFilter,
                this);
    }

    @Override
    protected void init() {
        QuestBookManager.rescanQuestBook();
        this.setQuests(QuestBookManager.getQuests());

        this.addRenderableWidget(searchWidget);

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW.width() / 2,
                Texture.BACK_ARROW.height(),
                new WynntilsMenuScreen()));

        this.addRenderableWidget(new ReloadQuestsButton(
                Texture.QUEST_BOOK_BACKGROUND.width() - 21,
                11,
                (int) (Texture.RELOAD_BUTTON.width() / 2 / 1.7f),
                (int) (Texture.RELOAD_BUTTON.height() / 1.7f),
                this));
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
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = getTranslationX();
        final float translationY = getTranslationY();
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack);

        renderVersion(poseStack);

        renderButtons(poseStack, mouseX, mouseY, partialTick);

        renderDescription(poseStack);

        renderPageInfo(poseStack);

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
        if (delta < 0) {
            setCurrentPage(getCurrentPage() + 1);
        } else if (delta > 0) {
            setCurrentPage(getCurrentPage() - 1);
        }

        return true;
    }

    private void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        if (this.hovered instanceof ReloadQuestsButton) {
            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY,
                    100,
                    RELOAD_TOOLTIP,
                    FontRenderer.getInstance().getFont(),
                    true);
            return;
        }

        if (this.hovered instanceof QuestButton questButton) {
            QuestInfo questInfo = questButton.getQuestInfo();

            List<Component> tooltipLines = getTooltipLinesForQuest(questInfo);

            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY,
                    100,
                    tooltipLines,
                    FontRenderer.getInstance().getFont(),
                    true);
        }
    }

    private void renderPageInfo(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        (currentPage + 1) + " / " + (maxPage + 1),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f,
                        Texture.QUEST_BOOK_BACKGROUND.width(),
                        Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.Center,
                        FontRenderer.TextShadow.NONE);
    }

    private static void renderDescription(PoseStack poseStack) {
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

    private void renderBackgroundTexture(PoseStack poseStack) {
        int txWidth = Texture.QUEST_BOOK_BACKGROUND.width();
        int txHeight = Texture.QUEST_BOOK_BACKGROUND.height();

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.QUEST_BOOK_BACKGROUND.resource(),
                (this.width - txWidth) / 2f,
                (this.height - txHeight) / 2f,
                0,
                txWidth,
                txHeight,
                txWidth,
                txHeight);
    }

    private void renderTitle(PoseStack poseStack) {
        int txWidth = Texture.QUEST_BOOK_TITLE.width();
        int txHeight = Texture.QUEST_BOOK_TITLE.height();
        RenderUtils.drawScalingTexturedRect(
                poseStack, Texture.QUEST_BOOK_TITLE.resource(), 0, 30, 0, txWidth, txHeight, txWidth, txHeight);

        poseStack.pushPose();
        poseStack.scale(2f, 2f, 0f);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.wynntilsQuestBook.quests"),
                        5,
                        18,
                        CommonColors.YELLOW,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.NORMAL);
        poseStack.popPose();
    }

    private void renderVersion(PoseStack poseStack) {
        // FIXME: Replace with better scaling support

        poseStack.pushPose();
        String version = "Build " + WynntilsMod.getBuildNumber();
        poseStack.scale(0.7f, 0.7f, 0);
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        version,
                        0,
                        Texture.QUEST_BOOK_BACKGROUND.width() * 1.3f / 2f
                                + FontRenderer.getInstance().getFont().width(version),
                        Texture.QUEST_BOOK_BACKGROUND.height() * 1.3f - 6f,
                        0,
                        CommonColors.YELLOW,
                        HorizontalAlignment.Center,
                        FontRenderer.TextShadow.NORMAL);
        poseStack.popPose();
    }

    private static List<Component> getTooltipLinesForQuest(QuestInfo questInfo) {
        List<Component> tooltipLines = new ArrayList<>() {
            {
                add(new TextComponent(questInfo.getName())
                        .withStyle(ChatFormatting.BOLD)
                        .withStyle(ChatFormatting.WHITE));
                add(questInfo.getStatus().getComponent());
                add(new TextComponent(""));
                add((CharacterManager.getCharacterInfo().getLevel() >= questInfo.getLevel()
                                ? new TextComponent("✔").withStyle(ChatFormatting.GREEN)
                                : new TextComponent("✖").withStyle(ChatFormatting.RED))
                        .append(new TextComponent(" Combat Lv. Min: ").withStyle(ChatFormatting.GRAY))
                        .append(new TextComponent(String.valueOf(questInfo.getLevel()))
                                .withStyle(ChatFormatting.WHITE)));
            }
        };

        for (Pair<String, Integer> additionalRequirement : questInfo.getAdditionalRequirements()) {
            MutableComponent base = CharacterManager.getCharacterInfo()
                                    .getProfessionInfo()
                                    .getLevel(ProfessionInfo.ProfessionType.valueOf(additionalRequirement.a))
                            >= additionalRequirement.b
                    ? new TextComponent("✔ ").withStyle(ChatFormatting.GREEN)
                    : new TextComponent("✖ ").withStyle(ChatFormatting.RED);

            tooltipLines.add(base.append(new TextComponent(additionalRequirement.a + " Lv. Min: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(new TextComponent(String.valueOf(additionalRequirement.b))
                            .withStyle(ChatFormatting.WHITE))));
        }

        tooltipLines.add(new TextComponent("-")
                .withStyle(ChatFormatting.GREEN)
                .append(new TextComponent(" Length: ").withStyle(ChatFormatting.GRAY))
                .append(new TextComponent(StringUtils.capitalizeFirst(
                                questInfo.getLength().toString().toLowerCase(Locale.ROOT)))
                        .withStyle(ChatFormatting.WHITE)));
        tooltipLines.add(new TextComponent(""));

        if (questInfo.getStatus() != QuestStatus.COMPLETED) {
            tooltipLines.add(new TextComponent(questInfo.getNextTask()).withStyle(ChatFormatting.GRAY));
            tooltipLines.add(new TextComponent(""));
        }

        if (questInfo.getStatus() != QuestStatus.CANNOT_START) {
            tooltipLines.add(new TextComponent("Left click to pin it!")
                    .withStyle(ChatFormatting.GREEN)
                    .withStyle(ChatFormatting.BOLD));
        }
        tooltipLines.add(new TextComponent("WIP: Middle click to view on map!")
                .withStyle(ChatFormatting.YELLOW)
                .withStyle(ChatFormatting.BOLD));
        tooltipLines.add(new TextComponent("Right to open on the wiki!")
                .withStyle(ChatFormatting.GOLD)
                .withStyle(ChatFormatting.BOLD));
        return tooltipLines;
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

    public void reloadQuests() {
        updateQuestsFilter(this.searchWidget.getTextBoxInput());
    }

    private void updateQuestsFilter(String searchText) {
        this.setQuests(QuestBookManager.getQuests().stream()
                .filter(questInfo -> StringUtils.partialMatch(questInfo.getName(), searchText))
                .toList());
    }

    private float getTranslationY() {
        return (this.height - Texture.QUEST_BOOK_BACKGROUND.height()) / 2f;
    }

    private float getTranslationX() {
        return (this.width - Texture.QUEST_BOOK_BACKGROUND.width()) / 2f;
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

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = MathUtils.clamp(currentPage, 0, maxPage);
        reloadQuestButtons();
    }

    public int getMaxPage() {
        return maxPage;
    }
}
