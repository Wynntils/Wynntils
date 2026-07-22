package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
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
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class LoadoutMenuScrollListTomeWidget extends AbstractWidget implements ItemTooltipProvider {
    private final StyledText text;
    private final ItemStack tomeStack;
    private int x;
    private int y;
    private final BuildLoadoutsScreen parent;

    public LoadoutMenuScrollListTomeWidget(StyledText text, ItemStack tomeStack, int x, int y, int width, int height, BuildLoadoutsScreen parent) {
        super(x, y, width, height, Component.literal("Build Loadout Scroll List Tome Widget"));
        this.text = text;
        this.tomeStack = tomeStack;
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                this.x,
                this.y,
                this.width,
                this.height);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        this.text,
                        this.x + this.width / 2f + 13,
                        this.y,
                        this.y + this.height,
                        this.width - 40,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        RenderUtils.renderItem(
                guiGraphics,
                tomeStack,
                this.x + 10,
                this.y + this.height / 2 - 8
        );
    }

    @Override
    public void renderHoveredItemTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        handleCursor(guiGraphics);
        RenderUtils.renderTooltip(guiGraphics, tomeStack, mouseX, mouseY);
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }


    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
