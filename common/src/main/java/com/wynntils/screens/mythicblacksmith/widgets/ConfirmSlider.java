package com.wynntils.screens.mythicblacksmith.widgets;

import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ConfirmSlider extends AbstractWidget {
    private final int originalX;
    private final int maxX;

    private int mouseAttachOffset = 0;
    public boolean isSliding = false;
    public float sliderValue = 0.0F;

    public ConfirmSlider(int x, int y, int width, int height, int widgetWidth) {
        super(x, y, width, height, Component.literal("Confirm Slider"));
        this.originalX = x;
        this.maxX = x + widgetWidth - width - 2;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(guiGraphics.pose(),
                this.isMouseOver(mouseX, mouseY) ? CommonColors.GRAY : CommonColors.LIGHT_GRAY,
                getX(),
                getY(),
                1,
                getWidth(),
                getHeight());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.isSliding = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!this.isSliding) return false;

        this.setX(Math.clamp((int) mouseX, originalX, maxX));
        sliderValue = (float) (getX() - originalX) / (maxX - originalX);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isSliding = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public void decaySlider() {
        if (this.getX() > this.originalX) {
            this.setX(this.getX() - (this.getX() - 2 < this.originalX ? this.getX() - this.originalX : 2));
            sliderValue = (float) (getX() - originalX) / (maxX - originalX);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
