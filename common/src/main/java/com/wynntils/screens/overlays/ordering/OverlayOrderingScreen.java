/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.ordering;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.overlays.ordering.widgets.ElementTypeWidget;
import com.wynntils.screens.overlays.ordering.widgets.OverlayOrderWidget;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.RenderElementType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class OverlayOrderingScreen extends WynntilsScreen {
    private static final float SCROLL_FACTOR = 10f;
    private static final int WIDGETS_PER_PAGE = 9;

    private final Screen previousScreen;

    private List<AbstractWidget> widgets = new ArrayList<>();
    private Button saveButton;

    private boolean draggingScroll = false;
    private float scrollY;
    private int scrollOffset = 0;
    private int offsetX;
    private int offsetY;

    private StyledText[] helpText = {
        StyledText.fromComponent(Component.translatable("screens.wynntils.overlayOrdering.help1")),
        StyledText.fromString(""),
        StyledText.fromComponent(Component.translatable("screens.wynntils.overlayOrdering.help2")),
        StyledText.fromString(""),
        StyledText.fromComponent(Component.translatable("screens.wynntils.overlayOrdering.help3"))
    };

    private OverlayOrderingScreen(Screen previousScreen) {
        super(Component.translatable("screens.wynntils.overlayOrdering.name"));

        this.previousScreen = previousScreen;
    }

    public static Screen create(Screen previousScreen) {
        return new OverlayOrderingScreen(previousScreen);
    }

    @Override
    protected void doInit() {
        offsetX = (int) ((this.width - Texture.OVERLAY_ORDERING_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.OVERLAY_ORDERING_BACKGROUND.height()) / 2f);

        saveButton = new Button.Builder(
                        Component.translatable("screens.wynntils.overlayOrdering.save"), (b) -> onClose())
                .size(80, 20)
                .pos(
                        offsetX + Texture.OVERLAY_ORDERING_BACKGROUND.width() / 2 - 40,
                        offsetY + Texture.OVERLAY_ORDERING_BACKGROUND.height() + 4)
                .build();
        this.addRenderableWidget(saveButton);

        populateOverlays();
    }

    @Override
    public void onClose() {
        Managers.Overlay.rebuildRenderOrder();
        Managers.Config.saveConfig();
        McUtils.setScreen(previousScreen);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        RenderUtils.drawTexturedRect(guiGraphics, Texture.OVERLAY_ORDERING_BACKGROUND, offsetX, offsetY);

        renderScroll(guiGraphics);

        RenderUtils.enableScissor(guiGraphics, offsetX + 9, offsetY + 15, 200, 181);
        widgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
        RenderUtils.disableScissor(guiGraphics);

        int helpWidth = this.width / 2 - Texture.OVERLAY_ORDERING_BACKGROUND.width() / 2 - 10;
        float renderHeight = FontRenderer.getInstance().calculateRenderHeight(Arrays.asList(helpText), helpWidth - 2);
        RenderUtils.drawRect(
                guiGraphics,
                CommonColors.BLACK.withAlpha(0.5f),
                offsetX + Texture.OVERLAY_ORDERING_BACKGROUND.width() + 5,
                offsetY + 7,
                helpWidth,
                renderHeight);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        helpText,
                        offsetX + Texture.OVERLAY_ORDERING_BACKGROUND.width() + 6,
                        offsetX + Texture.OVERLAY_ORDERING_BACKGROUND.width() + 5 + helpWidth,
                        offsetY + 8,
                        offsetY + 9 + renderHeight,
                        helpWidth - 2,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL,
                        1f);

        if (draggingScroll) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else if (!draggingScroll && widgets.size() > WIDGETS_PER_PAGE) {
            if (MathUtils.isInside(
                    mouseX, mouseY, offsetX + 217, offsetX + 217 + Texture.SCROLL_BUTTON.width(), (int) scrollY, (int)
                            (scrollY + Texture.SCROLL_BUTTON.height()))) {
                guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
            }
        }
    }

    private void renderScroll(GuiGraphics guiGraphics) {
        scrollY = 14
                + offsetY
                + MathUtils.map(
                        scrollOffset, 0, getMaxScrollOffset(), 0, 187 - Texture.CONFIG_BOOK_SCROLL_BUTTON.height());

        RenderUtils.drawTexturedRect(guiGraphics, Texture.SCROLL_BUTTON, 217 + offsetX, scrollY);
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!draggingScroll && widgets.size() > WIDGETS_PER_PAGE) {
            if (MathUtils.isInside(
                    (int) event.x(),
                    (int) event.y(),
                    offsetX + 217,
                    offsetX + 217 + Texture.SCROLL_BUTTON.width(),
                    (int) scrollY,
                    (int) (scrollY + Texture.SCROLL_BUTTON.height()))) {
                draggingScroll = true;

                return true;
            }
        }

        if (saveButton.isMouseOver(event.x(), event.y())) {
            return saveButton.mouseClicked(event, isDoubleClick);
        }

        for (AbstractWidget widget : widgets) {
            if (widget.isMouseOver(event.x(), event.y())) {
                return widget.mouseClicked(event, isDoubleClick);
            }
        }

        return super.doMouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScroll) {
            int scrollAreaStartY = 14 + 10 + offsetY;
            int scrollAreaHeight = WIDGETS_PER_PAGE * 20 - Texture.SCROLL_BUTTON.height();

            int newOffset = Math.round(MathUtils.map(
                    (float) event.y(), scrollAreaStartY, scrollAreaStartY + scrollAreaHeight, 0, getMaxScrollOffset()));

            newOffset = Math.max(0, Math.min(newOffset, getMaxScrollOffset()));

            scroll(newOffset);

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScroll = false;

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);
        int newOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, getMaxScrollOffset()));
        scroll(newOffset);

        return true;
    }

    public void reorderOverlay(Overlay overlay, int direction) {
        Managers.Overlay.updateOverlayPosition(overlay, direction);
        populateOverlays();
    }

    private void populateOverlays() {
        widgets.clear();

        Map<RenderElementType, List<Overlay>> overlays = Managers.Overlay.getRenderMap();

        int renderY = offsetY + 15;
        int renderX = offsetX + 10;
        RenderElementType[] types = RenderElementType.values();
        for (int i = types.length - 1; i >= 0; i--) {
            RenderElementType type = types[i];
            if (!type.isRootRender() || type == RenderElementType.GUI_PRE) continue;

            // We don't need to add a widget for GUI_POST as nothing can render after it
            // so this prevents it from always appearing at the top
            if (type != RenderElementType.GUI_POST) {
                widgets.add(new ElementTypeWidget(renderX, renderY, type));
                renderY += 20;
            }

            List<Overlay> overlayList = overlays.get(type);
            for (int j = overlayList.size() - 1; j >= 0; j--) {
                Overlay overlay = overlayList.get(j);
                widgets.add(new OverlayOrderWidget(renderX, renderY, overlay, this));
                renderY += 20;
            }
        }

        scroll(scrollOffset);
    }

    private void scroll(int newOffset) {
        scrollOffset = newOffset;

        for (AbstractWidget widget : widgets) {
            int newY = 16 + offsetY + (widgets.indexOf(widget) * 20) - scrollOffset;
            widget.setY(newY);
            widget.visible = (newY <= offsetY + 15 + 181) && (newY + 20 >= offsetY + 15);
        }
    }

    private int getMaxScrollOffset() {
        return (widgets.size() - WIDGETS_PER_PAGE) * 20 + 1;
    }
}
