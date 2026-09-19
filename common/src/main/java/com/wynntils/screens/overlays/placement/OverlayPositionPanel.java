/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.placement;

import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public final class OverlayPositionPanel {
    private static final int WIDTH = 204;
    private static final int HEIGHT = 148;
    private static final CustomColor TEXT_COLOR = CustomColor.fromARGBInt(0xFF3E2418);

    private final int x;
    private final int y;
    private final Overlay overlay;
    private final List<AbstractWidget> widgets = new ArrayList<>();
    private final List<TextInputBoxWidget> numberInputs = new ArrayList<>();
    private final List<Boolean> sizeInputs = new ArrayList<>();
    private final Button horizontalAlignmentButton;
    private final Button verticalAlignmentButton;
    private final LockIconButton placementLockButton;

    public OverlayPositionPanel(
            Overlay overlay,
            int mouseX,
            int mouseY,
            int screenWidth,
            int screenHeight,
            TextboxScreen screen,
            Consumer<Boolean> placementLockChanged) {
        this.overlay = overlay;
        x = Math.max(0, Math.min(mouseX + 8, screenWidth - WIDTH));
        y = Math.max(0, Math.min(mouseY + 8, screenHeight - HEIGHT));

        addNumber(
                screen, "x", x + 8, y + 32, overlay.getRenderX(), false, value -> moveTo(value, overlay.getRenderY()));
        addNumber(
                screen,
                "y",
                x + 106,
                y + 32,
                overlay.getRenderY(),
                false,
                value -> moveTo(overlay.getRenderX(), value));
        addNumber(
                screen, "width", x + 8, y + 68, overlay.getWidth(), true, value -> resize(value, overlay.getHeight()));
        addNumber(
                screen,
                "height",
                x + 106,
                y + 68,
                overlay.getHeight(),
                true,
                value -> resize(overlay.getWidth(), value));

        horizontalAlignmentButton = Button.builder(horizontalLabel(), button -> {
                    if (overlay.isPlacementLocked()) return;
                    HorizontalAlignment[] values = HorizontalAlignment.values();
                    HorizontalAlignment alignment =
                            values[(overlay.getRenderHorizontalAlignment().ordinal() + 1) % values.length];
                    overlay.getConfigOptionFromString("horizontalAlignmentOverride")
                            .ifPresent(config -> ((Config<HorizontalAlignment>) config).setValue(alignment));
                    button.setMessage(horizontalLabel());
                })
                .bounds(x + 8, y + 116, 90, 20)
                .build();
        widgets.add(horizontalAlignmentButton);
        verticalAlignmentButton = Button.builder(verticalLabel(), button -> {
                    if (overlay.isPlacementLocked()) return;
                    VerticalAlignment[] values = VerticalAlignment.values();
                    VerticalAlignment alignment =
                            values[(overlay.getRenderVerticalAlignment().ordinal() + 1) % values.length];
                    overlay.getConfigOptionFromString("verticalAlignmentOverride")
                            .ifPresent(config -> ((Config<VerticalAlignment>) config).setValue(alignment));
                    button.setMessage(verticalLabel());
                })
                .bounds(x + 106, y + 116, 90, 20)
                .build();
        widgets.add(verticalAlignmentButton);

        placementLockButton = new LockIconButton(
                x + WIDTH - 24, y + 4, button -> placementLockChanged.accept(!overlay.isPlacementLocked()));
        placementLockButton.setSize(16, 16);
        widgets.add(placementLockButton);
        setPlacementLocked(overlay.isPlacementLocked());
    }

    public List<AbstractWidget> getWidgets() {
        return widgets;
    }

    public boolean contains(double mouseX, double mouseY) {
        return MathUtils.isInside(
                (int) Math.floor(mouseX), (int) Math.floor(mouseY), x, x + WIDTH - 1, y, y + HEIGHT - 1);
    }

    public boolean isPlacementLocked() {
        return overlay.isPlacementLocked();
    }

    public void setPlacementLocked(boolean locked) {
        for (int i = 0; i < numberInputs.size(); i++) {
            TextInputBoxWidget input = numberInputs.get(i);
            input.active = !locked;
            input.setRenderColor(
                    locked
                            ? CommonColors.GRAY
                            : isValidNumber(input.getTextBoxInput(), sizeInputs.get(i))
                                    ? CommonColors.WHITE
                                    : CommonColors.RED);
        }
        horizontalAlignmentButton.active = !locked;
        verticalAlignmentButton.active = !locked;
        placementLockButton.setMessage(lockLabel());
        placementLockButton.setLocked(locked);
        placementLockButton.setTooltip(Tooltip.create(lockLabel()));
    }

    public void render(GuiGraphics graphics) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                graphics, Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND, x, y, WIDTH, HEIGHT);
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromString(FontRenderer.getInstance()
                                .getFont()
                                .plainSubstrByWidth(overlay.getTranslatedName(), WIDTH - 36)),
                        x + 8,
                        y + 7,
                        TEXT_COLOR,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        label(graphics, "x", x + 8, y + 22);
        label(graphics, "y", x + 106, y + 22);
        label(graphics, "width", x + 8, y + 58);
        label(graphics, "height", x + 106, y + 58);
        label(graphics, "alignment", x + 8, y + 100);
    }

    private void label(GuiGraphics graphics, String key, int labelX, int labelY) {
        FontRenderer.getInstance()
                .renderText(
                        graphics,
                        StyledText.fromComponent(text(key)),
                        labelX,
                        labelY,
                        TEXT_COLOR,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }

    private Component horizontalLabel() {
        return text(overlay.getRenderHorizontalAlignment().name().toLowerCase(Locale.ROOT));
    }

    private Component verticalLabel() {
        return text(overlay.getRenderVerticalAlignment().name().toLowerCase(Locale.ROOT));
    }

    private Component lockLabel() {
        return text(overlay.isPlacementLocked() ? "unlockPlacement" : "lockPlacement");
    }

    private static Component text(String key) {
        return Component.translatable("screens.wynntils.overlayManagement.positionPanel." + key);
    }

    private void moveTo(float newX, float newY) {
        if (overlay.isPlacementLocked()) return;
        overlay.setPosition(OverlayPosition.getBestPositionFor(
                overlay,
                overlay.getRenderX(),
                overlay.getRenderY(),
                newX - overlay.getRenderX(),
                newY - overlay.getRenderY()));
    }

    private void resize(float width, float height) {
        if (overlay.isPlacementLocked()) return;
        float oldX = overlay.getRenderX();
        float oldY = overlay.getRenderY();
        overlay.setWidth(width);
        overlay.setHeight(height);
        moveTo(oldX, oldY);
    }

    private boolean isValidNumber(String value, boolean size) {
        try {
            float number = Float.parseFloat(value);
            return Float.isFinite(number) && (!size || number >= 3);
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private void addNumber(
            TextboxScreen screen,
            String key,
            int fieldX,
            int fieldY,
            float initialValue,
            boolean size,
            Consumer<Float> update) {
        TextInputBoxWidget input = new TextInputBoxWidget(fieldX, fieldY, 90, 18, text(key), null, screen) {
            private boolean initialized;

            @Override
            protected void onUpdate(String value) {
                if (!initialized) {
                    initialized = true;
                    return;
                }
                if (overlay.isPlacementLocked()) return;
                try {
                    float number = Float.parseFloat(value);
                    if (!Float.isFinite(number) || (size && number < 3)) {
                        setRenderColor(CommonColors.RED);
                        return;
                    }
                    update.accept(number);
                    setRenderColor(CommonColors.WHITE);
                } catch (NumberFormatException exception) {
                    setRenderColor(CommonColors.RED);
                }
            }
        };
        input.setTextBoxInput(Float.toString(initialValue));
        numberInputs.add(input);
        sizeInputs.add(size);
        widgets.add(input);
    }
}
