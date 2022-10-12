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
import com.wynntils.gui.widgets.LootrunButton;
import com.wynntils.gui.widgets.PageSelectorButton;
import com.wynntils.gui.widgets.QuestBookSearchWidget;
import com.wynntils.gui.widgets.TextInputBoxWidget;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.model.LootrunModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class WynntilsLootrunsScreen extends WynntilsMenuPagedScreenBase implements TextboxScreen {
    private static final int LOOTRUNS_PER_PAGE = 13;

    private int currentPage = 0;
    private int maxPage = 0;
    private final List<LootrunModel.LootrunInstance> lootruns = new ArrayList<>();

    private final List<LootrunButton> lootrunButtons = new ArrayList<>();
    private final QuestBookSearchWidget searchWidget;
    private Widget hovered = null;

    public WynntilsLootrunsScreen() {
        super(new TranslatableComponent("screens.wynntils.lootruns.name"));

        // Do not lose search info on re-init
        this.searchWidget = new QuestBookSearchWidget(
                (int) (Texture.QUEST_BOOK_BACKGROUND.width() / 2f + 15),
                0,
                Texture.QUEST_BOOK_SEARCH.width(),
                Texture.QUEST_BOOK_SEARCH.height(),
                this::reloadLootruns,
                this);
    }

    @Override
    public void onClose() {
        McUtils.mc().keyboardHandler.setSendRepeatsToGui(false);
        WynntilsMod.unregisterEventListener(this);
        super.onClose();
    }

    @Override
    protected void init() {
        McUtils.mc().keyboardHandler.setSendRepeatsToGui(true);

        reloadLootruns(searchWidget.getTextBoxInput());

        this.addRenderableWidget(searchWidget);

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW.width() / 2,
                Texture.BACK_ARROW.height(),
                new WynntilsMenuScreen()));

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

        renderTitle(poseStack, I18n.get("screens.wynntils.lootruns.name"));

        renderVersion(poseStack);

        renderButtons(poseStack, mouseX, mouseY, partialTick);

        if (lootruns.isEmpty()) {
            renderNoLootrunsHelper(poseStack);
        }

        renderDescription(poseStack);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        poseStack.popPose();

        renderTooltip(poseStack, mouseX, mouseY);
    }

    private void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        if (hovered instanceof LootrunButton lootrunButton) {
            List<Component> tooltipLines;

            LootrunModel.LootrunInstance currentLootrun = LootrunModel.getCurrentLootrun();
            if (currentLootrun != null
                    && Objects.equals(lootrunButton.getLootrun().name(), currentLootrun.name())) {
                tooltipLines = List.of(
                        new TextComponent(lootrunButton.getLootrun().name()).withStyle(ChatFormatting.BOLD),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.loaded")
                                .withStyle(ChatFormatting.YELLOW),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.viewInFolder")
                                .withStyle(ChatFormatting.GOLD),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.openOnMap")
                                .withStyle(ChatFormatting.BLUE),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.unload")
                                .withStyle(ChatFormatting.GREEN));
            } else {
                tooltipLines = List.of(
                        new TextComponent(lootrunButton.getLootrun().name()).withStyle(ChatFormatting.BOLD),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.load")
                                .withStyle(ChatFormatting.GREEN),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.viewInFolder")
                                .withStyle(ChatFormatting.GOLD),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.openOnMap")
                                .withStyle(ChatFormatting.BLUE),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.remove")
                                .withStyle(ChatFormatting.RED));
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

    private void renderNoLootrunsHelper(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        I18n.get("screens.wynntils.lootruns.noLootruns"),
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

    private void renderDescription(PoseStack poseStack) {
        LootrunModel.LootrunInstance currentLootrun = LootrunModel.getCurrentLootrun();
        if (currentLootrun != null) {
            poseStack.pushPose();
            poseStack.translate(20, 80, 0);

            poseStack.pushPose();
            poseStack.scale(1.4f, 1.4f, 0f);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            currentLootrun.name(),
                            0,
                            0,
                            CommonColors.BLACK,
                            HorizontalAlignment.Left,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.NONE);
            poseStack.popPose();

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            I18n.get("screens.wynntils.lootruns.chests") + ": "
                                    + currentLootrun.chests().size(),
                            0,
                            19,
                            CommonColors.BLACK,
                            HorizontalAlignment.Left,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.NONE);

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            I18n.get("screens.wynntils.lootruns.notes") + ": "
                                    + currentLootrun.notes().size(),
                            0,
                            29,
                            CommonColors.BLACK,
                            HorizontalAlignment.Left,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.NONE);

            Vec3 start = currentLootrun.path().points().get(0);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            I18n.get("screens.wynntils.lootruns.start") + ": "
                                    + String.format("[%d, %d, %d]", (int) start.x, (int) start.y, (int) start.z),
                            0,
                            39,
                            CommonColors.BLACK,
                            HorizontalAlignment.Left,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.NONE);

            Vec3 end = currentLootrun
                    .path()
                    .points()
                    .get(currentLootrun.path().points().size() - 1);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            I18n.get("screens.wynntils.lootruns.end") + ": "
                                    + String.format("[%d, %d, %d]", (int) end.x, (int) end.y, (int) end.z),
                            0,
                            49,
                            CommonColors.BLACK,
                            HorizontalAlignment.Left,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.NONE);

            poseStack.popPose();
        } else {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            I18n.get("screens.wynntils.lootruns.description1"),
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
                            I18n.get("screens.wynntils.lootruns.description2"),
                            20,
                            Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 10,
                            155,
                            Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30,
                            CommonColors.BLACK,
                            HorizontalAlignment.Left,
                            FontRenderer.TextShadow.NONE);
        }
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

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void setCurrentPage(int currentPage) {
        this.currentPage = MathUtils.clamp(currentPage, 0, maxPage);
        reloadLootruns(searchWidget.getTextBoxInput());
    }

    @Override
    public int getMaxPage() {
        return maxPage;
    }

    private void reloadLootruns(String searchTerm) {
        lootruns.clear();
        lootruns.addAll(LootrunModel.getLootruns().stream()
                .filter(lootrunInstance -> StringUtils.partialMatch(lootrunInstance.name(), searchTerm))
                .toList());

        this.maxPage = (lootruns.size() / LOOTRUNS_PER_PAGE + (lootruns.size() % LOOTRUNS_PER_PAGE != 0 ? 1 : 0)) - 1;

        for (LootrunButton lootrunButton : lootrunButtons) {
            this.removeWidget(lootrunButton);
        }

        lootrunButtons.clear();

        final int start = Math.max(0, currentPage * LOOTRUNS_PER_PAGE);
        for (int i = start; i < Math.min(lootruns.size(), start + LOOTRUNS_PER_PAGE); i++) {
            int offset = i % LOOTRUNS_PER_PAGE;
            LootrunButton lootrunButton = new LootrunButton(
                    Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 15,
                    offset * 13 + 25,
                    Texture.QUEST_BOOK_BACKGROUND.width() / 2 - 37,
                    9,
                    lootruns.get(i),
                    this);
            lootrunButtons.add(lootrunButton);
            this.addRenderableWidget(lootrunButton);
        }
    }

    public void reloadLootruns() {
        reloadLootruns(searchWidget.getTextBoxInput());
    }

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return this.searchWidget;
    }

    // Dummy impl
    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {}
}
