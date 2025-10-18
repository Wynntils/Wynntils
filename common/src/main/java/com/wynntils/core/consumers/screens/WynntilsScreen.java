/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.screens;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.mod.type.CrashType;
import com.wynntils.handlers.wrappedscreen.WrappedScreen;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.CrashReport;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class WynntilsScreen extends Screen implements TextboxScreen {
    private TextInputBoxWidget focusedTextInput;

    protected WynntilsScreen(Component component) {
        super(component);
    }

    private void failure(String method, Throwable throwable) {
        McUtils.setScreen(null);

        WynntilsMod.reportCrash(
                CrashType.SCREEN,
                this.getClass().getSimpleName(),
                this.getClass().getName(),
                method,
                true,
                false,
                throwable);
        McUtils.sendErrorToClient("Screen was forcefully closed.");
    }

    @Override
    public final void init() {
        try {
            doInit();
        } catch (Throwable t) {
            failure("init", t);
        }
    }

    protected void doInit() {
        super.init();
    }

    @Override
    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        try {
            doRender(guiGraphics, mouseX, mouseY, partialTick);
        } catch (Throwable t) {
            failure("render", t);
        }
    }

    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // renderMenuBackground causes issues with our texture rendering so until that is fixed we can just overwrite the
    // call
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (McUtils.mc().level == null) {
            renderPanorama(guiGraphics, partialTick);
        }

        renderBlurredBackground();
    }

    @Override
    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        try {
            return doMouseClicked(mouseX, mouseY, button);
        } catch (Throwable t) {
            failure("mouseClicked", t);
        }

        return false;
    }

    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void wrapCurrentScreenError(CrashReport crashReport) {
        failure(crashReport.getDetails(), crashReport.getException());
    }

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return focusedTextInput;
    }

    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {
        this.focusedTextInput = focusedTextInput;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return (getFocusedTextInput() != null && getFocusedTextInput().charTyped(codePoint, modifiers))
                || super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // When tab is pressed, focus the next text box
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            int index = getFocusedTextInput() == null ? 0 : children().indexOf(getFocusedTextInput());
            int actualIndex = Math.max(index, 0) + 1;

            // Try to find next text input
            // From index - end
            for (int i = actualIndex; i < children().size(); i++) {
                if (children().get(i) instanceof TextInputBoxWidget textInputBoxWidget) {
                    setFocusedTextInput(textInputBoxWidget);
                    return true;
                }
            }

            // From 0 - index
            for (int i = 0; i < Math.min(actualIndex, children().size()); i++) {
                if (children().get(i) instanceof TextInputBoxWidget textInputBoxWidget) {
                    setFocusedTextInput(textInputBoxWidget);
                    return true;
                }
            }
        }

        if (getFocusedTextInput() != null) {
            return getFocusedTextInput().keyPressed(keyCode, scanCode, modifiers);
        }

        if (this instanceof WrappedScreen && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
