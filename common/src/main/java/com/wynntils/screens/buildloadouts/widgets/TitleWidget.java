package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class TitleWidget extends AbstractWidget {
    private final StyledText text;
    private final int x;
    private final int y;

    public TitleWidget(StyledText text, int x, int y) {
        super(x, y, 133 - 10, 20, Component.literal("Title Widget"));
        this.text = text;
        this.x = x;
        this.y = y;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_GEM,
                this.x,
                (this.y + this.height / 2f) - (Texture.BUILD_LOADOUTS_GEM.height() / 2f));

        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_GEM,
                this.x + this.width - Texture.BUILD_LOADOUTS_GEM.width(),
                (this.y + this.height / 2f) - (Texture.BUILD_LOADOUTS_GEM.height() / 2f));

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        this.text,
                        this.x + this.width / 2f,
                        this.y + this.height / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
