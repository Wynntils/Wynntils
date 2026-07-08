package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
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

public class NewLoadoutInfoWidget extends AbstractWidget {
    private StyledText text;
    private final int x;
    private final int y;

    public NewLoadoutInfoWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("New Loadout Widget"));
        this.x = x;
        this.y = y;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (text == null || text.isEmpty()) return;

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_INFO_WIDGET_BOX,
                this.x,
                this.y,
                this.width,
                this.height);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        this.text,
                        this.x + 10 + Texture.BUILD_LOADOUTS_INFO_ICON.width() + 5,
                        this.y + 8,
                        this.y + this.height - 8,
                        this.width - Texture.BUILD_LOADOUTS_INFO_ICON.width() - 20,
                        CustomColor.fromInt(0x191915),
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE);

        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_INFO_ICON,
                x + 10,
                (this.y + this.height / 2f) - Texture.BUILD_LOADOUTS_INFO_ICON.height() / 2f);
    }

    public void setText(StyledText text) {
        this.text = text;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
