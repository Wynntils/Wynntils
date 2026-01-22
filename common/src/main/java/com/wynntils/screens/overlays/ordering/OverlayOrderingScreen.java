/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.ordering;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.screens.overlays.ordering.widgets.ElementTypeWidget;
import com.wynntils.screens.overlays.ordering.widgets.OverlayOrderWidget;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.RenderElementType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class OverlayOrderingScreen extends WynntilsScreen {
    private final Screen previousScreen;

    private List<AbstractWidget> widgets = new ArrayList<>();

    private int scrollOffset = 0;

    private OverlayOrderingScreen(Screen previousScreen) {
        super(Component.translatable("screens.wynntils.overlayOrdering.name"));

        this.previousScreen = previousScreen;
    }

    public static Screen create(Screen previousScreen) {
        return new OverlayOrderingScreen(previousScreen);
    }

    @Override
    protected void doInit() {
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
        widgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {}

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics) {}

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        for (AbstractWidget widget : widgets) {
            if (widget.isMouseOver(event.x(), event.y())) {
                return widget.mouseClicked(event, isDoubleClick);
            }
        }

        return super.doMouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int delta = (int) (deltaY * 10);
        scrollOffset += delta;

        widgets.forEach(widget -> widget.setY(widget.getY() + delta));
        return true;
    }

    public void reorderOverlay(Overlay overlay, int direction) {
        Managers.Overlay.updateOverlayPosition(overlay, direction);
        populateOverlays();
    }

    private void populateOverlays() {
        widgets.clear();

        Map<RenderElementType, List<Overlay>> overlays = Managers.Overlay.getRenderMap();

        int renderY = scrollOffset;
        for (RenderElementType type : RenderElementType.values()) {
            if (!type.isRootRender() || type == RenderElementType.GUI_PRE) continue;

            for (Overlay overlay : overlays.get(type)) {
                widgets.add(new OverlayOrderWidget(0, renderY, overlay, this));
                renderY += 20;
            }

            widgets.add(new ElementTypeWidget(0, renderY, type));
            renderY += 20;
        }
    }
}
