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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class PathNoteScreen extends WynntilsScreen {
    private static final Identifier DEFAULT_FONT = Identifier.withDefaultNamespace("default");
    private static final int WIDTH = 576;
    private static final int HEIGHT = 320;
    private static final int CONTENT_TOP = 43;
    private static final int NOTE_LIST_X = 15;
    private static final int NOTE_LIST_WIDTH = 200;
    private static final int CONTENT_BOTTOM = 274;
    private static final int OPTIONS_X = 220;
    private static final int OPTIONS_BOTTOM = 239;
    private static final int OPTIONS_WIDTH = 341;
    private static final int OPTIONS_PADDING = 6;
    private static final int INPUT_GAP = 5;
    private static final int INPUT_WIDTH = OPTIONS_WIDTH - 2 * OPTIONS_PADDING;
    private static final int COORDINATE_WIDTH = (INPUT_WIDTH - 2 * INPUT_GAP) / 3;
    private static final int COORDINATE_STEP = COORDINATE_WIDTH + INPUT_GAP;
    private static final int BUTTON_Y = 282;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_GAP = BUTTON_WIDTH / 2;
    private static final int PREVIEW_PADDING = 5;
    private static final CustomColor PREVIEW_TITLE_COLOR = CustomColor.fromInt(0x524030);

    private final Screen previous;
    private String content = "";
    private String fontId = DEFAULT_FONT.toString();
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
    private boolean editingNote;
    private Component error = Component.empty();
    private int x;
    private int y;

    public PathNoteScreen(Screen previous) {
        super(Component.translatable("screens.wynntils.map.paths.noteEditor.title"));
        this.previous = previous;
    }

    @Override
    protected void doInit() {
        super.doInit();
        setFocusedTextInput(null);
        x = (width - WIDTH) / 2;
        y = (height - HEIGHT) / 2;
        int scroll = noteList == null ? 0 : noteList.scrollOffsetY;
        noteList = addRenderableWidget(new PathNoteListWidget(
                x + NOTE_LIST_X,
                y + CONTENT_TOP,
                NOTE_LIST_WIDTH,
                CONTENT_BOTTOM - CONTENT_TOP,
                this::selectNote,
                this::removeNote,
                Services.MapPaths::canEditNotes));
        noteList.scrollOffsetY = scroll;
        noteList.setNotes(Services.MapPaths.getNotes(), selected);

        int doneX = x + (WIDTH - BUTTON_WIDTH) / 2;
        newButton = addRenderableWidget(new StyledButton(
                doneX - BUTTON_GAP - BUTTON_WIDTH,
                y + BUTTON_Y,
                BUTTON_WIDTH,
                20,
                text("new"),
                Texture.MANAGER_WIDGET_BACKGROUND_GREEN,
                Texture.MANAGER_WIDGET_BACKGROUND_DARKGREEN,
                () -> selectNote(null)));
        addRenderableWidget(new StyledButton(
                doneX, y + BUTTON_Y, BUTTON_WIDTH, 20, text("done"), Texture.MANAGER_WIDGET_BACKGROUND, this::onClose));
        saveButton = addRenderableWidget(new StyledButton(
                doneX + BUTTON_WIDTH + BUTTON_GAP,
                y + BUTTON_Y,
                BUTTON_WIDTH,
                20,
                text("save"),
                Texture.MANAGER_WIDGET_BACKGROUND_GREEN,
                Texture.MANAGER_WIDGET_BACKGROUND_DARKGREEN,
                this::saveNote));
        updateButtons();

        if (editingNote) {
            addNoteOptions();
        }
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
        if (editingNote) {
            renderNoteOptions(graphics);
        }

        updateButtons();
        renderables.forEach(widget -> widget.render(graphics, mouseX, mouseY, partialTick));

        if (editingNote) {
            renderPreview(graphics);
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

        if (!editingNote) return;

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
        if (editingNote) {
            for (ColorOptionWidget widget : List.of(colorWidget, shadowWidget)) {
                if (widget.isMouseOverTextBox(event.x(), event.y())) {
                    setFocusedTextInput(widget.getTextInputBoxWidget(event.x(), event.y()));
                }
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

    private void updateButtons() {
        boolean canEditNotes = Services.MapPaths.canEditNotes();
        newButton.active = canEditNotes;
        saveButton.active = canEditNotes && editingNote && !content.isBlank();
    }

    private void addNoteOptions() {
        int left = x + OPTIONS_X + OPTIONS_PADDING;
        int toggleWidth = (INPUT_WIDTH - INPUT_GAP) / 2;
        int rightToggle = left + toggleWidth + INPUT_GAP;

        TexturedTextInputBoxWidget textInput = new TexturedTextInputBoxWidget(
                left, y + 59, INPUT_WIDTH, 20, value -> content = value, this, TexturedTextInputBoxWidget.Mode.STRING);
        textInput.setTextBoxInput(content);
        addRenderableWidget(textInput);

        colorWidget =
                new ColorOptionWidget(text("color"), text("colorDescription"), color, CommonColors.WHITE, this, true);
        shadowWidget = new ColorOptionWidget(
                text("shadowColor"), text("shadowDescription"), shadowColor, CommonColors.BLACK, this, true);
        colorWidget.setX(left);
        colorWidget.setY(y + 84);
        colorWidget.setWidth(INPUT_WIDTH);
        shadowWidget.setX(left);
        shadowWidget.setY(y + 107);
        shadowWidget.setWidth(INPUT_WIDTH);
        addRenderableWidget(colorWidget);
        addRenderableWidget(shadowWidget);

        boldWidget = toggle(left, y + 130, toggleWidth, "bold", bold);
        italicWidget = toggle(rightToggle, y + 130, toggleWidth, "italic", italic);
        underlineWidget = toggle(left, y + 153, toggleWidth, "underlined", underlined);
        strikeWidget = toggle(rightToggle, y + 153, toggleWidth, "strikethrough", strikethrough);

        TexturedTextInputBoxWidget fontInput = new TexturedTextInputBoxWidget(
                left + 55,
                y + 177,
                INPUT_WIDTH - 55,
                20,
                value -> fontId = value,
                this,
                TexturedTextInputBoxWidget.Mode.STRING);
        fontInput.setTextBoxInput(fontId);
        fontInput.setTooltip(Tooltip.create(text("fontHint")));
        addRenderableWidget(fontInput);

        TexturedTextInputBoxWidget inputX = new TexturedTextInputBoxWidget(
                left,
                y + 213,
                COORDINATE_WIDTH,
                20,
                value -> positionX = value,
                this,
                TexturedTextInputBoxWidget.Mode.FLOAT);
        TexturedTextInputBoxWidget inputY = new TexturedTextInputBoxWidget(
                left + COORDINATE_STEP,
                y + 213,
                COORDINATE_WIDTH,
                20,
                value -> positionY = value,
                this,
                TexturedTextInputBoxWidget.Mode.FLOAT);
        TexturedTextInputBoxWidget inputZ = new TexturedTextInputBoxWidget(
                left + 2 * COORDINATE_STEP,
                y + 213,
                INPUT_WIDTH - 2 * COORDINATE_STEP,
                20,
                value -> positionZ = value,
                this,
                TexturedTextInputBoxWidget.Mode.FLOAT);
        inputX.setTextBoxInput(positionX);
        inputY.setTextBoxInput(positionY);
        inputZ.setTextBoxInput(positionZ);
        addRenderableWidget(inputX);
        addRenderableWidget(inputY);
        addRenderableWidget(inputZ);
    }

    private void renderNoteOptions(GuiGraphics graphics) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                graphics,
                Texture.MANAGER_WIDGET_BACKGROUND,
                x + OPTIONS_X,
                y + CONTENT_TOP,
                OPTIONS_WIDTH,
                OPTIONS_BOTTOM - CONTENT_TOP);
        int optionsLeft = x + OPTIONS_X + OPTIONS_PADDING;

        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(text("text")),
                        optionsLeft,
                        y + 49,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(text("font")),
                        optionsLeft,
                        y + 187,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(Component.literal("X")),
                        optionsLeft,
                        y + 203,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(Component.literal("Y")),
                        optionsLeft + COORDINATE_STEP,
                        y + 203,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(Component.literal("Z")),
                        optionsLeft + 2 * COORDINATE_STEP,
                        y + 203,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
    }

    private void renderPreview(GuiGraphics graphics) {
        int previewTop = OPTIONS_BOTTOM + 5;
        int previewTextX = x + OPTIONS_X + PREVIEW_PADDING;
        int previewTextWidth = OPTIONS_WIDTH - 2 * PREVIEW_PADDING;
        FontRenderer fontRenderer = FontRenderer.getInstance();
        RenderUtils.drawNineSliceScalingTexturedRect(
                graphics,
                Texture.MANAGER_TEXT_BOX_BACKGROUND,
                x + OPTIONS_X,
                y + previewTop,
                OPTIONS_WIDTH,
                CONTENT_BOTTOM - previewTop);
        fontRenderer.renderText(
                graphics,
                StyledText.fromComponent(text("preview").copy().withStyle(ChatFormatting.BOLD)),
                previewTextX,
                y + previewTop + PREVIEW_PADDING,
                PREVIEW_TITLE_COLOR,
                HorizontalAlignment.LEFT,
                VerticalAlignment.TOP,
                TextShadow.NONE);
        Identifier font = resolveFont();
        if (font != null && !content.isBlank()) {
            int bodyY = previewTop + PREVIEW_PADDING + fontRenderer.getFont().lineHeight + 2;
            RenderUtils.enableScissor(
                    graphics, previewTextX, y + bodyY, previewTextWidth, CONTENT_BOTTOM - PREVIEW_PADDING - bodyY);
            fontRenderer.renderScrollingText(
                    graphics,
                    StyledText.fromComponent(component(font)),
                    previewTextX,
                    y + bodyY,
                    previewTextWidth,
                    CommonColors.WHITE,
                    HorizontalAlignment.LEFT,
                    VerticalAlignment.TOP,
                    TextShadow.NORMAL);
            RenderUtils.disableScissor(graphics);
        }
    }

    private void loadNote(PathNote note) {
        selected = note;
        error = Component.empty();
        Vec3 position;
        if (note == null) {
            content = "";
            fontId = DEFAULT_FONT.toString();
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
        editingNote = true;
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
            if (remaining.isEmpty()) {
                closeNoteEditor();
            } else {
                selectNote(remaining.get(Math.min(index, remaining.size() - 1)));
            }
        } else {
            rebuildWidgets();
        }
    }

    private PathToggleOptionWidget toggle(int x, int y, int width, String key, boolean value) {
        PathToggleOptionWidget widget =
                new PathToggleOptionWidget(text(key), text(key + "Description"), attributes -> Optional.of(false));
        widget.updateFromAttributes(Optional.empty(), Optional.empty());
        widget.setValue(value);
        widget.setInherited(false);
        widget.setX(x);
        widget.setY(y);
        widget.setWidth(width);

        return addRenderableWidget(widget);
    }

    private void collectStyle() {
        if (!editingNote || colorWidget == null) return;

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

        Identifier font = resolveFont();
        if (font == null) {
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

        closeNoteEditor();
    }

    private void closeNoteEditor() {
        editingNote = false;
        selected = null;
        error = Component.empty();
        super.rebuildWidgets();
    }

    private Identifier resolveFont() {
        return fontId.isBlank() ? DEFAULT_FONT : Identifier.tryParse(fontId);
    }

    private Component text(String key) {
        return Component.translatable("screens.wynntils.map.paths.noteEditor." + key);
    }
}
