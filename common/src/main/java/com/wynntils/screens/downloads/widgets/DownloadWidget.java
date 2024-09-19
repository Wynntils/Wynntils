/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.downloads.widgets;

import com.google.common.collect.Lists;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.DownloadDependencyGraph;
import com.wynntils.core.net.QueuedDownload;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class DownloadWidget extends AbstractWidget {
    private static final List<Component> DOWNLOAD_TOOLTIP =
            List.of(Component.translatable("screens.wynntils.downloads.tryRedownload"));

    private final String downloadName;

    private float offset = 0f;

    public DownloadWidget(int x, int y, int width, int height, QueuedDownload download) {
        super(x, y, width, height, Component.literal("Download Widget"));

        downloadName = EnumUtils.toNiceString(download.urlId());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        DownloadDependencyGraph.DownloadDependencyGraphState downloadState = Managers.Download.graphState();

        RenderUtils.drawRect(
                guiGraphics.pose(),
                CommonColors.GRAY.withAlpha(isHovered ? 200 : 100),
                getX(),
                getY(),
                0,
                width,
                height);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString(downloadName),
                        getX() + width / 2f,
                        getY() + height / 2f,
                        downloadState.successful() ? CommonColors.GREEN : CommonColors.RED,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (!downloadState.finished()) {
            offset = (float) ((offset + 0.1f) % (2 * Math.PI));
            int outerRadius = (int) (height * 0.5f);
            float arcY = getY() + (height / 2f) - outerRadius;
            int innerRadius = (int) (outerRadius * 0.85f);

            RenderUtils.drawArc(
                    guiGraphics.pose(), CommonColors.BLACK, getX(), arcY, 0, 0.8f, innerRadius, outerRadius, offset);
        }

        if (isHovered && !downloadState.successful()) {
            McUtils.mc()
                    .screen
                    .setTooltipForNextRenderPass(Lists.transform(DOWNLOAD_TOOLTIP, Component::getVisualOrderText));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Managers.Download.graphState().successful()) return false;

        // This reloads all URLs, and will then trigger a re-download
        // in both DownloadManager and dynamically downloaded data (CoreComponent#reloadData)
        Managers.Url.loadUrls();

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
