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
import com.wynntils.models.character.type.ClassType;
import com.wynntils.screens.abilities.widgets.AbilityNodeConnectionWidget;
import com.wynntils.screens.abilities.widgets.AbilityNodeWidget;
import com.wynntils.screens.base.WynntilsScreen;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public class CustomAbilityTreeScreen extends WynntilsScreen {
    private static final int NODE_AREA_OFFSET_X = 18;
    private static final int NODE_AREA_OFFSET_Y = 24;

    private static final int NODE_AREA_WIDTH = 153;
    private static final int NODE_AREA_HEIGHT = 105;

    private final AbilityTreeInfo abilityTreeInfo;

    private final List<AbilityNodeWidget> nodeWidgets = new ArrayList<>();
    private final Map<AbilityTreeLocation, AbilityNodeConnectionWidget> connectionWidgets = new LinkedHashMap();

    private int currentPage;

    public CustomAbilityTreeScreen() {
        super(Component.literal("Ability Tree"));

        if (Models.Character.getClassType() == ClassType.NONE) {
            abilityTreeInfo = null;
            McUtils.sendMessageToClient(Component.translatable("screens.wynntils.abilityTree.noClassData"));
            onClose();
            return;
        }

        // FIXME: Handle failure here
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

    private void renderNodes(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        nodeWidgets.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));
        connectionWidgets.values().forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        setCurrentPage((int) (currentPage - Math.signum(delta)));
        return true;
    }

    public void updateAbilityTree() {
        reconstructWidgets();
    }

    private void setCurrentPage(int page) {
        currentPage = MathUtils.clamp(page, 0, Models.AbilityTree.ABILITY_TREE_PAGES - 1);

        reconstructWidgets();
    }

    private void reconstructWidgets() {
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

        List<AbilityTreeSkillNode> multiPageConnectionNodesFromLastPage = abilityTreeInfo.getNodes().stream()
                .filter(node -> node.location().page() == currentPage)
                .filter(node -> nodeWidgets.stream()
                        .map(AbilityNodeWidget::getNode)
                        .map(AbilityTreeSkillNode::id)
                        .anyMatch(node.connections()::contains))
                .toList();

        // We do this "backwards":
        // First find connection nodes from last page, then find the nodes they connect to on this page
        for (AbilityTreeSkillNode connectionNode : multiPageConnectionNodesFromLastPage) {
            List<AbilityTreeSkillNode> multiPageConnections = nodeWidgets.stream()
                    .map(AbilityNodeWidget::getNode)
                    .filter(node -> connectionNode.connections().contains(node.id()))
                    .toList();

            for (AbilityTreeSkillNode currentNode : multiPageConnections) {
                final int col = currentNode.location().col();
                final int row = currentNode.location().row();

                // Multi page connections are basically the same as vertical connections,
                // when the receiving node is the one rendered. But this has to be handled first.
                addConnectionsVertically(currentNode, connectionNode, col, 0, row);
            }
        }

        for (AbilityNodeWidget nodeWidget : nodeWidgets) {
            final AbilityTreeSkillNode currentNode = nodeWidget.getNode();
            final int col = currentNode.location().col();
            final int row = currentNode.location().row();

            for (Integer connection : currentNode.connections()) {
                Optional<AbilityTreeSkillNode> connectionOptional = abilityTreeInfo.getNodes().stream()
                        .filter(node -> node.id() == connection)
                        .findFirst();

                if (connectionOptional.isEmpty()) {
                    WynntilsMod.warn("Unable to find connection node for " + connection);
                    continue;
                }

                AbilityTreeSkillNode connectionNode = connectionOptional.get();

                final int connectionCol = connectionNode.location().col();
                final int connectionRow = connectionNode.location().row();

                // Only horizontal connections are needed for the same column
                if (row == connectionRow) {
                    addConnectionsHorizontally(currentNode, connectionNode, col, row, connectionCol);
                    continue;
                }

                // Only vertical connections are needed for the same row
                if (col == connectionCol) {
                    addConnectionsVertically(currentNode, connectionNode, col, row, connectionRow);
                    continue;
                }

                // Handle complex connections here

                // Firstly, we add horizontal connections, if the turn is not enough
                if (Math.abs(col - connectionCol) > 1) {
                    addConnectionsHorizontally(currentNode, connectionNode, col, row, connectionCol);
                }

                // Then we add the turn
                addTurnConnection(currentNode, connectionNode, col, row, connectionCol);

                // Finally, we add vertical connections, if the turn is not enough
                if (Math.abs(row - connectionRow) > 1) {
                    addConnectionsVertically(currentNode, connectionNode, connectionCol, row, connectionRow);
                }
            }
        }
    }

    // region Connection Logic

    private void addTurnConnection(
            AbilityTreeSkillNode currentNode,
            AbilityTreeSkillNode connectionNode,
            int currentCol,
            int currentRow,
            int connectionCol) {
        AbilityTreeLocation location =
                new AbilityTreeLocation(currentNode.location().page(), currentRow, connectionCol);
        Pair<Integer, Integer> renderLocation = getRenderLocation(location);

        AbilityTreeConnectionNode node;

        if (currentCol < connectionCol) {
            node = new AbilityTreeConnectionNode(
                    AbilityTreeConnectionType.DOWN_RIGHT_TURN,
                    new AbilityTreeSkillNode[] {null, null, connectionNode, currentNode});
        } else {
            node = new AbilityTreeConnectionNode(
                    AbilityTreeConnectionType.DOWN_LEFT_TURN,
                    new AbilityTreeSkillNode[] {null, currentNode, connectionNode, null});
        }

        AbilityNodeConnectionWidget oldWidget = connectionWidgets.get(location);
        AbilityTreeConnectionNode merged = node.merge(oldWidget != null ? oldWidget.getNode() : null);

        connectionWidgets.put(
                location,
                new AbilityNodeConnectionWidget(
                        renderLocation.a() - AbilityNodeConnectionWidget.SIZE / 2,
                        renderLocation.b() - AbilityNodeConnectionWidget.SIZE / 2,
                        AbilityNodeConnectionWidget.SIZE,
                        AbilityNodeConnectionWidget.SIZE,
                        merged));
    }

    private void addConnectionsHorizontally(
            AbilityTreeSkillNode currentNode,
            AbilityTreeSkillNode connectionNode,
            int currentCol,
            int currentRow,
            int targetCol) {
        AbilityTreeSkillNode startNode = currentCol < targetCol ? currentNode : connectionNode;
        AbilityTreeSkillNode endNode = currentCol < targetCol ? connectionNode : currentNode;

        int startCol = Math.min(currentCol, targetCol) + 1;
        int endCol = Math.max(currentCol, targetCol) - 1;

        for (int i = startCol; i <= endCol; i++) {
            AbilityTreeLocation location =
                    new AbilityTreeLocation(currentNode.location().page(), currentRow, i);
            Pair<Integer, Integer> renderLocation = getRenderLocation(location);

            AbilityTreeConnectionNode node = new AbilityTreeConnectionNode(
                    AbilityTreeConnectionType.HORIZONTAL, new AbilityTreeSkillNode[] {null, endNode, null, startNode});

            AbilityNodeConnectionWidget oldWidget = connectionWidgets.get(location);
            AbilityTreeConnectionNode merged = node.merge(oldWidget != null ? oldWidget.getNode() : null);

            connectionWidgets.put(
                    location,
                    new AbilityNodeConnectionWidget(
                            renderLocation.a() - AbilityNodeConnectionWidget.SIZE / 2,
                            renderLocation.b() - AbilityNodeConnectionWidget.SIZE / 2,
                            AbilityNodeConnectionWidget.SIZE,
                            AbilityNodeConnectionWidget.SIZE,
                            merged));
        }
    }

    private void addConnectionsVertically(
            AbilityTreeSkillNode currentNode,
            AbilityTreeSkillNode connectionNode,
            int currentCol,
            int currentRow,
            int targetRow) {
        AbilityTreeSkillNode startNode;
        AbilityTreeSkillNode endNode;
        int startRow;
        int endRow;

        if (currentNode.location().page() > connectionNode.location().page()) {
            startNode = connectionNode;
            endNode = currentNode;

            startRow = 0;
            endRow = targetRow - 1;
        } else if (currentNode.location().page() < connectionNode.location().page()) {
            startNode = currentNode;
            endNode = connectionNode;

            startRow = currentRow + 1;
            endRow = AbilityTreeLocation.MAX_ROWS - 1;
        } else {
            startNode = currentRow < targetRow ? currentNode : connectionNode;
            endNode = currentRow < targetRow ? connectionNode : currentNode;

            startRow = Math.min(currentRow, targetRow) + 1;
            endRow = Math.max(currentRow, targetRow) - 1;
        }

        for (int i = startRow; i <= endRow; i++) {
            AbilityTreeLocation location =
                    new AbilityTreeLocation(currentNode.location().page(), i, currentCol);
            Pair<Integer, Integer> renderLocation = getRenderLocation(location);

            AbilityTreeConnectionNode node = new AbilityTreeConnectionNode(
                    AbilityTreeConnectionType.VERTICAL, new AbilityTreeSkillNode[] {startNode, null, endNode, null});

            AbilityNodeConnectionWidget oldWidget = connectionWidgets.get(location);
            AbilityTreeConnectionNode merged = node.merge(oldWidget != null ? oldWidget.getNode() : null);

            connectionWidgets.put(
                    location,
                    new AbilityNodeConnectionWidget(
                            renderLocation.a() - AbilityNodeConnectionWidget.SIZE / 2,
                            renderLocation.b() - AbilityNodeConnectionWidget.SIZE / 2,
                            AbilityNodeConnectionWidget.SIZE,
                            AbilityNodeConnectionWidget.SIZE,
                            merged));
        }
    }

    // endregion

    private Pair<Integer, Integer> getRenderLocation(AbilityTreeLocation location) {
        float horizontalChunkWidth = NODE_AREA_WIDTH / AbilityTreeLocation.MAX_COLS;
        float verticalChunkHeight = NODE_AREA_HEIGHT / AbilityTreeLocation.MAX_ROWS;

        return Pair.of((int) (location.col() * horizontalChunkWidth + horizontalChunkWidth / 2f), (int)
                (location.row() * verticalChunkHeight + verticalChunkHeight / 2f));
    }
}
