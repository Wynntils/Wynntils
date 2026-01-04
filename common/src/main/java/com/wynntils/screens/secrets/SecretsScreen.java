/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.secrets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.secrets.widgets.SecretInputWidget;
import com.wynntils.services.secrets.type.WynntilsSecret;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class SecretsScreen extends WynntilsScreen {
    private static final float SCROLL_FACTOR = 10f;
    private static final int SCROLL_AREA_HEIGHT = 121;
    private static final int WIDGETS_PER_PAGE = 6;

    private boolean draggingScroll = false;
    private float scrollY;
    private int scrollOffset = 0;
    private int offsetX;
    private int offsetY;

    private List<SecretInputWidget> secretInputs = new ArrayList<>();

    private SecretsScreen() {
        super(Component.literal("Wynntils Secrets Screen"));
    }

    public static SecretsScreen create() {
        return new SecretsScreen();
    }

    @Override
    public void doInit() {
        offsetX = (this.width - Texture.SECRETS_BACKGROUND.width()) / 2;
        offsetY = (this.height - Texture.SECRETS_BACKGROUND.height()) / 2;

        this.addRenderableWidget(
                new Button.Builder(Component.translatable("screens.wynntils.secrets.close"), (b) -> onClose())
                        .pos(offsetX + 50, offsetY - 21)
                        .size(250, 20)
                        .build());

        populateSecrets();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtils.drawTexturedRect(guiGraphics, Texture.SECRETS_BACKGROUND, offsetX, offsetY);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.secrets.warning")),
                        offsetX + 75,
                        offsetX + 275,
                        offsetY - 42,
                        offsetY - 22,
                        200,
                        CommonColors.RED,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.OUTLINE);

        renderSecrets(guiGraphics, mouseX, mouseY, partialTick);

        renderScroll(guiGraphics);

        if (draggingScroll) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else if (MathUtils.isInside(
                mouseX, mouseY, offsetX + 336, offsetX + 336 + Texture.SCROLL_BUTTON.width(), (int) scrollY, (int)
                        (scrollY + Texture.SCROLL_BUTTON.height()))) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!draggingScroll) {
            if (MathUtils.isInside(
                    (int) event.x(),
                    (int) event.y(),
                    offsetX + 336,
                    offsetX + 336 + Texture.SCROLL_BUTTON.width(),
                    (int) scrollY,
                    (int) (scrollY + Texture.SCROLL_BUTTON.height()))) {
                draggingScroll = true;

                return true;
            }
        }

        for (SecretInputWidget secretInput : secretInputs) {
            if (secretInput.isMouseOver(event.x(), event.y())) {
                return secretInput.mouseClicked(event, isDoubleClick);
            }
        }

        return super.doMouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScroll) {
            int scrollAreaStartY = offsetY + 7 + 17;
            int scrollAreaHeight = SCROLL_AREA_HEIGHT - Texture.SCROLL_BUTTON.height();

            int newOffset = Math.round(MathUtils.map(
                    (float) event.y(), scrollAreaStartY, scrollAreaStartY + scrollAreaHeight, 0, getMaxScrollOffset()));

            newOffset = Math.max(0, Math.min(newOffset, getMaxScrollOffset()));

            scroll(newOffset);

            return true;
        }

        for (SecretInputWidget secretInput : secretInputs) {
            if (secretInput.isMouseOver(event.x(), event.y())) {
                return secretInput.mouseDragged(event, dragX, dragY);
            }
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScroll = false;

        for (SecretInputWidget secretInput : secretInputs) {
            if (secretInput.isMouseOver(event.x(), event.y())) {
                return secretInput.mouseReleased(event);
            }
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);
        int newOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, getMaxScrollOffset()));
        scroll(newOffset);

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private void populateSecrets() {
        secretInputs = new ArrayList<>();

        int renderX = offsetX + 9;
        int renderY = offsetY + 10;

        for (WynntilsSecret wynntilsSecret : WynntilsSecret.values()) {
            SecretInputWidget secretInputWidget =
                    new SecretInputWidget(renderX, renderY, 322, 20, this, wynntilsSecret);

            secretInputs.add(secretInputWidget);
            renderY += 22;
        }
    }

    private void scroll(int newOffset) {
        scrollOffset = newOffset;
        int currentY = offsetY + 10;

        for (AbstractWidget widget : secretInputs) {
            int newY = currentY - scrollOffset;
            widget.setY(newY);
            widget.visible = (newY <= offsetY + 10 + 135) && (newY + widget.getHeight() >= offsetY + 10);
            currentY += 22;
        }
    }

    private int getMaxScrollOffset() {
        return (secretInputs.size() - WIDGETS_PER_PAGE) * 22;
    }

    private void renderSecrets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.enableScissor(guiGraphics, offsetX + 9, offsetY + 8, 322, 134);
        secretInputs.forEach(secretInput -> secretInput.render(guiGraphics, mouseX, mouseY, partialTick));
        RenderUtils.disableScissor(guiGraphics);
    }

    private void renderScroll(GuiGraphics guiGraphics) {
        scrollY = offsetY
                + 7
                + MathUtils.map(scrollOffset, 0, getMaxScrollOffset(), 0, 135 - Texture.SCROLL_BUTTON.height());

        RenderUtils.drawTexturedRect(guiGraphics, Texture.SCROLL_BUTTON, offsetX + 336, scrollY);
    }
}
