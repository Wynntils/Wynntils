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
        try {
            return delegate.getTitle();
        } catch (Throwable t) {
            failure("getTitle", t);
            return null;
        }
    }

    @Override
    public Component getNarrationMessage() {
        try {
            return delegate.getNarrationMessage();
        } catch (Throwable t) {
            failure("getNarrationMessage", t);
            return null;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        try {
            return delegate.keyPressed(keyCode, scanCode, modifiers);
        } catch (Throwable t) {
            failure("keyPressed", t);
            return false;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        try {
            return delegate.shouldCloseOnEsc();
        } catch (Throwable t) {
            failure("shouldCloseOnEsc", t);
            return false;
        }
    }

    @Override
    public void onClose() {
        try {
            delegate.onClose();
        } catch (Throwable t) {
            failure("onClose", t);
        }
    }

    @Override
    public <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T widget) {
        try {
            return delegate.addRenderableWidget(widget);
        } catch (Throwable t) {
            failure("addRenderableWidget", t);
            return null;
        }
    }

    @Override
    public void clearWidgets() {
        try {
            delegate.clearWidgets();
        } catch (Throwable t) {
            failure("clearWidgets", t);
        }
    }

    @Override
    public void renderTooltip(PoseStack poseStack, ItemStack itemStack, int mouseX, int mouseY) {
        try {
            delegate.renderTooltip(poseStack, itemStack, mouseX, mouseY);
        } catch (Throwable t) {
            failure("renderTooltip", t);
        }
    }

    @Override
    public void renderTooltip(
            PoseStack poseStack,
            List<Component> tooltips,
            Optional<TooltipComponent> visualTooltipComponent,
            int mouseX,
            int mouseY) {
        try {
            delegate.renderTooltip(poseStack, tooltips, visualTooltipComponent, mouseX, mouseY);
        } catch (Throwable t) {
            failure("renderTooltip", t);
        }
    }

    @Override
    public List<Component> getTooltipFromItem(ItemStack itemStack) {
        try {
            return delegate.getTooltipFromItem(itemStack);
        } catch (Throwable t) {
            failure("getTooltipFromItem", t);
            return null;
        }
    }

    @Override
    public void renderTooltip(PoseStack poseStack, Component text, int mouseX, int mouseY) {
        try {
            delegate.renderTooltip(poseStack, text, mouseX, mouseY);
        } catch (Throwable t) {
            failure("renderTooltip", t);
        }
    }

    @Override
    public void renderComponentTooltip(PoseStack poseStack, List<Component> tooltips, int mouseX, int mouseY) {
        try {
            delegate.renderComponentTooltip(poseStack, tooltips, mouseX, mouseY);
        } catch (Throwable t) {
            failure("renderComponentTooltip", t);
        }
    }

    @Override
    public void renderTooltip(
            PoseStack poseStack, List<? extends FormattedCharSequence> tooltips, int mouseX, int mouseY) {
        try {
            delegate.renderTooltip(poseStack, tooltips, mouseX, mouseY);
        } catch (Throwable t) {
            failure("renderTooltip", t);
        }
    }

    @Override
    public boolean handleComponentClicked(Style style) {
        try {
            return delegate.handleComponentClicked(style);
        } catch (Throwable t) {
            failure("handleComponentClicked", t);
            return false;
        }
    }

    @Override
    public void sendMessage(String text) {
        try {
            delegate.sendMessage(text);
        } catch (Throwable t) {
            failure("sendMessage", t);
        }
    }

    @Override
    public void sendMessage(String text, boolean addToChat) {
        try {
            delegate.sendMessage(text, addToChat);
        } catch (Throwable t) {
            failure("sendMessage", t);
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        try {
            return delegate.children();
        } catch (Throwable t) {
            failure("children", t);
            return null;
        }
    }

    @Override
    public void tick() {
        try {
            delegate.tick();
        } catch (Throwable t) {
            failure("tick", t);
        }
    }

    @Override
    public void removed() {
        try {
            delegate.removed();
        } catch (Throwable t) {
            failure("removed", t);
        }
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        try {
            delegate.renderBackground(poseStack);
        } catch (Throwable t) {
            failure("renderBackground", t);
        }
    }

    @Override
    public void renderBackground(PoseStack poseStack, int vOffset) {
        try {
            delegate.renderBackground(poseStack, vOffset);
        } catch (Throwable t) {
            failure("renderBackground", t);
        }
    }

    @Override
    public void renderDirtBackground(int vOffset) {
        try {
            delegate.renderDirtBackground(vOffset);
        } catch (Throwable t) {
            failure("renderDirtBackground", t);
        }
    }

    @Override
    public boolean isPauseScreen() {
        try {
            return delegate.isPauseScreen();
        } catch (Throwable t) {
            failure("isPauseScreen", t);
            return false;
        }
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        try {
            delegate.resize(minecraft, width, height);
        } catch (Throwable t) {
            failure("resize", t);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        try {
            return delegate.isMouseOver(mouseX, mouseY);
        } catch (Throwable t) {
            failure("isMouseOver", t);
            return false;
        }
    }

    @Override
    public void onFilesDrop(List<Path> packs) {
        try {
            delegate.onFilesDrop(packs);
        } catch (Throwable t) {
            failure("onFilesDrop", t);
        }
    }

    @Override
    public void afterMouseMove() {
        try {
            delegate.afterMouseMove();
        } catch (Throwable t) {
            failure("afterMouseMove", t);
        }
    }

    @Override
    public void afterMouseAction() {
        try {
            delegate.afterMouseAction();
        } catch (Throwable t) {
            failure("afterMouseAction", t);
        }
    }

    @Override
    public void afterKeyboardAction() {
        try {
            delegate.afterKeyboardAction();
        } catch (Throwable t) {
            failure("afterKeyboardAction", t);
        }
    }

    @Override
    public void handleDelayedNarration() {
        try {
            delegate.handleDelayedNarration();
        } catch (Throwable t) {
            failure("handleDelayedNarration", t);
        }
    }

    @Override
    public void narrationEnabled() {
        try {
            delegate.narrationEnabled();
        } catch (Throwable t) {
            failure("narrationEnabled", t);
        }
    }

    @Override
    public GuiEventListener getFocused() {
        try {
            return delegate.getFocused();
        } catch (Throwable t) {
            failure("getFocused", t);
            return null;
        }
    }

    @Override
    public void setFocused(GuiEventListener focused) {
        try {
            delegate.setFocused(focused);
        } catch (Throwable t) {
            failure("setFocused", t);
        }
    }

    @Override
    public void blitOutlineBlack(int width, int height, BiConsumer<Integer, Integer> boxXYConsumer) {
        try {
            delegate.blitOutlineBlack(width, height, boxXYConsumer);
        } catch (Throwable t) {
            failure("blitOutlineBlack", t);
        }
    }

    @Override
    public void blit(PoseStack poseStack, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
        try {
            delegate.blit(poseStack, x, y, uOffset, vOffset, uWidth, vHeight);
        } catch (Throwable t) {
            failure("blit", t);
        }
    }

    @Override
    public int getBlitOffset() {
        try {
            return delegate.getBlitOffset();
        } catch (Throwable t) {
            failure("getBlitOffset", t);
            return 0;
        }
    }

    @Override
    public void setBlitOffset(int blitOffset) {
        try {
            delegate.setBlitOffset(blitOffset);
        } catch (Throwable t) {
            failure("setBlitOffset", t);
        }
    }

    @Override
    public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        try {
            return delegate.getChildAt(mouseX, mouseY);
        } catch (Throwable t) {
            failure("getChildAt", t);
            return null;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        try {
            return delegate.mouseClicked(mouseX, mouseY, button);
        } catch (Throwable t) {
            failure("mouseClicked", t);
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        try {
            return delegate.mouseReleased(mouseX, mouseY, button);
        } catch (Throwable t) {
            failure("mouseReleased", t);
            return false;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        try {
            return delegate.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        } catch (Throwable t) {
            failure("mouseDragged", t);
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        try {
            return delegate.mouseScrolled(mouseX, mouseY, delta);
        } catch (Throwable t) {
            failure("mouseScrolled", t);
            return false;
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        try {
            return delegate.keyReleased(keyCode, scanCode, modifiers);
        } catch (Throwable t) {
            failure("keyReleased", t);
            return false;
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        try {
            return delegate.charTyped(codePoint, modifiers);
        } catch (Throwable t) {
            failure("charTyped", t);
            return false;
        }
    }

    @Override
    public void setInitialFocus(GuiEventListener eventListener) {
        try {
            delegate.setInitialFocus(eventListener);
        } catch (Throwable t) {
            failure("setInitialFocus", t);
        }
    }

    @Override
    public void magicalSpecialHackyFocus(GuiEventListener eventListener) {
        try {
            delegate.magicalSpecialHackyFocus(eventListener);
        } catch (Throwable t) {
            failure("magicalSpecialHackyFocus", t);
        }
    }

    @Override
    public boolean changeFocus(boolean focus) {
        try {
            return delegate.changeFocus(focus);
        } catch (Throwable t) {
            failure("changeFocus", t);
            return false;
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        try {
            delegate.mouseMoved(mouseX, mouseY);
        } catch (Throwable t) {
            failure("mouseMoved", t);
        }
    }
}
