/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.soundtriggers.widgets;

import com.google.common.collect.Lists;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.soundtriggers.SoundTriggerManagmentScreen;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
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

public class TriggerSettingFunctionInput extends AbstractButton {
    private static final Texture NO_ERRORS = Texture.CHECKMARK_GREEN;
    private static final Texture ERRORS = Texture.WARNING;

    private final StyledText title;
    private final List<Component> tooltip;
    private final Function<SoundTrigger, String> functionTemplate;
    private final Function<SoundTrigger, Boolean> functionCheck;
    private final SoundTriggerManagmentScreen parentScreen;
    private final TextInputBoxWidget textInputBoxWidget;

    private SoundTrigger trigger;

    public TriggerSettingFunctionInput(
            int i,
            int j,
            int k,
            int l,
            StyledText title,
            List<Component> tooltip,
            Function<SoundTrigger, String> functionTemplate,
            BiConsumer<String, SoundTrigger> onUpdate,
            Function<SoundTrigger, Boolean> functionCheck,
            SoundTriggerManagmentScreen parentScreen,
            SoundTrigger trigger) {
        super(i, j, k, l, Component.empty());
        this.visible = false;

        this.title = title;
        this.tooltip = tooltip;
        this.functionTemplate = functionTemplate;
        this.functionCheck = functionCheck;
        this.parentScreen = parentScreen;
        this.textInputBoxWidget = new TextInputBoxWidget(
                i + 2,
                j + l / 2 + 2,
                k - 4,
                l / 2 - 4,
                (s -> {
                    if (this.trigger != null) {
                        onUpdate.accept(s, this.trigger);
                        WynntilsMod.error("triggered");
                    }
                    ;
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
                        getY() + getHeight() / 2,
                        getWidth(),
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE,
                        1f);
        Texture tex = functionCheck.apply(trigger) ? ERRORS : NO_ERRORS;
        float texX = getX() + McUtils.mc().font.width(title.getComponent()) + 3;
        RenderUtils.drawTexturedRect(
                guiGraphics,
                tex,
                CommonColors.WHITE,
                texX,
                (float) getY(),
                tex.width() / 2f,
                tex.height() / 2f,
                0,
                0,
                tex.width(),
                tex.height(),
                tex.width(),
                tex.height());

        textInputBoxWidget.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        if (functionCheck.apply(trigger)
                && MathUtils.isInside(
                        mouseX,
                        mouseY,
                        (int) texX,
                        (int) (texX) + tex.width() / 2,
                        getY(),
                        getY() + tex.height() / 2)) {
            guiGraphics.setTooltipForNextFrame(
                    Lists.transform(
                            List.of(
                                    Component.literal("Found Error in this function:"),
                                    Component.empty(),
                                    Component.literal(trigger.getControllerFunctionResult()
                                            .getError())),
                            Component::getVisualOrderText),
                    mouseX,
                    mouseY);
        } else if (isHovered) {
            guiGraphics.setTooltipForNextFrame(Lists.transform(tooltip, Component::getVisualOrderText), mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    public void setTrigger(SoundTrigger trigger) {
        this.trigger = trigger;
        this.visible = trigger != null;
        if (trigger != null) {
            this.textInputBoxWidget.setTextBoxInput(functionTemplate.apply(trigger));
        }
    }
}
