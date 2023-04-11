/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.abilities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.models.abilities.type.AbilityTreeInfo;
import com.wynntils.models.abilities.type.AbilityTreeLocation;
import com.wynntils.screens.abilities.widgets.AbilityNodeWidget;
import com.wynntils.screens.base.WynntilsScreen;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

public class CustomAbilityTreeScreen extends WynntilsScreen {
    private static final int NODE_AREA_OFFSET_X = 18;
    private static final int NODE_AREA_OFFSET_Y = 24;

    private static final int NODE_AREA_WIDTH = 153;
    private static final int NODE_AREA_HEIGHT = 105;

    private final AbilityTreeInfo abilityTreeInfo;

    private final List<AbilityNodeWidget> nodeWidgets = new ArrayList<>();

    private int currentPage;

    public CustomAbilityTreeScreen() {
        super(Component.literal("Ability Tree"));

        abilityTreeInfo = Models.AbilityTree.getAbilityTree(Models.Character.getClassType());
        setCurrentPage(0);
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        poseStack.pushPose();
        // Make the drawing origin the start of the texture, centered on the screen
        poseStack.translate(
                (this.width - Texture.ABILITY_TREE_BACKGROUND.width()) / 2,
                (this.height - Texture.ABILITY_TREE_BACKGROUND.height()) / 2,
                0);

        RenderUtils.drawTexturedRect(poseStack, Texture.ABILITY_TREE_BACKGROUND, 0, 0);

        poseStack.pushPose();
        poseStack.translate(NODE_AREA_OFFSET_X, NODE_AREA_OFFSET_Y, 0);

        renderNodes(poseStack, mouseX, mouseY, partialTick);

        poseStack.popPose();

        poseStack.popPose();
    }

    private void setCurrentPage(int page) {
        currentPage = page;

        reloadAbilityNodeWidgets();
    }

    private void reloadAbilityNodeWidgets() {
        nodeWidgets.clear();

        abilityTreeInfo.nodes().stream()
                .filter(node -> node.location().page() == currentPage + 1)
                .forEach(node -> {
                    Pair<Integer, Integer> renderLocation = getRenderLocation(node.location());

                    nodeWidgets.add(new AbilityNodeWidget(
                            renderLocation.a() - AbilityNodeWidget.SIZE / 2,
                            renderLocation.b() - AbilityNodeWidget.SIZE / 2,
                            AbilityNodeWidget.SIZE,
                            AbilityNodeWidget.SIZE,
                            node));
                });
    }

    private void renderNodes(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        nodeWidgets.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));
    }

    private Pair<Integer, Integer> getRenderLocation(AbilityTreeLocation location) {
        float horizontalChunkWidth = NODE_AREA_WIDTH / AbilityTreeLocation.MAX_COLS;
        float verticalChunkHeight = NODE_AREA_HEIGHT / AbilityTreeLocation.MAX_ROWS;

        return Pair.of((int) (location.col() * horizontalChunkWidth + horizontalChunkWidth / 2f), (int)
                (location.row() * verticalChunkHeight + verticalChunkHeight / 2f));
    }
}
