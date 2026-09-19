/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.soundtriggers.widgets;

import com.google.common.collect.Lists;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.soundtriggers.SoundTriggerManagmentScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
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
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class TriggerSettingNumberInput extends AbstractButton {
    private final StyledText title;
    private final List<FormattedCharSequence> tooltip;
    private final Function<SoundTrigger, Integer> functionTemplate;
    private final SoundTriggerManagmentScreen parentScreen;
    private final TextNumberInputBoxWidget textInputBoxWidget;

    private SoundTrigger trigger;

    public TriggerSettingNumberInput(
            int i,
            int j,
            int k,
            int l,
            Component title,
            List<Component> tooltip,
            Function<SoundTrigger, Integer> functionTemplate,
            BiConsumer<String, SoundTrigger> onUpdate,
            SoundTriggerManagmentScreen parentScreen,
            SoundTrigger trigger) {
        super(i, j, k, l, Component.empty());
        this.visible = trigger != null;
        this.title = StyledText.fromComponent(title);
        this.tooltip = Lists.transform(ComponentUtils.wrapTooltips(tooltip, 175), Component::getVisualOrderText);

        this.functionTemplate = functionTemplate;
        this.parentScreen = parentScreen;
        this.textInputBoxWidget = new TextNumberInputBoxWidget(
                i + 2,
                j + l / 2,
                k - 4,
                l / 2 - 2,
                (s -> {
                    if (this.trigger != null) {
                        onUpdate.accept(s, this.trigger);
                    }
                }),
                parentScreen);
        this.textInputBoxWidget.visible = trigger != null;
        this.trigger = trigger;
    }

    public TriggerSettingNumberInput(
            int i,
            int j,
            int k,
            int l,
            Component title,
            Component tooltip,
            Function<SoundTrigger, Integer> functionTemplate,
            BiConsumer<String, SoundTrigger> onUpdate,
            SoundTriggerManagmentScreen parentScreen,
            SoundTrigger trigger) {
        this(i, j, k, l, title, List.of(tooltip), functionTemplate, onUpdate, parentScreen, trigger);
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
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE,
                        1f);
        textInputBoxWidget.render(guiGraphics, mouseX, mouseY, partialTick);

        if (isHovered) {
            guiGraphics.setTooltipForNextFrame(tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (textInputBoxWidget.isHovered()) {
            return textInputBoxWidget.mouseClicked(event, isDoubleClick);
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (textInputBoxWidget.isHovered()) {
            return textInputBoxWidget.mouseReleased(event);
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        if (textInputBoxWidget.isHovered()) {
            return textInputBoxWidget.mouseDragged(event, mouseX, mouseY);
        }
        return super.mouseDragged(event, mouseX, mouseY);
    }

    public void setTrigger(SoundTrigger trigger) {
        this.trigger = trigger;
        this.visible = trigger != null;
        textInputBoxWidget.visible = trigger != null;
        if (trigger != null) {
            this.textInputBoxWidget.setTextBoxInput(String.valueOf(functionTemplate.apply(trigger)));
        }
    }
}
