package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Services;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class LoadoutMenuFavouriteButton extends AbstractWidget {
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;

    public LoadoutMenuFavouriteButton(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 20, 20, Component.literal("Loadout Menu Favorite Button"));
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND,
                x - 5,
                y - 5,
                this.width + 10,
                this.height + 10);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                x,
                y,
                this.width,
                this.height);

        Loadout loadout = parent.getSelectedLoadout();
        if (loadout != null) {
            if (loadout.favourited()) {
                RenderUtils.drawTexturedRect(
                        guiGraphics,
                        Texture.BUILD_LOADOUTS_STAR_ICON,
                        x + 4,
                        y + 4
                );
            } else {
                RenderUtils.drawTexturedRect(
                        guiGraphics,
                        Texture.BUILD_LOADOUTS_STAR_ICON_OUTLINE,
                        x + 4,
                        y + 4
                );
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) return false;
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());

        Loadout selected = parent.getSelectedLoadout();
        Services.loadout.setFavourited(selected.name(), !selected.favourited());
        parent.setSelectedLoadout(Services.loadout.getLoadout(selected.name()));
        parent.loadoutScrollListWidget.scrollOffset = 0;
        parent.loadoutScrollListWidget.populateLoadouts();

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
