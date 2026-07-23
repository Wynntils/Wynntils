package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.MenuCategory;
import com.wynntils.services.loadout.type.LoadoutType;
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
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class NewLoadoutSelectionButton extends AbstractWidget implements IconRenderer {
    private final StyledText text;
    private final StyledText infoText;
    private final int x;
    private final int y;
    private final LoadoutType loadoutType;
    private final BuildLoadoutsScreen parent;

    private static final int LIGHT_HOLDER_WIDTH_OFFSET = 5;
    private static final int LIGHT_HOLDER_HEIGHT_OFFSET = 5;

    public NewLoadoutSelectionButton(StyledText text, StyledText infoText, LoadoutType loadoutType, int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 128, 41, Component.literal("New Loadout Selection Button"));
        this.text = text;
        this.infoText = infoText;
        this.loadoutType = loadoutType;
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);
        if (parent.getNewLoadoutType() != loadoutType) {
            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                    x,
                    y,
                    this.width,
                    this.height);
        } else {
            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_BLUE,
                    x,
                    y,
                    this.width,
                    this.height);

            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_WIDGET_SELECT_TAB,
                    x + this.width - Texture.BUILD_LOADOUTS_WIDGET_SELECT_TAB.width() / 2f,
                    (this.y + this.height / 2f) - Texture.BUILD_LOADOUTS_WIDGET_SELECT_TAB.height() / 2f);
        }

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        this.text,
                        (this.x + this.width / 2f) + 24,
                        (this.y + this.height / 2f) - 4,
                        70,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (loadoutType == LoadoutType.BUILD) {
            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_BUILD_LOADOUTS_ICON,
                    this.x + 32 - (Texture.BUILD_LOADOUTS_BUILD_LOADOUTS_ICON.width() / 2f),
                    (this.y + this.height / 2f) - Texture.BUILD_LOADOUTS_BUILD_LOADOUTS_ICON.height() / 2f);
        } else if (loadoutType == LoadoutType.ABILITY_TREE) {
            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_ABILITY_TREE_LOADOUTS_ICON,
                    this.x + 28 - (Texture.BUILD_LOADOUTS_ABILITY_TREE_LOADOUTS_ICON.width() / 2f),
                    (this.y + this.height / 2f) - Texture.BUILD_LOADOUTS_ABILITY_TREE_LOADOUTS_ICON.height() / 2f);
        } else if (loadoutType == LoadoutType.SKILL_POINT) {
            float baseY = this.y + this.height / 2f - 6;
            renderSkillIcons(guiGraphics, this.x + 12, baseY, new int[]{0, 1, 2, 3, 4});
        } else if (loadoutType == LoadoutType.ASPECT) {
            renderAspect(
                    guiGraphics,
                    Texture.ASPECT_ARCHER,
                    Texture.ASPECT_ARCHER_FLAME,
                    this.x + 5,
                    this.y + 5);

            renderAspect(
                    guiGraphics,
                    Texture.ASPECT_ASSASSIN,
                    Texture.ASPECT_ASSASSIN_FLAME,
                    this.x + 25,
                    this.y + 5);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());

        parent.setNewLoadoutType(loadoutType);
        parent.newLoadoutInfoWidget.setText(this.infoText, true);
        parent.makeNewLoadoutButton.setButtonConfirm(false);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
