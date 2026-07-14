package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import com.wynntils.models.aspects.type.SavableAspectSet;
import com.wynntils.models.character.type.SavableSkillPointSet;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.Loadout;
import com.wynntils.screens.buildloadouts.type.LoadoutType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class LoadoutMenuScrollListWidget extends ScrollListWidget {
    private static final int WIDTH = 271 - 10;
    private static final int HEIGHT = 131 - 17;
    private static final int BUTTON_OFFSET = 15;
    private static final int WIDGET_HEIGHT = 32;
    private static final int WIDGET_HEIGHT_PADDING = 2;
    private static final int WIDGET_HEIGHT_EDGE_PADDING = 5;
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;

    public LoadoutMenuScrollListWidget(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y + BUTTON_OFFSET, WIDTH, HEIGHT, WIDGET_HEIGHT, WIDGET_HEIGHT_PADDING, WIDGET_HEIGHT_EDGE_PADDING);
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected List<AbstractWidget> getWidgets() {
        return List.of();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND,
                x - 5,
                y - 5,
                this.width + 10,
                this.height + 10 + BUTTON_OFFSET + 1);

        int buttonWidth = (this.width - 8) / 4;
        for (int i = 0; i < 4; i++) {
            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_SCROLL_LIST_TOP_BUTTON,
                    x + 4 + i * buttonWidth,
                    y - 4,
                    buttonWidth + 1,
                    20);
        }

        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }


}
