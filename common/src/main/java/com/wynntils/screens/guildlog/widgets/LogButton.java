/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guildlog.widgets;

import com.google.common.collect.Lists;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.guild.type.GuildLogType;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class LogButton extends BasicTexturedButton {
    private final GuildLogType logType;

    public LogButton(int x, int y, Consumer<Integer> onClick, List<Component> tooltip, GuildLogType logType) {
        super(x, y, Texture.LOG_BUTTON.width(), Texture.LOG_BUTTON.height(), Texture.LOG_BUTTON, onClick, tooltip);

        this.logType = logType;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtils.drawTexturedRect(guiGraphics.pose(), logType.getIcon(), getX() + 3, getY() + 3);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString(logType.getDisplayName()),
                        getX() + width - 2,
                        getY() + height / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.RIGHT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (isHovered) {
            McUtils.screen()
                    .setTooltipForNextRenderPass(Lists.transform(getTooltipLines(), Component::getVisualOrderText));
        }
    }
}
