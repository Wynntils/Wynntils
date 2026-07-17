package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.MenuCategory;
import com.wynntils.screens.buildloadouts.type.ScrollListCategory;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.services.loadout.type.LoadoutType;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;

public class LoadoutMenuScrollListWidget extends ScrollListWidget {
    private static final int  MAX_WIDGETS_PER_PAGE = 4;
    private static final int WIDTH = 271 - 10;
    private static final int HEIGHT = 136;
    private static final int WIDGET_HEIGHT = 30;
    private static final int WIDGET_HEIGHT_PADDING = 2;
    private static final int WIDGET_HEIGHT_EDGE_PADDING = 5;
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;
    private List<AbstractWidget> itemWidgets = new ArrayList<>();

    public LoadoutMenuScrollListWidget(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, WIDTH, HEIGHT, WIDGET_HEIGHT, WIDGET_HEIGHT_PADDING, WIDGET_HEIGHT_EDGE_PADDING,  MAX_WIDGETS_PER_PAGE);
        this.x = x;
        this.y = y;
        this.parent = parent;
    }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND,
                x - 5,
                y - 5,
                this.width + 10,
                this.height + 10);



        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return super.mouseClicked(event, isDoubleClick);
    }


    @Override
    protected List<AbstractWidget> getWidgets() {
        return itemWidgets;
    }

    public void populateWidgets() {
        itemWidgets = new ArrayList<>();
        scrollOffset = 0;

        Loadout selectedLoadout = parent.getSelectedLoadout();
        if (selectedLoadout == null) return;

        if (selectedLoadout.type() == LoadoutType.ABILITY_TREE) {
            for (String abilityName : selectedLoadout.abilityTree().abilities()) {
                itemWidgets.add(new BuildLoadoutScrollListAbilityWidget(
                        StyledText.fromString(abilityName),
                        this.x + WIDGET_HEIGHT_EDGE_PADDING,
                        this.y + WIDGET_HEIGHT_EDGE_PADDING + itemWidgets.size() * (WIDGET_HEIGHT + WIDGET_HEIGHT_PADDING),
                        this.width - 20,
                        WIDGET_HEIGHT,
                        parent));
            }
        }

        if (selectedLoadout.type() == LoadoutType.ASPECT) {
            for (String abilityName : selectedLoadout.aspects().aspectNames()) {
                itemWidgets.add(new BuildLoadoutScrollListAspectWidget(
                        StyledText.fromString(abilityName),
                        this.x + WIDGET_HEIGHT_EDGE_PADDING,
                        this.y + WIDGET_HEIGHT_EDGE_PADDING + itemWidgets.size() * (WIDGET_HEIGHT + WIDGET_HEIGHT_PADDING),
                        this.width - 20,
                        WIDGET_HEIGHT,
                        parent));
            }
        }

    }

}
