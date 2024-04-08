/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.abilities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.abilitytree.type.AbilityTreeConnectionNode;
import com.wynntils.models.abilitytree.type.AbilityTreeConnectionType;
import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeLocation;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.abilitytree.type.ArchetypeInfo;
import com.wynntils.models.abilitytree.type.ParsedAbilityTree;
import com.wynntils.screens.abilities.widgets.AbilityArchetypeWidget;
import com.wynntils.screens.abilities.widgets.AbilityNodeConnectionWidget;
import com.wynntils.screens.abilities.widgets.AbilityNodeWidget;
import com.wynntils.screens.abilities.widgets.AbilityTreePageSelectorButton;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public class CustomAbilityTreeScreen extends WynntilsScreen {
    private static final int NODE_AREA_OFFSET_X = 81;
    private static final int NODE_AREA_OFFSET_Y = 22;

    private static final int NODE_AREA_WIDTH = 156;
    private static final int NODE_AREA_HEIGHT = 98;

    private static final int UP_ARROW_X = 250;
    private static final int UP_ARROW_Y = 117;

    private static final int DOWN_ARROW_X = 278;
    private static final int DOWN_ARROW_Y = 117;

    private final AbilityTreeInfo abilityTreeInfo;
    private ParsedAbilityTree parsedAbilityTree;

    private final List<AbilityNodeWidget> nodeWidgets = new ArrayList<>();
    private final Map<AbilityTreeLocation, AbilityNodeConnectionWidget> connectionWidgets = new LinkedHashMap();

    private final List<AbilityArchetypeWidget> archetypeWidgets = new ArrayList<>();

    private float currentScrollPercentage;
    private TreeParseState treeParseState = TreeParseState.PARSING;

    public CustomAbilityTreeScreen() {
        super(Component.literal("Ability Tree"));

        abilityTreeInfo = Models.AbilityTree.getAbilityTree(Models.Character.getClassType());
        setCurrentScrollPercentage(0);
    }

    // region Init

    @Override
    protected void doInit() {
        this.addRenderableWidget(new AbilityTreePageSelectorButton(
                UP_ARROW_X,
                UP_ARROW_Y,
                Texture.ABILITY_TREE_UP_ARROW.width(),
                Texture.ABILITY_TREE_UP_ARROW.height(),
                this,
                true));

        this.addRenderableWidget(new AbilityTreePageSelectorButton(
                DOWN_ARROW_X,
                DOWN_ARROW_Y,
                Texture.ABILITY_TREE_DOWN_ARROW.width(),
                Texture.ABILITY_TREE_DOWN_ARROW.height(),
                this,
                false));
    }

    // endregion

    // region Rendering

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        poseStack.pushPose();
        // Make the drawing origin the start of the texture, centered on the screen
        poseStack.translate(
                (this.width - Texture.ABILITY_TREE_BACKGROUND.width()) / 2,
                (this.height - Texture.ABILITY_TREE_BACKGROUND.height()) / 2,
                0);

        final int backgroundWidth = Texture.ABILITY_TREE_BACKGROUND.width();
        final int backgroundHeight = Texture.ABILITY_TREE_BACKGROUND.height();
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.ABILITY_TREE_BACKGROUND.resource(),
                0,
                0,
                0,
                backgroundWidth,
                backgroundHeight,
                backgroundWidth,
                backgroundHeight);

        if (treeParseState == TreeParseState.PARSED) {
            renderNodes(guiGraphics, mouseX, mouseY, partialTick);
            renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

            // Render tooltips
            renderTooltip(guiGraphics, mouseX, mouseY);
        } else {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromComponent(treeParseState.getText()),
                            0,
                            backgroundWidth,
                            0,
                            backgroundHeight,
                            0,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.OUTLINE,
                            1f);
        }

        poseStack.popPose();
    }

    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Translate the mouse position to match what is rendered on the screen
        int scaledMouseX = mouseX - (this.width - Texture.ABILITY_TREE_BACKGROUND.width()) / 2;
        int scaledMouseY = mouseY - (this.height - Texture.ABILITY_TREE_BACKGROUND.height()) / 2;

        // For the node widgets, we need to check if the mouse is over the node,
        // with the scroll offset applied
        int nodeWidgetMouseX = scaledMouseX - NODE_AREA_OFFSET_X;
        int nodeWidgetMouseY = scaledMouseY - NODE_AREA_OFFSET_Y;

        // Only show tooltips if the mouse is within the node area
        if (nodeWidgetMouseX >= 0
                && nodeWidgetMouseX <= NODE_AREA_WIDTH
                && nodeWidgetMouseY >= 0
                && nodeWidgetMouseY <= NODE_AREA_HEIGHT) {
            nodeWidgetMouseY += NODE_AREA_HEIGHT * currentScrollPercentage;

            for (AbilityNodeWidget nodeWidget : nodeWidgets) {
                if (nodeWidget.isMouseOver(nodeWidgetMouseX, nodeWidgetMouseY)) {
                    List<Component> tooltipLines = nodeWidget.getTooltipLines();
                    guiGraphics.renderTooltip(
                            FontRenderer.getInstance().getFont(),
                            tooltipLines,
                            Optional.empty(),
                            scaledMouseX,
                            scaledMouseY);
                    return;
                }
            }
        }

        for (AbilityArchetypeWidget archetypeWidget : archetypeWidgets) {
            if (archetypeWidget.isMouseOver(scaledMouseX, scaledMouseY)) {
                List<Component> tooltipLines = archetypeWidget.getTooltipLines();
                guiGraphics.renderTooltip(
                        FontRenderer.getInstance().getFont(),
                        tooltipLines,
                        Optional.empty(),
                        scaledMouseX,
                        scaledMouseY);
                return;
            }
        }

        for (GuiEventListener child : this.children()) {
            if (child instanceof TooltipProvider tooltipProvider) {
                if (child.isMouseOver(scaledMouseX, scaledMouseY)) {
                    List<Component> tooltipLines = tooltipProvider.getTooltipLines();
                    guiGraphics.renderTooltip(
                            FontRenderer.getInstance().getFont(),
                            tooltipLines,
                            Optional.empty(),
                            scaledMouseX,
                            scaledMouseY);
                    return;
                }
            }
        }
    }

    private void renderNodes(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        // Set the node area offset
        poseStack.translate(NODE_AREA_OFFSET_X, NODE_AREA_OFFSET_Y, 0);

        // Make this area a mask, so we only render the nodes within this area
        // Make sure it's a bit larger than the actual area, so we don't cut off the nodes
        RenderUtils.createRectMask(poseStack, -5, -5, NODE_AREA_WIDTH + 10, NODE_AREA_HEIGHT + 5);

        // Translate the nodes based on the current scroll percentage
        poseStack.translate(0, -NODE_AREA_HEIGHT * currentScrollPercentage, 0);

        nodeWidgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
        connectionWidgets.values().forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));

        RenderUtils.clearMask();

        poseStack.popPose();
    }

    private void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        archetypeWidgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
        renderables.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));

        poseStack.popPose();
    }

    // endregion

    // region Mouse Handlers

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        if (treeParseState != TreeParseState.PARSED) return false;

        // Translate the mouse position to match what is rendered on the screen
        double scaledMouseX = mouseX - (this.width - Texture.ABILITY_TREE_BACKGROUND.width()) / 2;
        double scaledMouseY = mouseY - (this.height - Texture.ABILITY_TREE_BACKGROUND.height()) / 2;

        for (GuiEventListener child : this.children()) {
            if (child.isMouseOver(scaledMouseX, scaledMouseY)) {
                if (child.mouseClicked(scaledMouseX, scaledMouseY, button)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (treeParseState != TreeParseState.PARSED) return false;

        setCurrentScrollPercentage((float) (currentScrollPercentage - scrollY));
        return true;
    }

    // endregion

    // region Page Management

    public void setCurrentScrollPercentage(float page) {
        currentScrollPercentage = MathUtils.clamp(page, 0, Models.AbilityTree.ABILITY_TREE_PAGES - 1);
    }

    public float getCurrentScrollPercentage() {
        return currentScrollPercentage;
    }

    // endregion

    // region Node Widget Building

    private void reconstructWidgets() {
        nodeWidgets.clear();
        connectionWidgets.clear();

        // Add nodes and connections for every page,
        // we calculate what's rendered based on the current scroll percentage
        // at render time
        for (int page = 1; page <= Models.AbilityTree.ABILITY_TREE_PAGES; page++) {
            int currentPage = page;

            int currentPageYRenderOffset = NODE_AREA_HEIGHT * (currentPage - 1);

            List<AbilityNodeWidget> currentPageNodeWidgets = new ArrayList<>();

            abilityTreeInfo.nodes().stream()
                    .filter(node -> node.location().page() == currentPage)
                    .forEach(node -> {
                        Pair<Integer, Integer> renderLocation = getRenderLocation(node.location());

                        AbilityNodeWidget nodeWidget = new AbilityNodeWidget(
                                renderLocation.a() - AbilityNodeWidget.SIZE / 2,
                                renderLocation.b() - AbilityNodeWidget.SIZE / 2 + currentPageYRenderOffset,
                                AbilityNodeWidget.SIZE,
                                AbilityNodeWidget.SIZE,
                                parsedAbilityTree,
                                node);
                        currentPageNodeWidgets.add(nodeWidget);
                        nodeWidgets.add(nodeWidget);
                    });

            // We do this "backwards":
            // First find connection nodes from last page
            List<AbilityTreeSkillNode> multiPageConnectionNodesFromLastPage = abilityTreeInfo.nodes().stream()
                    .filter(node -> node.location().page() == currentPage - 1)
                    .filter(node -> currentPageNodeWidgets.stream()
                            .map(AbilityNodeWidget::getNode)
                            .map(AbilityTreeSkillNode::id)
                            .anyMatch(node.connections()::contains))
                    .toList();

            // Then, find the nodes they connect to on this page
            for (AbilityTreeSkillNode connectionNode : multiPageConnectionNodesFromLastPage) {
                List<AbilityTreeSkillNode> multiPageConnections = currentPageNodeWidgets.stream()
                        .map(AbilityNodeWidget::getNode)
                        .filter(node -> connectionNode.connections().contains(node.id()))
                        .toList();

                for (AbilityTreeSkillNode currentNode : multiPageConnections) {
                    final int col = currentNode.location().col();
                    final int row = currentNode.location().row();

                    // Multi page connections are basically the same as vertical connections,
                    // when the receiving node is the one rendered. But this has to be handled first.
                    addConnectionsVertically(currentNode, connectionNode, col, 0, row, currentPageYRenderOffset);
                }
            }

            for (AbilityNodeWidget nodeWidget : currentPageNodeWidgets) {
                final AbilityTreeSkillNode currentNode = nodeWidget.getNode();
                final int col = currentNode.location().col();
                final int row = currentNode.location().row();

                for (Integer connection : currentNode.connections()) {
                    Optional<AbilityTreeSkillNode> connectionOptional = abilityTreeInfo.nodes().stream()
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
                        addConnectionsHorizontally(
                                currentNode, connectionNode, col, row, connectionCol, currentPageYRenderOffset);
                        continue;
                    }

                    // Only vertical connections are needed for the same row
                    if (col == connectionCol) {
                        addConnectionsVertically(
                                currentNode, connectionNode, col, row, connectionRow, currentPageYRenderOffset);
                        continue;
                    }

                    // Handle complex connections here

                    // Firstly, we add horizontal connections, if the turn is not enough
                    if (Math.abs(col - connectionCol) > 1) {
                        addConnectionsHorizontally(
                                currentNode, connectionNode, col, row, connectionCol, currentPageYRenderOffset);
                    }

                    // Then we add the turn
                    addTurnConnection(currentNode, connectionNode, col, row, connectionCol, currentPageYRenderOffset);

                    // Finally, we add vertical connections, if the turn is not enough, or if the connection is on the
                    // next
                    // page
                    if (Math.abs(row - connectionRow) > 1 || row > connectionRow) {
                        addConnectionsVertically(
                                currentNode,
                                connectionNode,
                                connectionCol,
                                row,
                                connectionRow,
                                currentPageYRenderOffset);
                    }
                }
            }
        }

        int lowerBoxRenderX = NODE_AREA_OFFSET_X;
        int lowerBoxRenderY = NODE_AREA_OFFSET_Y + NODE_AREA_HEIGHT;

        // 2/3 of the width of the node area, divided by 4, as there are 4 "spaces" between the archetypes
        int spaceBetweenArchetypes = (NODE_AREA_WIDTH - AbilityArchetypeWidget.SIZE * 3) * 2 / 3 / 4;

        // Render the archetypes in the left half of the lower box
        int archetypeCount = 0;
        for (Map.Entry<String, ArchetypeInfo> entry :
                abilityTreeInfo.archetypeInfoMap().entrySet()) {
            ArchetypeInfo archetypeInfo = entry.getValue();

            AbilityArchetypeWidget archetypeWidget = new AbilityArchetypeWidget(
                    lowerBoxRenderX + archetypeCount * (AbilityArchetypeWidget.SIZE + spaceBetweenArchetypes) + 10,
                    lowerBoxRenderY + 7,
                    AbilityArchetypeWidget.SIZE,
                    AbilityArchetypeWidget.SIZE,
                    Component.literal(archetypeInfo.name()),
                    abilityTreeInfo,
                    parsedAbilityTree,
                    archetypeInfo);

            archetypeWidgets.add(archetypeWidget);

            archetypeCount++;
        }
    }

    private Pair<Integer, Integer> getRenderLocation(AbilityTreeLocation location) {
        float horizontalChunkWidth = (float) NODE_AREA_WIDTH / AbilityTreeLocation.MAX_COLS;
        float verticalChunkHeight = (float) NODE_AREA_HEIGHT / AbilityTreeLocation.MAX_ROWS;

        return Pair.of((int) (location.col() * horizontalChunkWidth + horizontalChunkWidth / 2f), (int)
                (location.row() * verticalChunkHeight + verticalChunkHeight / 2f));
    }

    // endregion

    // region Connection Logic

    private void addTurnConnection(
            AbilityTreeSkillNode currentNode,
            AbilityTreeSkillNode connectionNode,
            int currentCol,
            int currentRow,
            int connectionCol,
            int currentPageYRenderOffset) {
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
                        renderLocation.b() - AbilityNodeConnectionWidget.SIZE / 2 + currentPageYRenderOffset,
                        AbilityNodeConnectionWidget.SIZE,
                        AbilityNodeConnectionWidget.SIZE,
                        merged));
    }

    private void addConnectionsHorizontally(
            AbilityTreeSkillNode currentNode,
            AbilityTreeSkillNode connectionNode,
            int currentCol,
            int currentRow,
            int targetCol,
            int currentPageYRenderOffset) {
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
                            renderLocation.b() - AbilityNodeConnectionWidget.SIZE / 2 + currentPageYRenderOffset,
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
            int targetRow,
            int currentPageYRenderOffset) {
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
                            renderLocation.b() - AbilityNodeConnectionWidget.SIZE / 2 + currentPageYRenderOffset,
                            AbilityNodeConnectionWidget.SIZE,
                            AbilityNodeConnectionWidget.SIZE,
                            merged));
        }
    }

    // endregion

    // region Parse State Logic

    public void setTreeParseState(TreeParseState treeParseState) {
        this.treeParseState = treeParseState;
        parsedAbilityTree = Models.AbilityTree.getCurrentAbilityTree().orElse(null);
        reconstructWidgets();
    }

    public enum TreeParseState {
        PARSING(Component.translatable("screens.wynntils.abilityTree.parsing")),
        PARSED(null),
        FAILED(Component.translatable("screens.wynntils.abilityTree.parseFailed"));

        private final Component text;

        TreeParseState(Component text) {
            this.text = text;
        }

        public Component getText() {
            return text;
        }
    }

    // endregion
}
