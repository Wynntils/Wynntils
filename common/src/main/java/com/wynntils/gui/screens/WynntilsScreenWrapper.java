/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.utils.McUtils;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class WynntilsScreenWrapper extends Screen {
    private final Screen delegate;

    private WynntilsScreenWrapper(Component component, Screen delegate) {
        super(component);

        this.delegate = delegate;
    }

    public static Screen create(Screen instance) {
        return new WynntilsScreenWrapper(instance.getTitle(), instance);
    }

    public Screen getDelegate() {
        return delegate;
    }

    private void failure(String method, Throwable e) {
        WynntilsMod.error("Failure in " + delegate.getClass().getSimpleName() + "." + method + "()", e);
        McUtils.sendMessageToClient(new TextComponent("Wynntils: Failure in " + method + " in "
                + delegate.getClass().getSimpleName() + ". Screen forcefully closed."));
        McUtils.mc().setScreen(null);
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        try {
            delegate.init(minecraft, width, height);
        } catch (Throwable t) {
            failure("init", t);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        try {
            delegate.render(poseStack, mouseX, mouseY, partialTick);
        } catch (Throwable t) {
            failure("render", t);
        }
    }

    @Override
    public Component getTitle() {
        return delegate.getTitle();
    }

    @Override
    public Component getNarrationMessage() {
        return delegate.getNarrationMessage();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return delegate.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return delegate.shouldCloseOnEsc();
    }

    @Override
    public void onClose() {
        delegate.onClose();
    }

    @Override
    public <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T widget) {
        return delegate.addRenderableWidget(widget);
    }

    @Override
    public void clearWidgets() {
        delegate.clearWidgets();
    }

    @Override
    public void renderTooltip(PoseStack poseStack, ItemStack itemStack, int mouseX, int mouseY) {
        delegate.renderTooltip(poseStack, itemStack, mouseX, mouseY);
    }

    @Override
    public void renderTooltip(
            PoseStack poseStack,
            List<Component> tooltips,
            Optional<TooltipComponent> visualTooltipComponent,
            int mouseX,
            int mouseY) {
        delegate.renderTooltip(poseStack, tooltips, visualTooltipComponent, mouseX, mouseY);
    }

    @Override
    public List<Component> getTooltipFromItem(ItemStack itemStack) {
        return delegate.getTooltipFromItem(itemStack);
    }

    @Override
    public void renderTooltip(PoseStack poseStack, Component text, int mouseX, int mouseY) {
        delegate.renderTooltip(poseStack, text, mouseX, mouseY);
    }

    @Override
    public void renderComponentTooltip(PoseStack poseStack, List<Component> tooltips, int mouseX, int mouseY) {
        delegate.renderComponentTooltip(poseStack, tooltips, mouseX, mouseY);
    }

    @Override
    public void renderTooltip(
            PoseStack poseStack, List<? extends FormattedCharSequence> tooltips, int mouseX, int mouseY) {
        delegate.renderTooltip(poseStack, tooltips, mouseX, mouseY);
    }

    @Override
    public boolean handleComponentClicked(Style style) {
        return delegate.handleComponentClicked(style);
    }

    @Override
    public void sendMessage(String text) {
        delegate.sendMessage(text);
    }

    @Override
    public void sendMessage(String text, boolean addToChat) {
        delegate.sendMessage(text, addToChat);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return delegate.children();
    }

    @Override
    public void tick() {
        delegate.tick();
    }

    @Override
    public void removed() {
        delegate.removed();
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        delegate.renderBackground(poseStack);
    }

    @Override
    public void renderBackground(PoseStack poseStack, int vOffset) {
        delegate.renderBackground(poseStack, vOffset);
    }

    @Override
    public void renderDirtBackground(int vOffset) {
        delegate.renderDirtBackground(vOffset);
    }

    @Override
    public boolean isPauseScreen() {
        return delegate.isPauseScreen();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        delegate.resize(minecraft, width, height);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return delegate.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void onFilesDrop(List<Path> packs) {
        delegate.onFilesDrop(packs);
    }

    @Override
    public void afterMouseMove() {
        delegate.afterMouseMove();
    }

    @Override
    public void afterMouseAction() {
        delegate.afterMouseAction();
    }

    @Override
    public void afterKeyboardAction() {
        delegate.afterKeyboardAction();
    }

    @Override
    public void handleDelayedNarration() {
        delegate.handleDelayedNarration();
    }

    @Override
    public void narrationEnabled() {
        delegate.narrationEnabled();
    }

    @Override
    public GuiEventListener getFocused() {
        return delegate.getFocused();
    }

    @Override
    public void setFocused(GuiEventListener focused) {
        delegate.setFocused(focused);
    }

    @Override
    public void blitOutlineBlack(int width, int height, BiConsumer<Integer, Integer> boxXYConsumer) {
        delegate.blitOutlineBlack(width, height, boxXYConsumer);
    }

    @Override
    public void blit(PoseStack poseStack, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
        delegate.blit(poseStack, x, y, uOffset, vOffset, uWidth, vHeight);
    }

    @Override
    public int getBlitOffset() {
        return delegate.getBlitOffset();
    }

    @Override
    public void setBlitOffset(int blitOffset) {
        delegate.setBlitOffset(blitOffset);
    }

    @Override
    public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        return delegate.getChildAt(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return delegate.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return delegate.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return delegate.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return delegate.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return delegate.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return delegate.charTyped(codePoint, modifiers);
    }

    @Override
    public void setInitialFocus(GuiEventListener eventListener) {
        delegate.setInitialFocus(eventListener);
    }

    @Override
    public void magicalSpecialHackyFocus(GuiEventListener eventListener) {
        delegate.magicalSpecialHackyFocus(eventListener);
    }

    @Override
    public boolean changeFocus(boolean focus) {
        return delegate.changeFocus(focus);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        delegate.mouseMoved(mouseX, mouseY);
    }
}
