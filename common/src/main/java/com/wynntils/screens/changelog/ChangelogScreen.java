/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.changelog;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.WynntilsPagedScreen;
import com.wynntils.screens.base.widgets.PageSelectorButton;
import com.wynntils.screens.changelog.widgets.ExitFlagButton;
import com.wynntils.services.athena.type.ChangelogMap;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ChangelogScreen extends WynntilsScreen implements WynntilsPagedScreen {
    private static final float SCROLL_FACTOR = 10f;
    private static final int SCISSOR_HEIGHT = 165;
    private static final int SCROLLBAR_RENDER_X = 268;
    private static final int SCROLL_AREA_HEIGHT = 120;
    private static final int SCROLLBAR_HEIGHT = 20;
    private static final int SCROLLBAR_WIDTH = 6;

    private final ChangelogMap changelog;
    private final Screen previousScreen;

    private List<List<TextRenderTask>> changelogTasks;
    private boolean draggingScroll = false;
    private int currentPage = 0;
    private int offsetX;
    private int offsetY;
    private int scrollOffset = 0;
    private int scrollRenderY;

    private ChangelogScreen(ChangelogMap changelog, Screen previousScreen) {
        super(Component.translatable("screens.wynntils.changelog.name"));

        this.changelog = changelog;
        this.previousScreen = previousScreen;
    }

    public static Screen create(ChangelogMap changelog, Screen previousScreen) {
        return new ChangelogScreen(changelog, previousScreen);
    }

    public static Screen create(ChangelogMap changelog) {
        return new ChangelogScreen(changelog, null);
    }

    @Override
    protected void doInit() {
        super.doInit();

        offsetX = (int) ((this.width - Texture.SCROLL_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.SCROLL_BACKGROUND.height()) / 2f);

        calculateRenderTasks();

        setCurrentPage(0);

        this.addRenderableWidget(new PageSelectorButton(
                (int) (80 - Texture.FORWARD_ARROW_OFFSET.width() / 2f + offsetX),
                Texture.SCROLL_BACKGROUND.height() - 17 + offsetY,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.SCROLL_BACKGROUND.width() - 80 + offsetX,
                Texture.SCROLL_BACKGROUND.height() - 17 + offsetY,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                true,
                this));

        this.addRenderableWidget(new ExitFlagButton(offsetX - 35, offsetY + 24, this::onClose));
    }

    @Override
    public void onClose() {
        super.onClose();

        if (previousScreen != null) {
            McUtils.setScreen(previousScreen);
        }
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawTexturedRect(poseStack, Texture.SCROLL_BACKGROUND, offsetX, offsetY);

        RenderUtils.enableScissor(guiGraphics, offsetX + 40, offsetY + 11, 220, SCISSOR_HEIGHT);
        FontRenderer.getInstance()
                .renderTexts(poseStack, 45 + offsetX, 15 + offsetY - scrollOffset, changelogTasks.get(currentPage));
        RenderUtils.disableScissor(guiGraphics);

        if (getMaxScrollOffset() != 0) {
            renderScrollBar(poseStack);
        }

        renderPageInfo(poseStack, getCurrentPage() + 1, getMaxPage() + 1);

        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderPageInfo(PoseStack poseStack, int currentPage, int maxPage) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString((currentPage) + " / " + (maxPage)),
                        80 + offsetX,
                        Texture.SCROLL_BACKGROUND.width() - 80 + offsetX,
                        Texture.SCROLL_BACKGROUND.height() - 17 + offsetY,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        TextShadow.OUTLINE);
    }

    private void renderScrollBar(PoseStack poseStack) {
        RenderUtils.drawRect(
                poseStack,
                CommonColors.LIGHT_GRAY,
                offsetX + SCROLLBAR_RENDER_X,
                offsetY + 14,
                0,
                SCROLLBAR_WIDTH,
                SCROLL_AREA_HEIGHT);

        scrollRenderY = (int) (offsetY
                + 14
                + MathUtils.map(scrollOffset, 0, getMaxScrollOffset(), 0, SCROLL_AREA_HEIGHT - SCROLLBAR_HEIGHT));

        RenderUtils.drawRect(
                poseStack,
                draggingScroll ? CommonColors.BLACK : CommonColors.GRAY,
                offsetX + SCROLLBAR_RENDER_X,
                scrollRenderY,
                0,
                SCROLLBAR_WIDTH,
                SCROLLBAR_HEIGHT);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        if (!draggingScroll) {
            if (MathUtils.isInside(
                    (int) mouseX,
                    (int) mouseY,
                    offsetX + SCROLLBAR_RENDER_X,
                    offsetX + SCROLLBAR_RENDER_X + SCROLLBAR_WIDTH,
                    scrollRenderY,
                    scrollRenderY + SCROLLBAR_HEIGHT)) {
                draggingScroll = true;

                return true;
            }
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScroll) {
            int scrollAreaStartY = offsetY + 14 + 10;

            int newOffset = Math.round(MathUtils.map(
                    (float) mouseY,
                    scrollAreaStartY,
                    scrollAreaStartY + SCROLL_AREA_HEIGHT - SCROLLBAR_HEIGHT,
                    0,
                    getMaxScrollOffset()));

            newOffset = Math.max(0, Math.min(newOffset, getMaxScrollOffset()));

            scroll(newOffset);

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);

        int newOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, getMaxScrollOffset()));
        scroll(newOffset);

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private void scroll(int newOffset) {
        scrollOffset = newOffset;
    }

    private int getMaxScrollOffset() {
        float totalHeight = FontRenderer.getInstance().calculateRenderHeight(changelogTasks.get(currentPage));

        if (totalHeight <= SCISSOR_HEIGHT) return 0;

        return (int) (totalHeight - SCISSOR_HEIGHT);
    }

    private void calculateRenderTasks() {
        TextRenderSetting setting = TextRenderSetting.DEFAULT
                .withMaxWidth(Texture.SCROLL_BACKGROUND.width() - 85)
                .withCustomColor(CommonColors.WHITE)
                .withTextShadow(TextShadow.OUTLINE);

        this.changelogTasks = changelog.allChangelogs().stream()
                .map(content -> Arrays.stream(content.split("\n"))
                        .map(StringUtils::convertMarkdownToColorCode)
                        .map(s -> new TextRenderTask(s, setting))
                        .toList())
                .toList();
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void setCurrentPage(int currentPage) {
        scrollOffset = 0;
        this.currentPage = MathUtils.clamp(currentPage, 0, getMaxPage());
    }

    @Override
    public int getMaxPage() {
        return Math.max(0, this.changelogTasks.size() - 1);
    }
}
