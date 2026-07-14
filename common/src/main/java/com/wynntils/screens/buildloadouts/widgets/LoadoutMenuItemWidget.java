package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class LoadoutMenuItemWidget extends AbstractWidget {
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;


    public LoadoutMenuItemWidget(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 98, 68, Component.literal("Loadout Menu Item Widget"));
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                x,
                y,
                this.width,
                this.height);

        // Bottom row: 4 boxes
        for (int i = 0; i < 4; i++) {
            int bx = x + 3 + 19 + 19 * i;
            int by = y + this.height - 16 - 7;

            /*
            RenderUtils.drawRect(
                    guiGraphics,
                    CommonColors.BLACK,
                    bx,
                    by,
                    16,
                    16);

             */

            RenderUtils.drawRectBorders(
                    guiGraphics,
                    CustomColor.fromInt(0x654f3c),
                    bx, by,
                    bx + 16, by + 16,
                    1);
        }

        // Top row: 5 boxes
        for (int i = 0; i < 5; i++) {
            int bx = x + 3 + 19 * i;
            int by = y + this.height - 16 * 2 - 10;

            /*
            RenderUtils.drawRect(
                    guiGraphics,
                    CommonColors.BLACK,
                    bx,
                    by,
                    16,
                    16);

             */

            RenderUtils.drawRectBorders(
                    guiGraphics,
                    CustomColor.fromInt(0x654f3c),
                    bx, by,
                    bx + 16, by + 16,
                    1);
        }

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_MENU_RIBBON,
                x + 5,
                y + 2,
                this.width - 10,
                20);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString("Items"),
                        this.x + 5 + (this.width - 10) / 2f,
                        this.y + 11,
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
