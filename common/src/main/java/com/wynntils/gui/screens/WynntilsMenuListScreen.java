/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.widgets.QuestBookSearchWidget;
import com.wynntils.gui.widgets.TextInputBoxWidget;
import com.wynntils.gui.widgets.WynntilsButton;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.MathUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class WynntilsMenuListScreen<E, B extends WynntilsButton> extends WynntilsMenuPagedScreenBase
        implements TextboxScreen {
    protected int currentPage = 0;
    protected int maxPage = 0;
    protected List<E> elements = new ArrayList<>();

    protected final List<B> elementButtons = new ArrayList<>();
    protected final QuestBookSearchWidget searchWidget;
    protected Renderable hovered = null;

    @Override
    protected void doInit() {
        reloadElements(searchWidget.getTextBoxInput());

        this.addRenderableWidget(searchWidget);
    }

    public WynntilsMenuListScreen(Component component) {
        super(component);

        // Do not lose search info on re-init
        this.searchWidget = new QuestBookSearchWidget(
                (int) (Texture.QUEST_BOOK_BACKGROUND.width() / 2f + 15),
                0,
                Texture.QUEST_BOOK_SEARCH.width(),
                Texture.QUEST_BOOK_SEARCH.height(),
                s -> reloadElements(),
                this);
    }

    protected void renderButtons(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.hovered = null;

        final float translationX = getTranslationX();
        final float translationY = getTranslationY();

        for (Renderable renderable : new ArrayList<>(this.renderables)) {
            renderable.render(poseStack, (int) (mouseX - translationX), (int) (mouseY - translationY), partialTick);

            if (renderable instanceof WynntilsButton button) {
                if (button.isMouseOver(mouseX - translationX, mouseY - translationY)) {
                    this.hovered = button;
                }
            }
        }
    }

    protected void renderNoElementsHelper(PoseStack poseStack, String key) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        key,
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
        reloadElements(searchWidget.getTextBoxInput());
    }

    @Override
    public int getMaxPage() {
        return maxPage;
    }

    protected void reloadElements(String searchTerm) {
        elements.clear();
        reloadElementsList(searchTerm);

        this.maxPage = Math.max(
                0,
                (elements.size() / getElementsPerPage() + (elements.size() % getElementsPerPage() != 0 ? 1 : 0)) - 1);

        for (B button : elementButtons) {
            this.removeWidget(button);
        }

        elementButtons.clear();

        final int start = Math.max(0, currentPage * getElementsPerPage());
        for (int i = start; i < Math.min(elements.size(), start + getElementsPerPage()); i++) {
            B button = getButtonFromElement(i);
            elementButtons.add(button);
            this.addRenderableWidget(button);
        }
    }

    protected abstract B getButtonFromElement(int i);

    protected abstract void reloadElementsList(String searchTerm);

    public void reloadElements() {
        reloadElements(searchWidget.getTextBoxInput());
        setCurrentPage(0);
    }

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return this.searchWidget;
    }

    // Dummy impl
    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {}

    protected int getElementsPerPage() {
        return 13;
    }
}
