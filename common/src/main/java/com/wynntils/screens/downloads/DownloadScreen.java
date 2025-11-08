/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.downloads;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.DownloadDependencyGraph;
import com.wynntils.core.net.QueuedDownload;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.ui.WynncraftButtonFeature;
import com.wynntils.screens.base.WynntilsGridLayoutScreen;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.downloads.widgets.DownloadWidget;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

public final class DownloadScreen extends WynntilsGridLayoutScreen {
    private static final float SCROLL_FACTOR = 10f;
    private static final int WIDGET_TOP_Y = 11;
    private static final int WIDGETS_PER_PAGE = 15;

    private final Screen previousScreen;
    private final ServerData serverData;

    private final Component infoText;
    private List<DownloadWidget> downloadWidgets = new ArrayList<>();
    private int scrollY;
    private int scrollOffset = 0;
    private int widgetHeight;
    private boolean draggingScroll = false;

    private DownloadScreen(Screen previousScreen, ServerData serverData) {
        super(Component.translatable("screens.wynntils.downloads.name"));

        this.previousScreen = previousScreen;
        this.serverData = serverData;

        infoText = Component.translatable("screens.wynntils.downloads.description1")
                .append("\n\n")
                .append(Component.translatable("screens.wynntils.downloads.description2"))
                .append("\n\n")
                .append(Component.translatable("screens.wynntils.downloads.description3"))
                .append("\n\n")
                .append(Component.translatable("screens.wynntils.downloads.description4"));
    }

    public static Screen create(Screen previousScreen, ServerData serverData) {
        return new DownloadScreen(previousScreen, serverData);
    }

    @Override
    protected void doInit() {
        super.doInit();

        this.addRenderableWidget(
                new Button.Builder(Component.translatable("screens.wynntils.downloads.back"), (button -> onClose()))
                        .pos((int) (dividedWidth * 5), (int) (dividedHeight * 37))
                        .size((int) (dividedWidth * 6), 20)
                        .build());

        if (serverData != null) {
            this.addRenderableWidget(new Button.Builder(
                            Component.translatable("screens.wynntils.downloads.connect"), (button -> connectToServer()))
                    .pos((int) dividedWidth, (int) (dividedHeight * 31))
                    .size((int) (dividedWidth * 14), 20)
                    .build());

            this.addRenderableWidget(new WynntilsCheckbox(
                    (int) (dividedWidth * 2),
                    (int) (dividedHeight * 25),
                    20,
                    Component.translatable("screens.wynntils.downloads.dontShowAgain"),
                    false,
                    (int) (dividedWidth * 10),
                    (c, b) -> toggleShowAgain(b)));
        }

        widgetHeight = (int) (dividedHeight * 3.5f);

        updateDownloadWidgets();
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previousScreen);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        PoseStack poseStack = guiGraphics.pose();

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.downloads.name")
                                .withStyle(ChatFormatting.UNDERLINE)),
                        dividedWidth * 32,
                        dividedHeight * 6.5f,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        3f);

        RenderUtils.enableScissor(
                guiGraphics,
                (int) (dividedWidth * 16),
                (int) (dividedHeight * WIDGET_TOP_Y),
                (int) (dividedWidth * 32),
                WIDGETS_PER_PAGE * widgetHeight);

        for (Renderable renderable : downloadWidgets) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        RenderUtils.disableScissor(guiGraphics);

        RenderUtils.drawRect(
                poseStack,
                CommonColors.LIGHT_GRAY,
                (dividedWidth * 48),
                (int) (dividedHeight * WIDGET_TOP_Y),
                0,
                6,
                WIDGETS_PER_PAGE * widgetHeight);

        scrollY = (int) ((dividedHeight * WIDGET_TOP_Y)
                + MathUtils.map(scrollOffset, 0, getMaxScrollOffset(), 0, (WIDGETS_PER_PAGE * widgetHeight) - 20));

        RenderUtils.drawRect(
                poseStack,
                draggingScroll ? CommonColors.BLACK : CommonColors.GRAY,
                (dividedWidth * 48),
                scrollY,
                0,
                6,
                20);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.downloads.info")
                                .withStyle(ChatFormatting.UNDERLINE)),
                        (dividedWidth * 56),
                        (dividedHeight * WIDGET_TOP_Y) - 18,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL,
                        1.5f);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromComponent(infoText),
                        (dividedWidth * 50),
                        (dividedWidth * 63),
                        (dividedHeight * WIDGET_TOP_Y),
                        (dividedHeight * WIDGET_TOP_Y) + WIDGETS_PER_PAGE * widgetHeight,
                        (dividedWidth * 13),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL,
                        0.8f);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        if (!draggingScroll) {
            if (MathUtils.isInside(
                    (int) mouseX,
                    (int) mouseY,
                    (int) (dividedWidth * 48),
                    (int) ((dividedWidth * 48) + 6),
                    scrollY,
                    scrollY + 20)) {
                draggingScroll = true;

                return true;
            }
        }

        for (GuiEventListener listener : this.children) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return listener.mouseClicked(mouseX, mouseY, button);
            }
        }

        for (DownloadWidget downloadWidget : this.downloadWidgets) {
            if (downloadWidget.isMouseOver(mouseX, mouseY)) {
                return downloadWidget.mouseClicked(mouseX, mouseY, button);
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScroll) {
            int scrollAreaStartY = (int) ((dividedHeight * WIDGET_TOP_Y) + 10);
            int scrollAreaHeight = (WIDGETS_PER_PAGE - 1) * widgetHeight;

            int newOffset = Math.round(MathUtils.map(
                    (float) mouseY, scrollAreaStartY, scrollAreaStartY + scrollAreaHeight, 0, getMaxScrollOffset()));

            newOffset = Math.max(0, Math.min(newOffset, getMaxScrollOffset()));

            scroll(newOffset);

            return true;
        }

        for (GuiEventListener listener : this.children) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return listener.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;

        for (GuiEventListener listener : this.children) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return listener.mouseReleased(mouseX, mouseY, button);
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);

        int newOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, getMaxScrollOffset()));
        scroll(newOffset);

        return true;
    }

    private void connectToServer() {
        ConnectScreen.startConnecting(
                this, McUtils.mc(), ServerAddress.parseString(serverData.ip), serverData, false, null);
    }

    private void toggleShowAgain(boolean show) {
        Managers.Feature.getFeatureInstance(WynncraftButtonFeature.class)
                .ignoreFailedDownloads
                .store(show);
    }

    private void scroll(int newOffset) {
        scrollOffset = newOffset;

        for (DownloadWidget widget : downloadWidgets) {
            int newY = (int)
                    ((dividedHeight * WIDGET_TOP_Y) + (downloadWidgets.indexOf(widget) * widgetHeight) - scrollOffset);

            widget.setY(newY);
            widget.visible = newY >= ((dividedHeight * WIDGET_TOP_Y) - widgetHeight)
                    && newY <= ((dividedHeight * WIDGET_TOP_Y) + (WIDGETS_PER_PAGE + 1) * widgetHeight);
        }
    }

    private void updateDownloadWidgets() {
        for (AbstractWidget widget : downloadWidgets) {
            this.removeWidget(widget);
        }

        downloadWidgets = new ArrayList<>();

        int y = (int) (dividedHeight * WIDGET_TOP_Y);

        for (QueuedDownload download : Managers.Download.registeredDownloads()) {
            DownloadDependencyGraph.DownloadDependencyGraphState downloadState = Managers.Download.graphState();
            downloadWidgets.add(new DownloadWidget(
                    (int) (dividedWidth * 16), y, (int) (dividedWidth * 32), widgetHeight, download));
            y += widgetHeight;
        }

        scroll(scrollOffset);
    }

    private int getMaxScrollOffset() {
        return (downloadWidgets.size() - WIDGETS_PER_PAGE) * widgetHeight;
    }
}
