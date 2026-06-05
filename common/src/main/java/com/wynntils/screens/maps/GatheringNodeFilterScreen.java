/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.profession.type.MaterialProfile;
import com.wynntils.screens.base.widgets.InfoButton;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.widgets.GatheringNodeFilterWidget;
import com.wynntils.screens.maps.widgets.GatheringProfessionFilterButton;
import com.wynntils.services.map.PoiService;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class GatheringNodeFilterScreen extends WynntilsScreen {
    private static final float SCROLL_FACTOR = 10f;
    private static final int MAX_WIDGETS_PER_PAGE = 9;
    private static final int SCROLL_RENDER_X = 341;
    private static final int SCROLL_AREA_HEIGHT = 168;

    private final MainMapScreen oldMapScreen;
    private List<GatheringNodeFilterWidget> gatheringNodeFilterWidgets = new ArrayList<>();
    private List<GatheringProfessionFilterButton> professionFilterButtons = new ArrayList<>();
    private List<PoiService.GatheringNodeType> gatheringNodeTypes = new ArrayList<>();
    private Map<MaterialProfile.MaterialType, Boolean> filteredMaterialTypes =
            new EnumMap<>(MaterialProfile.MaterialType.class);

    private TextInputBoxWidget searchInput;

    private boolean draggingScroll = false;
    private int gatheringNodesScrollOffset = 0;
    private float scrollY;

    private GatheringNodeFilterScreen(MainMapScreen oldMapScreen) {
        super(Component.literal("Gathering Node Filter Screen"));
        this.oldMapScreen = oldMapScreen;
    }

    public static Screen create(MainMapScreen oldMapScreen) {
        return new GatheringNodeFilterScreen(oldMapScreen);
    }

    @Override
    protected void doInit() {
        addRenderableWidget(
                new Button.Builder(Component.literal("X").withStyle(ChatFormatting.RED), (button) -> onClose())
                        .pos((int) (getTranslationX() + Texture.WAYPOINT_MANAGER_BACKGROUND.width() + 10), (int)
                                (getTranslationY() - 25))
                        .size(20, 20)
                        .build());

        addRenderableWidget(new InfoButton(
                (int) (getTranslationX() - 30),
                (int) (getTranslationY() - 25),
                Component.literal("")
                        .append(Component.translatable("screens.wynntils.gatheringNodeFilterGui.help")
                                .withStyle(ChatFormatting.UNDERLINE))
                        .append(Component.literal("\n"))
                        .append(Component.translatable("screens.wynntils.gatheringNodeFilterGui.help1")
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("\n"))
                        .append(Component.translatable("screens.wynntils.gatheringNodeFilterGui.help2")
                                .withStyle(ChatFormatting.GRAY))));

        searchInput = new TextInputBoxWidget(
                (int) (getTranslationX() + 5),
                (int) (getTranslationY() - 25),
                Texture.WAYPOINT_MANAGER_BACKGROUND.width() / 2,
                20,
                (s) -> {
                    gatheringNodesScrollOffset = 0;
                    populateGatheringNodeTypes();
                },
                this,
                searchInput);

        addRenderableWidget(searchInput);
        setFocusedTextInput(searchInput);

        addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.gatheringNodeFilterGui.showAll"),
                        (button) -> toggleAllGatheringNodeTypes(true))
                .pos((width / 2) - 102, (int) (getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() + 10))
                .size(60, 20)
                .build());

        addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.gatheringNodeFilterGui.hideAll"),
                        (button) -> toggleAllGatheringNodeTypes(false))
                .pos((width / 2) + 42, (int) (getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() + 10))
                .size(60, 20)
                .build());

        populateProfessionFilters();
        populateGatheringNodeTypes();
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(oldMapScreen);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        renderScroll(guiGraphics);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.gatheringNodeFilterGui.search")),
                        getTranslationX() + 5,
                        getTranslationY() - 27.5f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.gatheringNodeFilterGui.professionFilter")),
                        getTranslationX() + 15 + Texture.WAYPOINT_MANAGER_BACKGROUND.width() / 2f,
                        getTranslationY() - 27.5f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.gatheringNodeFilterGui.icon")),
                        getTranslationX() + 10,
                        getTranslationY() + 4,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.gatheringNodeFilterGui.resource")),
                        getTranslationX() + 78,
                        getTranslationY() + 4,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.gatheringNodeFilterGui.level")),
                        getTranslationX() + 200,
                        getTranslationY() + 4,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        if (gatheringNodeTypes.isEmpty()) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(Component.translatable(
                                    "screens.wynntils.gatheringNodeFilterGui.noGatheringNodeTypes")),
                            getTranslationX() + Texture.WAYPOINT_MANAGER_BACKGROUND.width() / 2f,
                            getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() / 2f,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        } else {
            RenderUtils.enableScissor(
                    guiGraphics, (int) (getTranslationX() + 10), (int) (getTranslationY() + 16), 322, 181);
            gatheringNodeFilterWidgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
            RenderUtils.disableScissor(guiGraphics);
        }

        professionFilterButtons.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));

        if (draggingScroll) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else if (MathUtils.isInside(
                mouseX,
                mouseY,
                (int) (getTranslationX() + SCROLL_RENDER_X),
                (int) (getTranslationX() + SCROLL_RENDER_X + Texture.SCROLL_BUTTON.width()),
                (int) scrollY,
                (int) (scrollY + Texture.SCROLL_BUTTON.height()))) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        RenderUtils.drawTexturedRect(
                guiGraphics, Texture.WAYPOINT_MANAGER_BACKGROUND, getTranslationX(), getTranslationY());
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!draggingScroll
                && MathUtils.isInside(
                        (int) event.x(),
                        (int) event.y(),
                        (int) (getTranslationX() + SCROLL_RENDER_X),
                        (int) (getTranslationX() + SCROLL_RENDER_X + Texture.SCROLL_BUTTON.width()),
                        (int) scrollY,
                        (int) (scrollY + Texture.SCROLL_BUTTON.height()))) {
            draggingScroll = true;
            return true;
        }

        for (GatheringNodeFilterWidget widget : gatheringNodeFilterWidgets) {
            if (widget.isMouseOver(event.x(), event.y())) {
                return widget.mouseClicked(event, isDoubleClick);
            }
        }

        for (GatheringProfessionFilterButton widget : professionFilterButtons) {
            if (widget.isMouseOver(event.x(), event.y())) {
                return widget.mouseClicked(event, isDoubleClick);
            }
        }

        return super.doMouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!draggingScroll) return false;

        int scrollAreaStartY = (int) (getTranslationY() + 15 + 16);
        int scrollAreaHeight = SCROLL_AREA_HEIGHT - Texture.SCROLL_BUTTON.height();
        int newOffset = Math.round(MathUtils.map(
                (float) event.y(), scrollAreaStartY, scrollAreaStartY + scrollAreaHeight, 0, getMaxScrollOffset()));

        scroll(Math.max(0, Math.min(newOffset, getMaxScrollOffset())));
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScroll = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);
        int newOffset = Math.max(0, Math.min(gatheringNodesScrollOffset + scrollAmount, getMaxScrollOffset()));
        scroll(newOffset);
        return true;
    }

    public void toggleGatheringNodeType(PoiService.GatheringNodeType gatheringNodeType) {
        Services.Poi.setGatheringNodeTypeVisible(
                gatheringNodeType, !Services.Poi.isGatheringNodeTypeVisible(gatheringNodeType));
    }

    public void toggleMaterialType(MaterialProfile.MaterialType materialType, boolean selected, boolean excludeOthers) {
        if (excludeOthers) {
            for (MaterialProfile.MaterialType filteredMaterialType : MaterialProfile.MaterialType.values()) {
                if (filteredMaterialType != materialType) {
                    filteredMaterialTypes.put(filteredMaterialType, !selected);
                }
            }
        }

        filteredMaterialTypes.put(materialType, selected);
        gatheringNodesScrollOffset = 0;
        populateProfessionFilters();
        populateGatheringNodeTypes();
    }

    private void toggleAllGatheringNodeTypes(boolean visible) {
        Services.Poi.setAllGatheringNodeTypesVisible(visible);
    }

    private void renderScroll(GuiGraphics guiGraphics) {
        if (gatheringNodeTypes.size() <= MAX_WIDGETS_PER_PAGE) return;

        scrollY = getTranslationY()
                + 15
                + MathUtils.map(
                        gatheringNodesScrollOffset, 0, getMaxScrollOffset(), 0, 186 - Texture.SCROLL_BUTTON.height());

        RenderUtils.drawTexturedRect(guiGraphics, Texture.SCROLL_BUTTON, getTranslationX() + SCROLL_RENDER_X, scrollY);
    }

    private void scroll(int newOffset) {
        gatheringNodesScrollOffset = newOffset;
        int currentY = (int) (getTranslationY() + 16);

        for (GatheringNodeFilterWidget widget : gatheringNodeFilterWidgets) {
            int newY = currentY - gatheringNodesScrollOffset;
            widget.updateRenderY(newY);
            widget.visible = (newY <= getTranslationY() + 16 + 179) && (newY + 20 >= getTranslationY() + 16);
            currentY += 20;
        }
    }

    private int getMaxScrollOffset() {
        return Math.max(0, (gatheringNodeFilterWidgets.size() - MAX_WIDGETS_PER_PAGE) * 20);
    }

    private void populateGatheringNodeTypes() {
        gatheringNodeFilterWidgets = new ArrayList<>();
        gatheringNodeTypes = Services.Poi.getGatheringNodeTypes().stream()
                .filter(gatheringNodeType -> filteredMaterialTypes.getOrDefault(gatheringNodeType.materialType(), true))
                .filter(gatheringNodeType ->
                        searchMatches(gatheringNodeType.sourceMaterial().name()))
                .toList();

        int renderX = (int) (getTranslationX() + 12);
        int renderY = (int) (getTranslationY() + 16);

        for (PoiService.GatheringNodeType gatheringNodeType : gatheringNodeTypes) {
            GatheringNodeFilterWidget gatheringNodeFilterWidget =
                    new GatheringNodeFilterWidget(renderX, renderY, 320, 20, this, gatheringNodeType);

            gatheringNodeFilterWidget.visible = renderY <= getTranslationY() + 16 + 179;
            gatheringNodeFilterWidgets.add(gatheringNodeFilterWidget);
            renderY += 20;
        }

        scroll(Math.min(gatheringNodesScrollOffset, getMaxScrollOffset()));
    }

    private void populateProfessionFilters() {
        professionFilterButtons = new ArrayList<>();
        int renderX = (int) (getTranslationX() + 15 + Texture.WAYPOINT_MANAGER_BACKGROUND.width() / 2f);

        for (MaterialProfile.MaterialType materialType : MaterialProfile.MaterialType.values()) {
            boolean selected = filteredMaterialTypes.getOrDefault(materialType, true);
            filteredMaterialTypes.put(materialType, selected);
            professionFilterButtons.add(new GatheringProfessionFilterButton(
                    renderX, (int) (getTranslationY() - 25), 20, this, materialType, selected));
            renderX += 20;
        }
    }

    private boolean searchMatches(String text) {
        return searchInput == null
                || searchInput.getTextBoxInput().isBlank()
                || StringUtils.partialMatch(text, searchInput.getTextBoxInput());
    }

    private float getTranslationX() {
        return (this.width - Texture.WAYPOINT_MANAGER_BACKGROUND.width()) / 2f;
    }

    private float getTranslationY() {
        return (this.height - Texture.WAYPOINT_MANAGER_BACKGROUND.height()) / 2f;
    }
}
