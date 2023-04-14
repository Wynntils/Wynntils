/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.abilities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.abilitytree.type.AbilityTreeConnectionNode;
import com.wynntils.models.abilitytree.type.AbilityTreeConnectionType;
import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeLocation;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.abilitytree.type.ParsedAbilityTree;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.screens.abilities.widgets.AbilityNodeConnectionWidget;
import com.wynntils.screens.abilities.widgets.AbilityNodeWidget;
import com.wynntils.screens.base.WynntilsScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public class CustomAbilityTreeScreen extends WynntilsScreen {
    private static final int NODE_AREA_OFFSET_X = 18;
    private static final int NODE_AREA_OFFSET_Y = 24;

    private static final int NODE_AREA_WIDTH = 153;
    private static final int NODE_AREA_HEIGHT = 105;

    private final AbilityTreeInfo abilityTreeInfo;
    private ParsedAbilityTree currentAbilityTree;

    private final List<AbilityNodeWidget> nodeWidgets = new ArrayList<>();
    private final List<AbilityNodeConnectionWidget> connectionWidgets = new ArrayList<>();

    private int currentPage;

    public CustomAbilityTreeScreen() {
        super(Component.literal("Ability Tree"));

        if (Models.Character.getClassType() == ClassType.NONE) {
            abilityTreeInfo = null;
            McUtils.sendMessageToClient(Component.translatable("screens.wynntils.abilityTree.noClassData"));
            onClose();
            return;
        }

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

    public void updateAbilityTree(ParsedAbilityTree parsedAbilityTree) {
        currentAbilityTree = parsedAbilityTree;
    }

    private void setCurrentPage(int page) {
        currentPage = page;

        reloadAbilityNodeWidgets();
    }

    private void reloadAbilityNodeWidgets() {
        nodeWidgets.clear();
        connectionWidgets.clear();

        abilityTreeInfo.getNodes().stream()
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

        for (AbilityNodeWidget nodeWidget : nodeWidgets) {
            final AbilityTreeSkillNode currentNode = nodeWidget.getNode();
            final int col = currentNode.location().col();
            final int row = currentNode.location().row();

            for (String connection : currentNode.connections()) {
                Optional<AbilityTreeSkillNode> connectionOptional = abilityTreeInfo.getNodes().stream()
                        .filter(node -> node.name().equals(connection))
                        .findFirst();

                if (connectionOptional.isEmpty()) {
                    WynntilsMod.warn("Unable to find connection node for " + connection);
                    continue;
                }

                AbilityTreeSkillNode connectionNode = connectionOptional.get();

                // FIXME: Handle connections between pages
                if (connectionNode.location().page() != currentNode.location().page()) {
                    continue;
                }

                final int connectionCol = connectionNode.location().col();
                final int connectionRow = connectionNode.location().row();

                int startCol = Math.min(col, connectionCol);
                int endCol = Math.max(col, connectionCol);

                for (int i = startCol + 1; i <= endCol - 1; i++) {
                    Pair<Integer, Integer> renderLocation = getRenderLocation(
                            new AbilityTreeLocation(connectionNode.location().page(), row, i));

                    // FIXME: currentNode and connectionNode might be swapped, for merging, make sure to fix order
                    connectionWidgets.add(new AbilityNodeConnectionWidget(
                            renderLocation.a() - AbilityNodeWidget.SIZE / 2,
                            renderLocation.b() - AbilityNodeWidget.SIZE / 2,
                            AbilityNodeWidget.SIZE,
                            AbilityNodeWidget.SIZE,
                            new AbilityTreeConnectionNode(
                                    AbilityTreeConnectionType.HORIZONTAL,
                                    new AbilityTreeSkillNode[] {null, currentNode, null, connectionNode})));
                }

                int startRow = Math.min(row, connectionRow);
                int endRow = Math.max(row, connectionRow);

                for (int i = startRow + 1; i <= endRow - 1; i++) {
                    Pair<Integer, Integer> renderLocation = getRenderLocation(
                            new AbilityTreeLocation(connectionNode.location().page(), i, col));

                    // FIXME: currentNode and connectionNode might be swapped, for merging, make sure to fix order
                    connectionWidgets.add(new AbilityNodeConnectionWidget(
                            renderLocation.a() - AbilityNodeWidget.SIZE / 2,
                            renderLocation.b() - AbilityNodeWidget.SIZE / 2,
                            AbilityNodeWidget.SIZE,
                            AbilityNodeWidget.SIZE,
                            new AbilityTreeConnectionNode(
                                    AbilityTreeConnectionType.VERTICAL,
                                    new AbilityTreeSkillNode[] {currentNode, null, connectionNode, null})));
                }
            }
        }
    }

    private void renderNodes(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        connectionWidgets.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));
        nodeWidgets.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));
    }

    private Pair<Integer, Integer> getRenderLocation(AbilityTreeLocation location) {
        float horizontalChunkWidth = NODE_AREA_WIDTH / AbilityTreeLocation.MAX_COLS;
        float verticalChunkHeight = NODE_AREA_HEIGHT / AbilityTreeLocation.MAX_ROWS;

        return Pair.of((int) (location.col() * horizontalChunkWidth + horizontalChunkWidth / 2f), (int)
                (location.row() * verticalChunkHeight + verticalChunkHeight / 2f));
    }
}
