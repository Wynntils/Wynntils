package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeType;
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

public class LoadoutMenuScrollListAbilityWidget extends AbstractWidget {
    private final StyledText text;
    private int x;
    private int y;
    private final BuildLoadoutsScreen parent;
    private final ItemStack abilityItemStack;
    private final boolean ultimateAbility;

    public LoadoutMenuScrollListAbilityWidget(StyledText text, int x, int y, int width, int height, BuildLoadoutsScreen parent) {
        super(x, y, width, height, Component.literal("Build Loadout Scroll List Ability Widget"));
        this.text = text;
        this.x = x;
        this.y = y;
        this.parent = parent;

        AbilityTreeNodeType abilityTreeNodeType = Models.AbilityTree.getNodeFromNameAndClass(this.text.getString(), parent.getSelectedLoadout().getClassType()).abilityTreeNodeType().getUnlockedType();
        abilityItemStack = abilityTreeNodeType.generateItemStack();
        ultimateAbility = abilityTreeNodeType.isUltimate();
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
                .renderText(
                        guiGraphics,
                        this.text,
                        (this.x + this.width / 2f),
                        (this.y + this.height / 2f),
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (abilityItemStack != null) {
            if (!ultimateAbility) {
                RenderUtils.renderItem(
                        guiGraphics,
                        abilityItemStack,
                        this.x + 10,
                        this.y + this.height / 2 - 8
                );
            } else {
                RenderUtils.renderScalingItem(
                        guiGraphics,
                        abilityItemStack,
                        this.x + 13,
                        this.y + this.height / 2 - 6,
                        32,
                        32
                );
            }
        }

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
