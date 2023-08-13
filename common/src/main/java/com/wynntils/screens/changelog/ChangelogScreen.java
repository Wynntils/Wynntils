/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.changelog;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.WynntilsPagedScreen;
import com.wynntils.screens.base.widgets.PageSelectorButton;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

public final class ChangelogScreen extends WynntilsScreen implements WynntilsPagedScreen {
    private final String changelog;
    private List<List<TextRenderTask>> changelogTasks;
    private int currentPage = 0;

    private ChangelogScreen(String changelog) {
        super(Component.translatable("screens.wynntils.changelog.name"));

        this.changelog = changelog;
    }

    @Override
    public void onClose() {
        super.onClose();

        // Send this to the server to request a re-sent class menu
        McUtils.sendPacket(new ServerboundContainerClosePacket(0));
    }

    public static Screen create(String changelog) {
        return new ChangelogScreen(changelog);
    }

    @Override
    protected void doInit() {
        calculateRenderTasks();

        setCurrentPage(0);

        this.addRenderableWidget(new PageSelectorButton(
                80 - Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.SCROLL_BACKGROUND.height() - 17,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.SCROLL_BACKGROUND.width() - 80,
                Texture.SCROLL_BACKGROUND.height() - 17,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                true,
                this));
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        poseStack.pushPose();

        poseStack.translate(
                (this.width - Texture.SCROLL_BACKGROUND.width()) / 2f,
                (this.height - Texture.SCROLL_BACKGROUND.height()) / 2f,
                0);

        RenderUtils.drawTexturedRect(poseStack, Texture.SCROLL_BACKGROUND, 0, 0);

        FontRenderer.getInstance().renderTexts(poseStack, 45, 15, changelogTasks.get(currentPage));

        renderPageInfo(poseStack, getCurrentPage() + 1, getMaxPage() + 1);

        super.doRender(poseStack, mouseX, mouseY, partialTick);

        poseStack.popPose();
    }

    private void renderPageInfo(PoseStack poseStack, int currentPage, int maxPage) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString((currentPage) + " / " + (maxPage)),
                        80,
                        Texture.SCROLL_BACKGROUND.width() - 80,
                        Texture.SCROLL_BACKGROUND.height() - 17,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        TextShadow.OUTLINE);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        double adjustedMouseX = mouseX - (this.width - Texture.SCROLL_BACKGROUND.width()) / 2f;
        double adjustedMouseY = mouseY - (this.height - Texture.SCROLL_BACKGROUND.height()) / 2f;

        return super.doMouseClicked(adjustedMouseX, adjustedMouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0) {
            setCurrentPage(getCurrentPage() - 1);
        } else if (delta < 0) {
            setCurrentPage(getCurrentPage() + 1);
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void calculateRenderTasks() {
        TextRenderSetting setting = TextRenderSetting.DEFAULT
                .withMaxWidth(Texture.SCROLL_BACKGROUND.width() - 85)
                .withCustomColor(CommonColors.WHITE)
                .withTextShadow(TextShadow.OUTLINE);

        List<TextRenderTask> textRenderTasks = Arrays.stream(changelog.split("\n"))
                .map(StringUtils::convertMarkdownToColorCode)
                .map(s -> new TextRenderTask(s, setting))
                .toList();

        this.changelogTasks = new ArrayList<>();

        final int maxHeight = Texture.SCROLL_BACKGROUND.height() - 55;

        float currentHeight = 0;
        List<TextRenderTask> currentPage = new ArrayList<>();

        for (TextRenderTask textRenderTask : textRenderTasks) {
            float height = FontRenderer.getInstance().calculateRenderHeight(List.of(textRenderTask));

            if (currentHeight + height > maxHeight) {
                this.changelogTasks.add(currentPage);
                currentPage = new ArrayList<>();
                currentHeight = 0;
            }

            currentPage.add(textRenderTask);
            currentHeight += height;
        }

        this.changelogTasks.add(currentPage);
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void setCurrentPage(int currentPage) {
        this.currentPage = MathUtils.clamp(currentPage, 0, getMaxPage());
    }

    @Override
    public int getMaxPage() {
        return Math.max(0, this.changelogTasks.size() - 1);
    }
}
