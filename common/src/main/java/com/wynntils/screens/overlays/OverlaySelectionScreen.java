/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.overlays.lists.OverlayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class OverlaySelectionScreen extends Screen {
    private static final int BUTTON_WIDTH = 60;
    private static final int BUTTON_HEIGHT = 20;

    private OverlayList overlayList;
    private final Screen lastScreen;

    public OverlaySelectionScreen() {
        super(new TranslatableComponent("screens.wynntils.overlaySelection.name"));
        lastScreen = McUtils.mc().screen;
    }

    @Override
    protected void init() {
        overlayList = new OverlayList(this);
        this.addRenderableWidget(new Button(
                this.width / 2 - BUTTON_WIDTH / 2,
                this.height / 10 + Texture.OVERLAY_SELECTION_GUI.height() + 20,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.overlaySelection.close"),
                button -> McUtils.mc().setScreen(lastScreen)));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
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
                        I18n.get("screens.wynntils.overlaySelection.overlays"),
                        5,
                        4,
                        CommonColors.WHITE,
                        FontRenderer.TextAlignment.LEFT_ALIGNED,
                        FontRenderer.TextShadow.NORMAL);

        poseStack.popPose();

        overlayList.render(poseStack, mouseX, mouseY, partialTick);

        super.render(poseStack, mouseX, mouseY, partialTick);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
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
