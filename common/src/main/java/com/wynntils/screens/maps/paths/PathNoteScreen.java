/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.paths;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.managers.widgets.StyledButton;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.screens.maps.managers.widgets.categorymanager.options.AbstractOptionWidget;
import com.wynntils.screens.maps.managers.widgets.categorymanager.options.ColorOptionWidget;
import com.wynntils.screens.maps.paths.widgets.PathNoteListWidget;
import com.wynntils.screens.maps.paths.widgets.PathToggleOptionWidget;
import com.wynntils.services.mapdata.paths.PathNote;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class PathNoteScreen extends WynntilsScreen {
    private static final int WIDTH = 576;
    private static final int HEIGHT = 320;

    private final Screen previous;
    private String content = "";
    private String fontId = "minecraft:default";
    private String positionX;
    private String positionY;
    private String positionZ;
    private CustomColor color = CommonColors.WHITE;
    private CustomColor shadowColor = CommonColors.BLACK;
    private boolean bold;
    private boolean italic;
    private boolean strikethrough;
    private boolean underlined;
    private ColorOptionWidget colorWidget;
    private ColorOptionWidget shadowWidget;
    private PathToggleOptionWidget boldWidget;
    private PathToggleOptionWidget italicWidget;
    private PathToggleOptionWidget strikeWidget;
    private PathToggleOptionWidget underlineWidget;
    private StyledButton saveButton;
    private StyledButton newButton;
    private PathNoteListWidget noteList;
    private PathNote selected;
    private Component error = Component.empty();
    private int x;
    private int y;

    public PathNoteScreen(Screen previous) {
        super(Component.translatable("screens.wynntils.map.paths.noteEditor.title"));
        this.previous = previous;
        loadNote(null);
    }

    @Override
    protected void doInit() {
        super.doInit();
        setFocusedTextInput(null);
        x = (width - WIDTH) / 2;
        y = (height - HEIGHT) / 2;
        int scroll = noteList == null ? 0 : noteList.scrollOffsetY;
        noteList = addRenderableWidget(new PathNoteListWidget(
                x + 15, y + 43, 200, 231, this::selectNote, this::removeNote, Services.MapPaths::canEditNotes));
        noteList.scrollOffsetY = scroll;
        noteList.setNotes(Services.MapPaths.getNotes(), selected);
        int left = x + 220;

        TexturedTextInputBoxWidget textInput = new TexturedTextInputBoxWidget(
                left, y + 43, 341, 20, value -> content = value, this, TexturedTextInputBoxWidget.Mode.STRING);
        textInput.setTextBoxInput(content);
        addRenderableWidget(textInput);

        colorWidget =
                new ColorOptionWidget(text("color"), text("colorDescription"), color, CommonColors.WHITE, this, true);
        shadowWidget = new ColorOptionWidget(
                text("shadowColor"), text("shadowDescription"), shadowColor, CommonColors.BLACK, this, true);
        colorWidget.setX(left);
        colorWidget.setY(y + 70);
        colorWidget.setWidth(341);
        shadowWidget.setX(left);
        shadowWidget.setY(y + 94);
        shadowWidget.setWidth(341);
        addRenderableWidget(colorWidget);
        addRenderableWidget(shadowWidget);

        boldWidget = toggle(left, y + 118, "bold", bold);
        italicWidget = toggle(left + 173, y + 118, "italic", italic);
        underlineWidget = toggle(left, y + 142, "underlined", underlined);
        strikeWidget = toggle(left + 173, y + 142, "strikethrough", strikethrough);

        TexturedTextInputBoxWidget fontInput = new TexturedTextInputBoxWidget(
                left + 55, y + 166, 286, 20, value -> fontId = value, this, TexturedTextInputBoxWidget.Mode.STRING);
        fontInput.setTextBoxInput(fontId);
        fontInput.setTooltip(Tooltip.create(text("fontHint")));
        addRenderableWidget(fontInput);

        TexturedTextInputBoxWidget inputX = new TexturedTextInputBoxWidget(
                left, y + 202, 110, 20, value -> positionX = value, this, TexturedTextInputBoxWidget.Mode.FLOAT);
        TexturedTextInputBoxWidget inputY = new TexturedTextInputBoxWidget(
                left + 115, y + 202, 110, 20, value -> positionY = value, this, TexturedTextInputBoxWidget.Mode.FLOAT);
        TexturedTextInputBoxWidget inputZ = new TexturedTextInputBoxWidget(
                left + 230, y + 202, 111, 20, value -> positionZ = value, this, TexturedTextInputBoxWidget.Mode.FLOAT);
        inputX.setTextBoxInput(positionX);
        inputY.setTextBoxInput(positionY);
        inputZ.setTextBoxInput(positionZ);
        addRenderableWidget(inputX);
        addRenderableWidget(inputY);
        addRenderableWidget(inputZ);

        newButton = addRenderableWidget(new StyledButton(
                x + 15, y + 282, 200, 20, text("new"), Texture.MANAGER_WIDGET_BACKGROUND, () -> selectNote(null)));
        saveButton = addRenderableWidget(new StyledButton(
                left,
                y + 282,
                168,
                20,
                text("save"),
                Texture.MANAGER_WIDGET_BACKGROUND_GREEN,
                Texture.MANAGER_WIDGET_BACKGROUND_DARKGREEN,
                this::saveNote));
        addRenderableWidget(new StyledButton(
                left + 173, y + 282, 168, 20, text("done"), Texture.MANAGER_WIDGET_BACKGROUND, this::onClose));
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previous);
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(graphics, Texture.MANAGER_BACKGROUND, x, y);
        RenderUtils.drawNineSliceScalingTexturedRect(
                graphics, Texture.MANAGER_SCROLL_BAR_HEADER, x + 15, y + 18, 150, 16);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(getTitle()),
                        x + 35,
                        y + 26,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(text("text")),
                        x + 220,
                        y + 33,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(text("font")),
                        x + 220,
                        y + 176,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(Component.literal("X")),
                        x + 220,
                        y + 192,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(Component.literal("Y")),
                        x + 335,
                        y + 192,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(Component.literal("Z")),
                        x + 450,
                        y + 192,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
        saveButton.active = Services.MapPaths.canEditNotes();
        newButton.active = Services.MapPaths.canEditNotes();
        renderables.forEach(widget -> widget.render(graphics, mouseX, mouseY, partialTick));

        RenderUtils.drawNineSliceScalingTexturedRect(
                graphics, Texture.MANAGER_SCROLL_BAR_HEADER, x + 220, y + 230, 150, 16);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(text("preview")),
                        x + 240,
                        y + 238,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
        RenderUtils.drawNineSliceScalingTexturedRect(
                graphics, Texture.MANAGER_WIDGET_BACKGROUND, x + 220, y + 254, 341, 20);
        Identifier font = Identifier.tryParse(fontId);
        if (font != null && !fontId.isBlank() && !content.isBlank()) {
            RenderUtils.enableScissor(graphics, x + 225, y + 257, 331, 14);
            FontRenderer.getInstance()
                    .renderScrollingText(
                            graphics,
                            StyledText.fromComponent(component(font)),
                            x + 228,
                            y + 264,
                            325,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
            RenderUtils.disableScissor(graphics);
        }

        if (!error.getString().isEmpty()) {
            FontRenderer.getInstance()
                    .renderScrollingText(
                            graphics,
                            StyledText.fromComponent(error),
                            x + 561,
                            y + 26,
                            341,
                            CommonColors.RED,
                            HorizontalAlignment.RIGHT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

        for (AbstractOptionWidget<?> widget :
                List.of(colorWidget, shadowWidget, boldWidget, italicWidget, underlineWidget, strikeWidget)) {
            List<Component> lines = widget.getTooltipLines(mouseX, mouseY);
            if (!lines.isEmpty()) {
                graphics.setTooltipForNextFrame(
                        lines.stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        for (ColorOptionWidget widget : List.of(colorWidget, shadowWidget)) {
            if (widget.isMouseOverTextBox(event.x(), event.y())) {
                setFocusedTextInput(widget.getTextInputBoxWidget(event.x(), event.y()));
            }
        }

        boolean handled = super.doMouseClicked(event, isDoubleClick);
        TextInputBoxWidget focused = getFocusedTextInput();
        if (focused != null && !focused.isMouseOver(event.x(), event.y())) {
            setFocusedTextInput(null);
        }

        return handled;
    }

    @Override
    protected void rebuildWidgets() {
        collectStyle();
        super.rebuildWidgets();
    }

    private void loadNote(PathNote note) {
        selected = note;
        error = Component.empty();
        Vec3 position;
        if (note == null) {
            content = "";
            fontId = "minecraft:default";
            color = CommonColors.WHITE;
            shadowColor = CommonColors.BLACK;
            bold = false;
            italic = false;
            underlined = false;
            strikethrough = false;
            position = McUtils.player() == null
                    ? Vec3.ZERO
                    : McUtils.player().getRootVehicle().position();
        } else {
            content = note.component().getString();
            PathNoteStyle style = PathNoteStyle.from(note.component());
            fontId = style.font().toString();
            color = style.color();
            shadowColor = style.shadowColor();
            bold = style.bold();
            italic = style.italic();
            underlined = style.underlined();
            strikethrough = style.strikethrough();
            position = note.position();
        }

        positionX = String.format(Locale.ROOT, "%.1f", position.x());
        positionY = String.format(Locale.ROOT, "%.1f", position.y());
        positionZ = String.format(Locale.ROOT, "%.1f", position.z());
    }

    private void selectNote(PathNote note) {
        loadNote(note);
        super.rebuildWidgets();
    }

    private void removeNote(PathNote note) {
        int index = Services.MapPaths.getNotes().indexOf(note);
        if (!Services.MapPaths.deleteNote(note)) {
            error = text("missingNote");

            return;
        }

        if (selected == note) {
            List<PathNote> remaining = Services.MapPaths.getNotes();
            selectNote(remaining.isEmpty() ? null : remaining.get(Math.min(index, remaining.size() - 1)));
        } else {
            rebuildWidgets();
        }
    }

    private PathToggleOptionWidget toggle(int x, int y, String key, boolean value) {
        PathToggleOptionWidget widget =
                new PathToggleOptionWidget(text(key), text(key + "Description"), attributes -> Optional.of(false));
        widget.updateFromAttributes(Optional.empty(), Optional.empty());
        widget.setValue(value);
        widget.setInherited(false);
        widget.setX(x);
        widget.setY(y);
        widget.setWidth(168);

        return addRenderableWidget(widget);
    }

    private void collectStyle() {
        if (colorWidget == null) return;

        color = colorWidget.getValue();
        shadowColor = shadowWidget.getValue();
        bold = boldWidget.getValue();
        italic = italicWidget.getValue();
        underlined = underlineWidget.getValue();
        strikethrough = strikeWidget.getValue();
    }

    private Component component(Identifier font) {
        collectStyle();
        PathNoteStyle style = new PathNoteStyle(font, color, shadowColor, bold, italic, underlined, strikethrough);

        return selected == null ? style.component(content) : style.apply(selected.component(), content);
    }

    private void saveNote() {
        if (content.isBlank()) {
            error = text("emptyText");

            return;
        }

        Identifier font = Identifier.tryParse(fontId);
        if (font == null || fontId.isBlank()) {
            error = text("invalidFont");

            return;
        }

        Vec3 position;
        try {
            position = new Vec3(
                    Double.parseDouble(positionX), Double.parseDouble(positionY), Double.parseDouble(positionZ));
        } catch (NumberFormatException e) {
            error = text("invalidPosition");

            return;
        }

        if (!Double.isFinite(position.x()) || !Double.isFinite(position.y()) || !Double.isFinite(position.z())) {
            error = text("invalidPosition");

            return;
        }

        if (selected == null) {
            if (!Services.MapPaths.addNote(position, component(font))) {
                error = text("noDraft");

                return;
            }
        } else {
            PathNote updated = Services.MapPaths.updateNote(selected, position, component(font));
            if (updated == null) {
                error = text("missingNote");

                return;
            }
        }

        selectNote(null);
    }

    private Component text(String key) {
        return Component.translatable("screens.wynntils.map.paths.noteEditor." + key);
    }
}
