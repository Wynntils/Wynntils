/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.paths.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.widgets.ScrollBarWidget;
import com.wynntils.screens.maps.managers.widgets.StyledButton;
import com.wynntils.services.mapdata.paths.PathNote;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class PathNoteListWidget extends ScrollBarWidget {
    private final List<NoteEntryWidget> entries = new ArrayList<>();
    private final Consumer<PathNote> onSelect;
    private final Consumer<PathNote> onRemove;
    private final BooleanSupplier editable;
    private PathNote selected;

    public PathNoteListWidget(
            int x,
            int y,
            int width,
            int height,
            Consumer<PathNote> onSelect,
            Consumer<PathNote> onRemove,
            BooleanSupplier editable) {
        super(x, y, width, height);
        this.onSelect = onSelect;
        this.onRemove = onRemove;
        this.editable = editable;
    }

    public void setNotes(List<PathNote> notes, PathNote selected) {
        this.selected = selected;
        entries.clear();
        for (PathNote note : notes) {
            entries.add(new NoteEntryWidget(note));
        }

        updatePositions();
    }

    @Override
    protected List<NoteEntryWidget> getWidgets() {
        return entries;
    }

    @Override
    protected int getWidgetHeightPadding() {
        return 4;
    }

    private void updatePositions() {
        int top = getY() + SCROLL_BAR_HEIGHT_PADDING;
        int bottom = top + getViewportHeight();
        int y = top - scrollOffsetY;
        for (NoteEntryWidget entry : entries) {
            entry.setX(getX() + SCROLL_BAR_WIDTH_PADDING);
            entry.setY(y);
            entry.setWidth(getViewportWidth());
            entry.visible = y + entry.getHeight() >= top && y <= bottom;
            y += entry.getHeight() + getWidgetHeightPadding();
        }
    }

    @Override
    protected void onScrollOffsetChanged() {
        updatePositions();
    }

    @Override
    protected void renderWidgets(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (entries.isEmpty()) {
            FontRenderer.getInstance()
                    .renderScrollingText(
                            graphics,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.map.paths.noteEditor.emptyList")),
                            getX() + 8,
                            getY() + 16,
                            getViewportWidth() - 8,
                            CommonColors.GRAY,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

        for (NoteEntryWidget entry : entries) {
            entry.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;

        if (super.mouseClicked(event, isDoubleClick)) return true;

        if (!isInsideViewport(event.x(), event.y())) return false;

        for (NoteEntryWidget entry : entries) {
            if (entry.visible && entry.isMouseOver(event.x(), event.y()) && entry.mouseClicked(event, isDoubleClick)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (!isInsideViewport(mouseX, mouseY)) return false;

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private final class NoteEntryWidget extends AbstractWidget {
        private final PathNote note;
        private final StyledButton removeButton;

        private NoteEntryWidget(PathNote note) {
            super(0, 0, 0, 36, Component.literal(note.component().getString().replace('\n', ' ')));
            this.note = note;
            removeButton = new StyledButton(
                    0,
                    0,
                    20,
                    20,
                    Component.literal("×"),
                    Texture.MANAGER_WIDGET_BACKGROUND_RED,
                    () -> onRemove.accept(note));
            removeButton.setTooltip(
                    Tooltip.create(Component.translatable("screens.wynntils.map.paths.noteEditor.remove")));
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            RenderUtils.drawNineSliceScalingTexturedRect(
                    graphics,
                    selected == note ? Texture.MANAGER_WIDGET_BACKGROUND_GREEN : Texture.MANAGER_WIDGET_BACKGROUND,
                    getX(),
                    getY(),
                    getWidth(),
                    getHeight());
            FontRenderer.getInstance()
                    .renderScrollingText(
                            graphics,
                            StyledText.fromComponent(getMessage()),
                            getX() + 6,
                            getY() + 11,
                            getWidth() - 38,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
            FontRenderer.getInstance()
                    .renderScrollingText(
                            graphics,
                            StyledText.fromString(String.format(
                                    Locale.ROOT,
                                    "%.1f, %.1f, %.1f",
                                    note.position().x(),
                                    note.position().y(),
                                    note.position().z())),
                            getX() + 6,
                            getY() + 26,
                            getWidth() - 38,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            0.8f);
            removeButton.setX(getX() + getWidth() - 26);
            removeButton.setY(getY() + 8);
            removeButton.active = editable.getAsBoolean();
            removeButton.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
            if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;

            if (removeButton.mouseClicked(event, isDoubleClick)) return true;

            onSelect.accept(note);
            playDownSound(McUtils.mc().getSoundManager());

            return true;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {}
    }
}
