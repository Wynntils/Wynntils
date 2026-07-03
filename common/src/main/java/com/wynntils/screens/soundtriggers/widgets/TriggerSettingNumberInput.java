/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.soundtriggers.widgets;

import com.google.common.collect.Lists;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.soundtriggers.SoundTriggerManagmentScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.soundtriggers.SoundTrigger;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class TriggerSettingNumberInput extends AbstractButton {
    private final StyledText title;
    private final List<Component> tooltip;
    private final Function<SoundTrigger, Integer> functionTemplate;
    private final SoundTriggerManagmentScreen parentScreen;
    private final TextNumberInputBoxWidget textInputBoxWidget;

    private SoundTrigger trigger;

    public TriggerSettingNumberInput(
            int i,
            int j,
            int k,
            int l,
            StyledText title,
            List<Component> tooltip,
            Function<SoundTrigger, Integer> functionTemplate,
            BiConsumer<String, SoundTrigger> onUpdate,
            SoundTriggerManagmentScreen parentScreen,
            SoundTrigger trigger) {
        super(i, j, k, l, Component.empty());
        this.visible = false;
        this.title = title;
        this.tooltip = tooltip;
        this.functionTemplate = functionTemplate;
        this.parentScreen = parentScreen;
        this.textInputBoxWidget = new TextNumberInputBoxWidget(
                i + 2,
                j + l / 2 + 2,
                k - 4,
                l / 2 - 4,
                (s -> {
                    if (this.trigger != null) {
                        onUpdate.accept(s, this.trigger);
                    }
                }),
                parentScreen);
        this.trigger = trigger;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (trigger == null) return;
        parentScreen.setFocusedTextInput(textInputBoxWidget);
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (trigger == null) return;

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        title,
                        getX(),
                        getX() + getWidth(),
                        getY(),
                        getY() + getHeight() / 2f,
                        getWidth(),
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE,
                        1f);
        textInputBoxWidget.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        if (isHovered) {
            guiGraphics.setTooltipForNextFrame(Lists.transform(tooltip, Component::getVisualOrderText), mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    public void setTrigger(SoundTrigger trigger) {
        this.trigger = trigger;
        this.visible = trigger != null;
        if (trigger != null) {
            this.textInputBoxWidget.setTextBoxInput(String.valueOf(functionTemplate.apply(trigger)));
        }
    }
}
