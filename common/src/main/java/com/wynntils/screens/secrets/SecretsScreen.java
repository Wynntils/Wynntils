/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.secrets;

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

        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.SECRETS_BACKGROUND, offsetX, offsetY);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics.pose(),
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
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        if (!draggingScroll) {
            if (MathUtils.isInside(
                    (int) mouseX,
                    (int) mouseY,
                    offsetX + 336,
                    offsetX + 336 + Texture.SCROLL_BUTTON.width(),
                    (int) scrollY,
                    (int) (scrollY + Texture.SCROLL_BUTTON.height()))) {
                draggingScroll = true;

                return true;
            }
        }

        for (SecretInputWidget secretInput : secretInputs) {
            if (secretInput.isMouseOver(mouseX, mouseY)) {
                return secretInput.mouseClicked(mouseX, mouseY, button);
            }
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScroll) {
            int scrollAreaStartY = offsetY + 7 + 17;
            int scrollAreaHeight = SCROLL_AREA_HEIGHT - Texture.SCROLL_BUTTON.height();

            int newOffset = Math.round(MathUtils.map(
                    (float) mouseY, scrollAreaStartY, scrollAreaStartY + scrollAreaHeight, 0, getMaxScrollOffset()));

            newOffset = Math.max(0, Math.min(newOffset, getMaxScrollOffset()));

            scroll(newOffset);

            return true;
        }

        for (SecretInputWidget secretInput : secretInputs) {
            if (secretInput.isMouseOver(mouseX, mouseY)) {
                return secretInput.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;

        for (SecretInputWidget secretInput : secretInputs) {
            if (secretInput.isMouseOver(mouseX, mouseY)) {
                return secretInput.mouseReleased(mouseX, mouseY, button);
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
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

        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.SCROLL_BUTTON, offsetX + 336, scrollY);
    }
}
