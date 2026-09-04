/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.soundtriggers.widgets;

import com.google.common.collect.Lists;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.soundtriggers.SoundTrigger;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class TriggerSettingButton extends WynntilsButton {
    private static final CustomColor BACKGROUND_COLOR = new CustomColor(98, 34, 8);
    private static final CustomColor HOVER_BACKGROUND_COLOR = new CustomColor(158, 52, 16);

    private final StyledText title;
    private final List<FormattedCharSequence> tooltip;
    private final Function<SoundTrigger, StyledText> getDisplayValue;
    private final Consumer<SoundTrigger> onClick;

    private SoundTrigger trigger;

    public TriggerSettingButton(
            int x,
            int y,
            int width,
            int height,
            Component title,
            List<Component> tooltip,
            Function<SoundTrigger, StyledText> getDisplayValue,
            Consumer<SoundTrigger> onClick,
            SoundTrigger trigger) {
        super(x, y, width, height, Component.empty());
        this.visible = trigger != null;

        this.title = StyledText.fromComponent(title);
        this.tooltip = Lists.transform(ComponentUtils.wrapTooltips(tooltip, 175), Component::getVisualOrderText);
        this.getDisplayValue = getDisplayValue;
        this.onClick = onClick;
        this.trigger = trigger;
    }

    public TriggerSettingButton(
            int x,
            int y,
            int width,
            int height,
            Component title,
            Component tooltip,
            Function<SoundTrigger, StyledText> getDisplayValue,
            Consumer<SoundTrigger> onClick,
            SoundTrigger trigger) {
        this(x, y, width, height, title, List.of(tooltip), getDisplayValue, onClick, trigger);
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        if (trigger == null) return;

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        title,
                        this.getX(),
                        this.getX() + this.getWidth(),
                        this.getY(),
                        this.getY() + this.getHeight() / 2f,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE,
                        1f);

        RenderUtils.drawRoundedRectWithBorder(
                guiGraphics,
                CommonColors.BLACK,
                isHovered ? HOVER_BACKGROUND_COLOR : BACKGROUND_COLOR,
                this.getX(),
                this.getY() + this.height / 2f,
                this.width,
                this.height / 2f,
                1,
                3,
                3);

        FontRenderer.getInstance()
                .renderScrollingAlignedTextInBox(
                        guiGraphics,
                        getDisplayValue.apply(trigger),
                        this.getX(),
                        this.getX() + this.width,
                        this.getY() + this.height / 2f,
                        this.getY() + this.height,
                        this.width - 2,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);

        if (isHovered) {
            guiGraphics.setTooltipForNextFrame(tooltip, i, j);
        }
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (trigger == null) return;
        onClick.accept(trigger);
    }

    public void setTrigger(SoundTrigger trigger) {
        this.trigger = trigger;
        this.visible = trigger != null;
    }
}
