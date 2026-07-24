/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.emotewheel;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.ui.EmoteWheelFeature;
import com.wynntils.screens.base.widgets.HoverableTexturedButton;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.emotewheel.widgets.EmoteConfigButton;
import com.wynntils.screens.emotewheel.widgets.EmoteSearchWidget;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class EmoteWheelConfigScreen extends EmoteWheelScreen {
    private static final int MAX_EMOTES_PER_PAGE = 8;
    private static final int EMOTE_BUTTON_SIZE = 21;

    private final Screen previousScreen;

    // Collections
    private List<String> emoteList = new ArrayList<>();
    private List<EmoteConfigButton> emoteButtons = new ArrayList<>();
    private List<HoverableTexturedButton> optionButtons = new ArrayList<>();

    // Renderables
    private final EmoteSearchWidget searchWidget;
    private TextInputBoxWidget focusedTextInput;

    // UI size, positions, etc
    private boolean draggingScrollWheel = false;
    private float scrollWheelY;
    private int scrollOffset = 0;
    private int offsetX;
    private int offsetY;
    private int scissorTopY;
    private int scissorBottomY;

    private EmoteWheelConfigScreen(Screen previousScreen, EmoteWheelFeature emoteWheelFeature) {
        super(false, emoteWheelFeature);
        this.previousScreen = previousScreen;

        searchWidget = new EmoteSearchWidget(
                11,
                13,
                119,
                20,
                (s) -> {
                    scrollOffset = 0;
                    populateEmotesList();
                },
                this);

        setFocusedTextInput(searchWidget);
    }

    public static Screen create(Screen returnScreen, EmoteWheelFeature emoteWheelFeature) {
        return new EmoteWheelConfigScreen(returnScreen, emoteWheelFeature);
    }

    @Override
    public void doInit() {
        super.doInit();
        offsetX = (int) (((double) this.width / 2) - (Texture.EMOTE_CONFIG_GUI.width() * 1.5));
        offsetY = (int) ((this.height - Texture.EMOTE_CONFIG_GUI.height()) / 2f) - 10;
        searchWidget.setX(11 + offsetX);
        searchWidget.setY(13 + offsetY);
        addOptionButtons();

        this.addRenderableWidget(searchWidget);
        populateEmotesList();
    }

    @Override
    protected void getCenterOfWheel() {
        centerX = (this.width / 2) + ((DIST_FROM_CENTER / 2) + BUTTON_SIZE);
        centerY = height / 2;
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtils.drawTexturedRect(guiGraphics, Texture.EMOTE_CONFIG_GUI, offsetX, offsetY);

        searchWidget.render(guiGraphics, mouseX, mouseY, partialTick);

        checkForRecentRefresh();
        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        // Add one because of the extra space at the top and bottom
        if (emoteList.size() + 1 > MAX_EMOTES_PER_PAGE) {
            renderScrollWheel(guiGraphics);
        }

        if (draggingScrollWheel) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else if (MathUtils.isInside(
                mouseX, mouseY, offsetX + 136, offsetX + 136 + Texture.SCROLL_BUTTON.width(), (int) scrollWheelY, (int)
                        (scrollWheelY + Texture.SCROLL_BUTTON.height()))) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }

        renderTooltips(guiGraphics, mouseX, mouseY);
    }

    private void checkForRecentRefresh() {
        if (Models.Emote.isRefreshedRecently()) {
            scrollOffset = 0;
            populateEmotesList();
            Models.Emote.setRefreshedRecently(false);
        }
    }

    private void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (HoverableTexturedButton optionsButton : optionButtons) {
            optionsButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        scissorTopY = 37 + offsetY;
        int scissorHeight = MAX_EMOTES_PER_PAGE * EMOTE_BUTTON_SIZE + 2;
        scissorBottomY = scissorTopY + scissorHeight;
        RenderUtils.enableScissor(guiGraphics, 9 + offsetX, scissorTopY, 122, scissorHeight);

        for (AbstractWidget widget : emoteButtons) {
            widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        RenderUtils.disableScissor(guiGraphics);
    }

    private void renderScrollWheel(GuiGraphics guiGraphics) {
        scrollWheelY = 32
                + offsetY
                + MathUtils.map(
                        scrollOffset, 0, getMaxScrollOffset(), 0, 177 - Texture.CONFIG_BOOK_SCROLL_BUTTON.height());

        RenderUtils.drawTexturedRect(guiGraphics, Texture.SCROLL_BUTTON, 136 + offsetX, scrollWheelY);
    }

    private int getMaxScrollOffset() {
        return ((emoteList.size() - MAX_EMOTES_PER_PAGE) * EMOTE_BUTTON_SIZE) + 5;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            McUtils.setScreen(previousScreen);
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!draggingScrollWheel && emoteList.size() > MAX_EMOTES_PER_PAGE) {
            if (MathUtils.isInside(
                    (int) event.x(),
                    (int) event.y(),
                    offsetX + 136,
                    offsetX + 136 + Texture.SCROLL_BUTTON.width(),
                    (int) scrollWheelY,
                    (int) (scrollWheelY + Texture.SCROLL_BUTTON.height()))) {
                draggingScrollWheel = true;

                return true;
            }
        }

        for (GuiEventListener listener : getWidgetsForIteration().toList()) {
            if (listener.isMouseOver(event.x(), event.y())) {
                // Buttons have a slight bit rendered underneath the background,
                // we don't want that part to be clickable
                if (listener instanceof HoverableTexturedButton) {
                    if (MathUtils.isInside(
                            (int) event.x(),
                            (int) event.y(),
                            offsetX,
                            offsetX + Texture.EMOTE_CONFIG_GUI.width(),
                            offsetY,
                            offsetY + Texture.EMOTE_CONFIG_GUI.height())) {
                        return false;
                    }
                }

                listener.mouseClicked(event, isDoubleClick);
            }
        }

        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        if (draggingScrollWheel) {
            int scrollAreaStartY = 23 + 10 + offsetY;
            int scrollAreaHeight = MAX_EMOTES_PER_PAGE * EMOTE_BUTTON_SIZE - Texture.SCROLL_BUTTON.height();

            int newOffset = Math.round(MathUtils.map(
                    (float) event.y(), scrollAreaStartY, scrollAreaStartY + scrollAreaHeight, 0, getMaxScrollOffset()));

            newOffset = Math.max(0, Math.min(newOffset, getMaxScrollOffset()));

            scrollEmotesList(newOffset);

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for (GuiEventListener listener : getWidgetsForIteration().toList()) {
            if (listener.isMouseOver(event.x(), event.y())) {
                listener.mouseReleased(event);
            }
        }

        draggingScrollWheel = false;

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int scrollAmount = (int) (-scrollY * 10F);
        int newOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, getMaxScrollOffset()));
        scrollEmotesList(newOffset);

        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return focusedTextInput != null && focusedTextInput.charTyped(event);
    }

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return focusedTextInput;
    }

    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {
        this.focusedTextInput = focusedTextInput;
    }

    private Stream<GuiEventListener> getWidgetsForIteration() {
        return Stream.concat(children.stream(), Stream.concat(optionButtons.stream(), emoteButtons.stream()));
    }

    private void populateEmotesList() {
        for (AbstractWidget widget : emoteButtons) {
            this.removeWidget(widget);
        }

        emoteButtons = new ArrayList<>();

        // Get all emoteButtons (available and selected), sorted alphabetically.
        // Filter to only include emoteButtons matching search query.
        createEmoteList();

        int yPos = 42 + offsetY;
        int xPos = 13 + offsetX;
        for (String value : emoteList) {
            emoteButtons.add(new EmoteConfigButton(xPos, yPos, 114, 18, value, this));

            yPos += EMOTE_BUTTON_SIZE;
        }

        scrollEmotesList(scrollOffset);
    }

    private void createEmoteList() {
        List<String> emotes = Models.Emote.getAvailableEmotes();

        if (emotes != null) {
            Set<String> selectedEmotes = Models.Emote.getFavoritedEmotes().stream()
                    .filter(Objects::nonNull)
                    .filter(s -> !emotes.contains(s))
                    .collect(Collectors.toSet());

            emoteList = new ArrayList<>(emotes);
            emoteList.addAll(selectedEmotes);
            emoteList = emoteList.stream()
                    .sorted(String::compareTo)
                    .filter(this::searchMatches)
                    .toList();
        } else {
            emoteList = Models.Emote.getFavoritedEmotes().stream()
                    .filter(Objects::nonNull)
                    .sorted(String::compareTo)
                    .filter(this::searchMatches)
                    .toList();
        }
    }

    private boolean searchMatches(String translatable) {
        return StringUtils.partialMatch(translatable, searchWidget.getTextBoxInput());
    }

    private void scrollEmotesList(int newOffset) {
        scrollOffset = newOffset;

        for (EmoteConfigButton overlay : emoteButtons) {
            int newY = 42 + offsetY + (emoteButtons.indexOf(overlay) * EMOTE_BUTTON_SIZE) - scrollOffset;

            overlay.setY(newY);
            overlay.visible = newY >= (42 + offsetY - EMOTE_BUTTON_SIZE)
                    && newY <= (42 + offsetY + (MAX_EMOTES_PER_PAGE) * EMOTE_BUTTON_SIZE);
        }
    }

    private void addOptionButtons() {
        optionButtons = new ArrayList<>();

        int numButtons = 2;
        int margin = (Texture.EMOTE_CONFIG_GUI.width() - (Texture.EMOTE_CONFIG_BUTTON.width() * numButtons))
                / (numButtons * 2);

        HoverableTexturedButton refreshButton = new HoverableTexturedButton(
                (int) (offsetX + (margin * 1.5)),
                Texture.EMOTE_CONFIG_GUI.height() - 4 + offsetY,
                Texture.EMOTE_CONFIG_BUTTON.width(),
                Texture.EMOTE_CONFIG_BUTTON.height() / 2,
                StyledText.fromComponent(Component.translatable("screens.wynntils.emoteWheelConfig.refresh")),
                (button) -> Models.Emote.refreshAvailableEmotes(),
                List.of(
                        Component.translatable("screens.wynntils.emoteWheelConfig.refreshTooltip"),
                        Component.translatable("screens.wynntils.emoteWheelConfig.refreshTooltip2")),
                Texture.EMOTE_CONFIG_BUTTON,
                Texture.EMOTE_CONFIG_GUI,
                false,
                offsetX,
                offsetY);

        optionButtons.add(refreshButton);

        HoverableTexturedButton closeButton = new HoverableTexturedButton(
                (int) ((Texture.EMOTE_CONFIG_GUI.width() / 2f) + offsetX + ((float) margin / 2)),
                Texture.EMOTE_CONFIG_GUI.height() - 4 + offsetY,
                Texture.EMOTE_CONFIG_BUTTON.width(),
                Texture.EMOTE_CONFIG_BUTTON.height() / 2,
                StyledText.fromComponent(Component.translatable("screens.wynntils.emoteWheelConfig.close")),
                (button) -> {
                    onClose();
                    McUtils.setScreen(previousScreen);
                },
                List.of(Component.translatable("screens.wynntils.emoteWheelConfig.closeTooltip")),
                Texture.EMOTE_CONFIG_BUTTON,
                Texture.EMOTE_CONFIG_GUI,
                false,
                offsetX,
                offsetY);

        optionButtons.add(closeButton);
    }

    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // The option buttons have a slight bit rendered underneath the background,
        // we don't want to render the tooltip when hovering that bit.
        if (MathUtils.isInside(
                mouseX,
                mouseY,
                offsetX,
                Texture.EMOTE_CONFIG_GUI.width(),
                offsetY,
                Texture.EMOTE_CONFIG_GUI.height())) {
            return;
        }

        for (GuiEventListener button : optionButtons) {
            if (button instanceof HoverableTexturedButton hoverable && hoverable.isHovered()) {
                guiGraphics.setTooltipForNextFrame(
                        Lists.transform(hoverable.getTooltipLines(), Component::getVisualOrderText), mouseX, mouseY);
                break;
            }
        }
    }

    public int getScissorTopY() {
        return scissorTopY;
    }

    public int getScissorBottomY() {
        return scissorBottomY;
    }
}
