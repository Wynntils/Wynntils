package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.Loadout;
import com.wynntils.screens.buildloadouts.type.LoadoutType;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.stream.IntStream;

public class LoadoutWidget extends AbstractWidget implements IconRenderer {
    private static final int TEXT_WIDTH_PADDING = 4;
    private static final int TEXT_HEIGHT_PADDING = 4;
    private static final int MAX_VISIBLE_CHARACTERS = 38;
    private final StyledText text;
    private int x;
    private int y;
    private Loadout loadout;
    private BuildLoadoutsScreen parent;
    private final ItemStack archetypeItemStack;
    private final Texture aspectTexture;
    private final Texture aspectFlameTexture;

    public LoadoutWidget(StyledText text, int x, int y, int width, int height, Loadout loadout, BuildLoadoutsScreen parent) {
        super(x, y, width, height, Component.literal("Loadout Widget"));
        this.text = text;
        this.x = x;
        this.y = y;
        this.loadout = loadout;
        this.parent = parent;
        this.archetypeItemStack = loadout.getArchetypeType() != null ? loadout.getArchetypeType().generateItemStack() : null;
        this.aspectTexture = loadout.getAspectTexture();
        this.aspectFlameTexture = loadout.getFlameTexture();
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

        if (loadout.type() != LoadoutType.SKILL_POINT) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            getTruncatedText(this.text, MAX_VISIBLE_CHARACTERS),
                            this.x + TEXT_WIDTH_PADDING + 20 + ((this.width - TEXT_WIDTH_PADDING * 2 - 20) / 2f),
                            this.y + TEXT_HEIGHT_PADDING,
                            this.y + this.height - TEXT_HEIGHT_PADDING,
                            this.width - TEXT_WIDTH_PADDING * 2 - 20,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

        if (archetypeItemStack != null) {
            RenderUtils.renderItem(
                    guiGraphics,
                    archetypeItemStack,
                    this.x + 5,
                    this.y + this.height / 2 - 8
            );
        } else if (aspectTexture != null && aspectFlameTexture != null) {
            renderAspect(guiGraphics, aspectTexture, aspectFlameTexture, this.x, this.y);
        } else if (loadout.type() == LoadoutType.SKILL_POINT) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            getTruncatedText(this.text, MAX_VISIBLE_CHARACTERS - 6),
                            this.x + TEXT_WIDTH_PADDING + 36 + ((this.width - TEXT_WIDTH_PADDING * 2 - 36) / 2f),
                            this.y + TEXT_HEIGHT_PADDING,
                            this.y + this.height - TEXT_HEIGHT_PADDING,
                            this.width - TEXT_WIDTH_PADDING * 2 - 36,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            int[] points = loadout.skillPoints().getSkillPointsAsArray();
            int[] activeElements = IntStream.range(0, 5).filter(i -> points[i] > 0).toArray();

            float baseY = this.y + this.height / 2f - 6;
            renderSkillIcons(guiGraphics, this.x, baseY, activeElements);

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
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
