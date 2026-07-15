package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeType;
import com.wynntils.models.aspects.type.AnimatedAspectType;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.utils.colors.CommonColors;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class BuildLoadoutScrollListAspectWidget extends AbstractWidget implements IconRenderer {
    private static final int TEXT_WIDTH_PADDING = 4;
    private static final int TEXT_HEIGHT_PADDING = 4;
    private static final int MAX_VISIBLE_CHARACTERS = 38;
    private final StyledText text;
    private int x;
    private int y;
    private final BuildLoadoutsScreen parent;
    private final ItemStack aspectItemStack;

    public BuildLoadoutScrollListAspectWidget(StyledText text, int x, int y, int width, int height, BuildLoadoutsScreen parent) {
        super(x, y, width, height, Component.literal("Build Loadout Scroll List Aspect Widget"));
        this.text = text;
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.aspectItemStack = AnimatedAspectType.fromClassType(parent.getSelectedLoadout().getClassType()).generateItemStack();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                this.x,
                this.y,
                this.width,
                this.height);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        this.text,
                        (this.x + this.width / 2f),
                        (this.y + this.height / 2f),
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (aspectItemStack != null) {
            RenderUtils.renderScalingItem(
                    guiGraphics,
                    aspectItemStack,
                    this.x + 10,
                    this.y + this.height / 2 ,
                    44,
                    44
            );
        }

    }

    private StyledText getTruncatedText(StyledText text, int maxVisibleChars) {
        int visibleLength = text.length();
        if (visibleLength <= maxVisibleChars) {
            return text;
        }

        return text.substring(0, maxVisibleChars - 3, StyleType.NONE).append("...");
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }


    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
