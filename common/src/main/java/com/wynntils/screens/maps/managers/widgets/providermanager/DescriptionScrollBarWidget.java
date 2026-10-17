/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.screens.maps.managers.widgets.ScrollBarWidget;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
import com.wynntils.services.mapdata.providers.type.MapDataProvider;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.net.URI;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

public class DescriptionScrollBarWidget extends ScrollBarWidget {
    private static final int WIDGET_HEIGHT_PADDING = 5;
    private static final int PADDING = 5;
    private static final int LINE_GAP = 2;
    private static final Font FONT = FontRenderer.getInstance().getFont();
    private static final int LINE_HEIGHT = FONT.lineHeight;
    private static final CustomColor TITLE_COLOR = CustomColor.fromInt(0x524030);

    private final PropertiesBoxWidget propertiesWidget;
    private final DescriptionBoxWidget descriptionWidget;

    public DescriptionScrollBarWidget(int x, int y, int width, int height, ProviderManagementScreen parent) {
        super(x, y, width, height);

        this.propertiesWidget = new PropertiesBoxWidget(parent);
        this.descriptionWidget = new DescriptionBoxWidget();
    }

    @Override
    protected int getWidgetHeightPadding() {
        return WIDGET_HEIGHT_PADDING;
    }

    @Override
    protected List<? extends AbstractWidget> getWidgets() {
        return List.of(propertiesWidget, descriptionWidget);
    }

    @Override
    protected void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        getWidgets().forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    public void updateContentHeight() {
        propertiesWidget.refresh();
        scrollOffsetY = 0;
        updateWidgetPositions();
    }

    public void setDescription(StyledText description) {
        descriptionWidget.setDescription(description);
        updateWidgetPositions();
    }

    public void updateWidgetPositions() {
        int viewportLeft = getX() + SCROLL_BAR_WIDTH_PADDING;
        int currentY = getY() + SCROLL_BAR_HEIGHT_PADDING - scrollOffsetY;

        for (AbstractWidget widget : getWidgets()) {
            widget.setX(viewportLeft);
            widget.setY(currentY);
            widget.setWidth(getViewportWidth());
            currentY += widget.getHeight() + getWidgetHeightPadding();
        }
    }

    @Override
    protected void onScrollOffsetChanged() {
        updateWidgetPositions();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        updateWidgetPositions();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (!isInsideViewport(mouseX, mouseY)) {
            return false;
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        for (AbstractWidget widget : getWidgets()) {
            if (widget.mouseClicked(event, isDoubleClick)) {
                return true;
            }
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    private static final class PropertiesBoxWidget extends AbstractWidget {
        private final ProviderManagementScreen parent;
        private final StyledText title;

        private List<Component> stats = List.of();
        private Component url = null;
        private String urlString = null;

        private PropertiesBoxWidget(ProviderManagementScreen parent) {
            super(0, 0, 0, 0, Component.literal("Properties Box Widget"));
            this.parent = parent;
            this.title = StyledText.fromComponent(Component.translatable(
                            "screens.wynntils.map.managers.providerManager.descriptionScrollBarWidget.properties.title")
                    .withStyle(ChatFormatting.BOLD));
        }

        private void refresh() {
            MapDataProvider provider = Services.MapData.getProviderFromId(parent.getSelectedProviderId())
                    .orElseThrow();

            stats = List.of(
                    Component.translatable(
                            "screens.wynntils.map.managers.providerManager.descriptionScrollBarWidget.properties.features",
                            provider.getFeatures().count()),
                    Component.translatable(
                            "screens.wynntils.map.managers.providerManager.descriptionScrollBarWidget.properties.categories",
                            provider.getCategories().count()),
                    Component.translatable(
                            "screens.wynntils.map.managers.providerManager.descriptionScrollBarWidget.properties.icons",
                            provider.getIcons().count()));

            JsonProviderInfo jsonProviderInfo =
                    Services.MapData.getJsonProviderInfo(provider).orElse(null);
            if (jsonProviderInfo != null) {
                urlString = jsonProviderInfo.providerUrl();
                url = Component.translatable(
                        "screens.wynntils.map.managers.providerManager.descriptionScrollBarWidget.properties.url",
                        urlString);
            } else {
                urlString = null;
                url = null;
            }

            int bodyHeight = LINE_HEIGHT + (url != null ? LINE_GAP + LINE_HEIGHT : 0);
            setHeight(PADDING * 2 + LINE_HEIGHT + LINE_GAP + bodyHeight);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            FontRenderer fontRenderer = FontRenderer.getInstance();
            int bodyX = getX() + PADDING;
            int bodyY = getY() + PADDING + LINE_HEIGHT + LINE_GAP;
            int bodyWidth = this.width - PADDING * 2;

            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics, Texture.MANAGER_TEXT_BOX_BACKGROUND, getX(), getY(), this.width, this.height);

            fontRenderer.renderText(
                    guiGraphics,
                    title,
                    getX() + PADDING,
                    getY() + PADDING,
                    TITLE_COLOR,
                    HorizontalAlignment.LEFT,
                    VerticalAlignment.TOP,
                    TextShadow.NONE);

            int totalTextWidth = 0;
            for (Component stat : stats) {
                totalTextWidth += FONT.width(stat);
            }
            int gap = Math.max(0, (bodyWidth - totalTextWidth) / (stats.size() - 1));

            int statX = bodyX;
            for (Component stat : stats) {
                fontRenderer.renderText(
                        guiGraphics,
                        StyledText.fromComponent(stat),
                        statX,
                        bodyY,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
                statX += FONT.width(stat) + gap;
            }

            if (url != null) {
                CustomColor urlColor = CommonColors.WHITE;

                if (isMouseOverUrl(mouseX, mouseY)) {
                    guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
                    urlColor = CommonColors.AQUA;
                }

                fontRenderer.renderText(
                        guiGraphics,
                        StyledText.fromComponent(url),
                        bodyX + bodyWidth / 2f,
                        bodyY + LINE_HEIGHT + LINE_GAP,
                        urlColor,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
            if (event.button() == InputConstants.MOUSE_BUTTON_LEFT
                    && urlString != null
                    && isMouseOverUrl(event.x(), event.y())) {
                try {
                    Util.getPlatform().openUri(URI.create(urlString));
                } catch (IllegalArgumentException e) {
                    return false;
                }

                playDownSound(McUtils.mc().getSoundManager());
                return true;
            }

            return super.mouseClicked(event, isDoubleClick);
        }

        private boolean isMouseOverUrl(double mouseX, double mouseY) {
            if (url == null) return false;

            int bodyX = getX() + PADDING;
            int bodyY = getY() + PADDING + LINE_HEIGHT + LINE_GAP;
            int bodyWidth = this.width - PADDING * 2;

            int urlWidth = FONT.width(url);
            float centerX = bodyX + bodyWidth / 2f;
            int urlLeft = (int) (centerX - urlWidth / 2f);
            int urlRight = (int) (centerX + urlWidth / 2f);
            int urlTop = bodyY + LINE_HEIGHT + LINE_GAP;
            int urlBottom = urlTop + LINE_HEIGHT;

            return MathUtils.isInside((int) mouseX, (int) mouseY, urlLeft, urlRight, urlTop, urlBottom);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }

    private static final class DescriptionBoxWidget extends AbstractWidget {
        private final StyledText title;

        private StyledText description = null;
        private StyledText[] wrappedDescription = new StyledText[0];

        private DescriptionBoxWidget() {
            super(0, 0, 0, 0, Component.literal("Description Box Widget"));
            this.title = StyledText.fromComponent(Component.translatable(
                            "screens.wynntils.map.managers.providerManager.descriptionScrollBarWidget.description.title")
                    .withStyle(ChatFormatting.BOLD));
            rewrap();
        }

        private void setDescription(StyledText description) {
            this.description = description;
            rewrap();
        }

        @Override
        public void setWidth(int width) {
            if (this.width == width) return;

            super.setWidth(width);
            rewrap();
        }

        private void rewrap() {
            wrappedDescription = description == null
                    ? new StyledText[0]
                    : RenderedStringUtils.wrapTextBySize(description, this.width - PADDING);

            setHeight(PADDING * 2 + LINE_HEIGHT + LINE_GAP + wrappedDescription.length * LINE_HEIGHT);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            FontRenderer fontRenderer = FontRenderer.getInstance();
            int bodyX = getX() + PADDING;
            int bodyY = getY() + PADDING + LINE_HEIGHT + LINE_GAP;

            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics, Texture.MANAGER_TEXT_BOX_BACKGROUND, getX(), getY(), this.width, this.height);

            fontRenderer.renderText(
                    guiGraphics,
                    title,
                    getX() + PADDING,
                    getY() + PADDING,
                    TITLE_COLOR,
                    HorizontalAlignment.LEFT,
                    VerticalAlignment.TOP,
                    TextShadow.NONE);

            for (int i = 0; i < wrappedDescription.length; i++) {
                fontRenderer.renderText(
                        guiGraphics,
                        wrappedDescription[i],
                        bodyX,
                        bodyY + i * LINE_HEIGHT,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }
}
