/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guildlog;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.wrappedscreen.WrappedScreen;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.models.guild.type.GuildLogType;
import com.wynntils.models.items.items.gui.GuildLogItem;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.screens.guildlog.widgets.LogButton;
import com.wynntils.screens.guildlog.widgets.LogDateWidget;
import com.wynntils.screens.guildlog.widgets.LogEntryWidget;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.wynn.ContainerUtils;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public class GuildLogScreen extends WynntilsScreen implements WrappedScreen {
    // Constants
    private static final float SCROLL_FACTOR = 10f;
    private static final int SCROLL_AREA_HEIGHT = 125;

    // Collections
    private List<AbstractWidget> logs = new ArrayList<>();

    // UI size, positions, etc
    private boolean draggingScroll = false;
    private float scrollY;
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private int offsetX;
    private int offsetY;

    // WrappedScreen fields
    private final WrappedScreenInfo wrappedScreenInfo;
    private final GuildLogHolder holder;

    public GuildLogScreen(WrappedScreenInfo wrappedScreenInfo, GuildLogHolder holder) {
        super(Component.literal("Guild Log"));
        this.wrappedScreenInfo = wrappedScreenInfo;
        this.holder = holder;
    }

    @Override
    protected void doInit() {
        offsetX = (this.width - Texture.GUILD_LOG_BACKGROUND.width()) / 2;
        offsetY = (this.height - Texture.GUILD_LOG_BACKGROUND.height()) / 2;

        this.addRenderableWidget(new BasicTexturedButton(
                offsetX + 4,
                offsetY + 2,
                Texture.LOG_BACK.width(),
                Texture.LOG_BACK.height(),
                Texture.LOG_BACK,
                (button) -> ContainerUtils.clickOnSlot(
                        holder.BACK_BUTTON_SLOT,
                        wrappedScreenInfo.containerId(),
                        button,
                        wrappedScreenInfo.containerMenu().getItems()),
                List.of(Component.translatable("gui.back").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD))));

        for (GuildLogType logType : GuildLogType.values()) {
            this.addRenderableWidget(new LogButton(
                    offsetX + 9,
                    offsetY + 17 + (logType.ordinal() * 23),
                    (button) -> ContainerUtils.clickOnSlot(
                            holder.LOG_SLOTS_MAP.get(logType),
                            wrappedScreenInfo.containerId(),
                            button,
                            wrappedScreenInfo.containerMenu().getItems()),
                    ComponentUtils.wrapTooltips(
                            List.of(
                                    Component.literal(logType.getDisplayName())
                                            .withStyle(logType.getColor(), ChatFormatting.BOLD),
                                    Component.translatable(logType.getTooltipKey())
                                            .withStyle(ChatFormatting.GRAY)),
                            150),
                    logType));
        }
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtils.drawTexturedRect(poseStack, Texture.GUILD_LOG_BACKGROUND, offsetX, offsetY);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(wrappedScreenInfo.screen().getTitle()),
                        offsetX + 20,
                        offsetY + 10,
                        CommonColors.TITLE_GRAY,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE);

        renderLogs(guiGraphics, mouseX, mouseY, partialTick);

        renderScroll(poseStack);

        renderables.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        if (!draggingScroll) {
            if (MathUtils.isInside(
                    (int) mouseX,
                    (int) mouseY,
                    offsetX + 393,
                    offsetX + 393 + Texture.SCROLL_BUTTON.width(),
                    (int) scrollY,
                    (int) (scrollY + Texture.SCROLL_BUTTON.height()))) {
                draggingScroll = true;

                return true;
            }
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScroll) {
            int scrollAreaStartY = offsetY + 15 + 17;
            int scrollAreaHeight = SCROLL_AREA_HEIGHT - Texture.SCROLL_BUTTON.height();

            int newOffset = Math.round(MathUtils.map(
                    (float) mouseY, scrollAreaStartY, scrollAreaStartY + scrollAreaHeight, 0, maxScrollOffset));

            newOffset = Math.max(0, Math.min(newOffset, maxScrollOffset));

            scroll(newOffset);

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);
        int newOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, maxScrollOffset));
        scroll(newOffset);

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    public void updateLogItems() {
        logs = new ArrayList<>();
        maxScrollOffset = 0;

        LocalDate currentDate = null;

        int renderX = offsetX + 108;
        int renderY = offsetY + 17;

        for (GuildLogItem logItem : holder.guildLogItems) {
            Instant logInstant = logItem.getLogInstant();
            LocalDate logDate = logInstant.atZone(ZoneId.systemDefault()).toLocalDate();

            if (currentDate == null || !logDate.isEqual(currentDate)) {
                currentDate = logDate;
                LogDateWidget dateWidget = new LogDateWidget(renderX, renderY, logInstant);
                logs.add(dateWidget);

                dateWidget.visible = renderY <= offsetY + 17 + 139;

                renderY += 21;
                maxScrollOffset += 21;
            }

            int widgetHeight = Texture.LOG_ENTRY_TOP.height()
                    + Texture.LOG_ENTRY_BOTTOM.height()
                    + ((logItem.getLogInfo().size() + 1) * Texture.LOG_ENTRY_MIDDLE.height());

            LogEntryWidget entryWidget =
                    new LogEntryWidget(renderX, renderY, widgetHeight, logItem.getLogInfo(), logInstant);
            logs.add(entryWidget);

            entryWidget.visible = renderY <= offsetY + 17 + 139;

            renderY += widgetHeight + 1;
            maxScrollOffset += widgetHeight + 1;
        }

        maxScrollOffset -= SCROLL_AREA_HEIGHT - 4 + Texture.SCROLL_BUTTON.height();
    }

    private void scroll(int newOffset) {
        scrollOffset = newOffset;
        int currentY = offsetY + 17;

        for (AbstractWidget widget : logs) {
            int newY = currentY - scrollOffset;
            widget.setY(newY);
            widget.visible = (newY <= offsetY + 17 + 139) && (newY + widget.getHeight() >= offsetY + 17);
            currentY += widget.getHeight() + 1;
        }
    }

    private void renderLogs(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.enableScissor(guiGraphics, offsetX + 108, offsetY + 16, Texture.LOG_ENTRY_MIDDLE.width(), 139);
        logs.forEach(log -> log.render(guiGraphics, mouseX, mouseY, partialTick));
        RenderUtils.disableScissor(guiGraphics);
    }

    private void renderScroll(PoseStack poseStack) {
        scrollY =
                offsetY + 15 + MathUtils.map(scrollOffset, 0, maxScrollOffset, 0, 141 - Texture.SCROLL_BUTTON.height());

        RenderUtils.drawTexturedRect(poseStack, Texture.SCROLL_BUTTON, offsetX + 393, scrollY);
    }

    @Override
    public WrappedScreenInfo getWrappedScreenInfo() {
        return wrappedScreenInfo;
    }
}
