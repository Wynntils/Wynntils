/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.paths.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.screens.maps.managers.widgets.StyledButton;
import com.wynntils.screens.maps.managers.widgets.categorymanager.options.AbstractOptionWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class PathNotesOptionWidget extends AbstractOptionWidget<Void> {
    private final IntSupplier noteCount;
    private final BooleanSupplier editable;
    private final StyledButton addButton;

    public PathNotesOptionWidget(IntSupplier noteCount, BooleanSupplier editable, Runnable onAdd) {
        super(Component.empty(), Component.empty(), 20, OptionCategory.PATH, null);
        this.noteCount = noteCount;
        this.editable = editable;
        addButton = new StyledButton(
                0,
                0,
                90,
                20,
                Component.translatable("screens.wynntils.map.paths.addNote"),
                Texture.MANAGER_WIDGET_BACKGROUND,
                onAdd);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.map.paths.notesCount", noteCount.getAsInt())),
                        getX(),
                        getY() + getHeight() / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
        addButton.setX(getX() + getWidth() - addButton.getWidth());
        addButton.setY(getY());
        addButton.active = editable.getAsBoolean();
        addButton.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        addButton.active = editable.getAsBoolean();

        return addButton.mouseClicked(event, isDoubleClick);
    }

    @Override
    public List<Component> getTooltipLines(double mouseX, double mouseY) {
        return List.of();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}
}
