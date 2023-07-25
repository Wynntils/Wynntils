/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.selection;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.WynntilsMenuScreenBase;
import com.wynntils.screens.overlays.placement.OverlayManagementScreen;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class OverlaySelectionScreen extends WynntilsScreen {
    private static final int BUTTON_WIDTH = 60;
    private static final int BUTTON_HEIGHT = 20;

    private OverlayList overlayList;

    private OverlaySelectionScreen() {
        super(Component.translatable("screens.wynntils.overlaySelection.name"));
    }

    public static Screen create() {
        return new OverlaySelectionScreen();
    }

    @Override
    protected void doInit() {
        overlayList = new OverlayList(this);
        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.overlaySelection.close"),
                        button -> WynntilsMenuScreenBase.openBook(WynntilsMenuScreen.create()))
                .pos(
                        (int) (this.width / 2 - BUTTON_WIDTH * 1.5f),
                        this.height / 10 + Texture.OVERLAY_SELECTION_GUI.height() + 20)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.overlaySelection.freeMove"),
                        button -> McUtils.mc().setScreen(OverlayManagementScreen.create()))
                .pos(
                        (int) (this.width / 2 + BUTTON_WIDTH * 0.5f),
                        this.height / 10 + Texture.OVERLAY_SELECTION_GUI.height() + 20)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        poseStack.pushPose();

        int backgroundColor = CommonColors.DARK_GRAY.withAlpha(200).asInt();
        this.fillGradient(poseStack, 0, 0, this.width, this.height, backgroundColor, backgroundColor);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Texture.OVERLAY_SELECTION_GUI.resource());
        int x = (this.width - Texture.OVERLAY_SELECTION_GUI.width()) / 2;
        int y = this.height / 10;

        poseStack.translate(x, y, 0);

        blit(
                poseStack,
                0,
                0,
                0,
                0,
                Texture.OVERLAY_SELECTION_GUI.width(),
                Texture.OVERLAY_SELECTION_GUI.height(),
                Texture.OVERLAY_SELECTION_GUI.width(),
                Texture.OVERLAY_SELECTION_GUI.height());

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.overlaySelection.overlays")),
                        5,
                        4,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        poseStack.popPose();

        overlayList.render(poseStack, mouseX, mouseY, partialTick);

        super.doRender(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        this.init();
        super.resize(minecraft, width, height);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return overlayList.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return overlayList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        super.doMouseClicked(mouseX, mouseY, button);
        return overlayList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return overlayList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        }

        return overlayList.keyPressed(keyCode, scanCode, modifiers);
    }
}
