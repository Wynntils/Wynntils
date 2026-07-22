package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.MenuCategory;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class LoadoutSelectionButton extends AbstractWidget implements TooltipProvider, IconRenderer {
    private final StyledText text;
    private final int x;
    private final int y;
    private MenuCategory menuCategory;
    private final BuildLoadoutsScreen parent;

    public LoadoutSelectionButton(StyledText text, MenuCategory menuCategory, int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 133 - 10, 31, Component.literal("Loadout Selection Button"));
        this.text = text;
        this.menuCategory = menuCategory;
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);
        if (parent.getCurrentCategory() != menuCategory) {
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

        if (menuCategory == MenuCategory.BUILD_LOADOUT) {
            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_BUILD_LOADOUTS_ICON,
                    this.x + 30 - (Texture.BUILD_LOADOUTS_BUILD_LOADOUTS_ICON.width() / 2f),
                    (this.y + this.height / 2f) - Texture.BUILD_LOADOUTS_BUILD_LOADOUTS_ICON.height() / 2f);
        } else if (menuCategory == MenuCategory.ABILITY_TREE_LOADOUT) {
            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_ABILITY_TREE_LOADOUTS_ICON,
                    this.x + 28 - (Texture.BUILD_LOADOUTS_ABILITY_TREE_LOADOUTS_ICON.width() / 2f),
                    (this.y + this.height / 2f) - Texture.BUILD_LOADOUTS_ABILITY_TREE_LOADOUTS_ICON.height() / 2f);
        } else if (menuCategory == MenuCategory.SKILL_POINT_LOADOUT) {
            float baseY = this.y + this.height / 2f - 6;
            renderSkillIcons(guiGraphics, this.x + 10, baseY, new int[]{0, 1, 2, 3, 4});
        } else if (menuCategory == MenuCategory.ASPECT_LOADOUT) {
            renderAspect(
                    guiGraphics,
                    Texture.ASPECT_ARCHER,
                    Texture.ASPECT_ARCHER_FLAME,
                    this.x,
                    this.y);

            renderAspect(
                    guiGraphics,
                    Texture.ASPECT_ASSASSIN,
                    Texture.ASPECT_ASSASSIN_FLAME,
                    this.x + 18,
                    this.y);

            renderAspect(
                    guiGraphics,
                    Texture.ASPECT_SHAMAN,
                    Texture.ASPECT_SHAMAN_FLAME,
                    this.x + 36,
                    this.y);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());

        parent.setCurrentCategory(menuCategory);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return List.of();
    }
}
