/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.services.loadout.type.LoadoutType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class MakeNewLoadoutButton extends AbstractButton {
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;
    private boolean buttonConfirm = false;

    public MakeNewLoadoutButton(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 133 - 10, 20, Component.literal("Make New Loadout Button"));
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                !buttonConfirm ? Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_GREEN : Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_RED,
                x,
                y,
                this.width,
                this.height);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            !buttonConfirm ? StyledText.fromString("Create") : StyledText.fromString("Confirm"),
                            (this.x + this.width / 2f),
                            (this.y + this.height / 2f),
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
    }

    @Override
    public void onPress(InputWithModifiers input) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());

        String name = parent.newLoadoutInputWidget.getTextBoxInput();

        if (name.isEmpty()) {
            parent.newLoadoutInfoWidget.setText(StyledText.fromString("Please enter a name."), false);
            buttonConfirm = false;
            return true;
        }

        if (parent.getNewLoadoutType() == null) {
            parent.newLoadoutInfoWidget.setText(StyledText.fromString("Please select a loadout type."), false);
            buttonConfirm = false;
            return true;
        }

        if (Services.loadout.hasLoadout(name) && !buttonConfirm) {
            parent.newLoadoutInfoWidget.setText(StyledText.fromString("This will overwrite an existing loadout by the same name."), false);
            buttonConfirm = true;
        } else {
            buttonConfirm = false;
            parent.newLoadoutInputWidget.setTextBoxInput("");

            if (parent.getNewLoadoutType() == LoadoutType.BUILD) {
                //save skillpoints
                Models.SkillPoint.saveCurrentBuild(name);

                //save ability tree
                Models.AbilityTree.saveCurrentAbilityTree(
                        name,
                        status -> parent.statusWidget.setStatus(status, parent.busyColor),
                        error -> parent.statusWidget.setStatus(error, parent.errorColor),
                        completed -> {

                            //save aspects
                            Models.Aspect.saveCurrentAspectLoadout(
                                    name,
                                    status -> parent.statusWidget.setStatus(status, parent.busyColor),
                                    error -> parent.statusWidget.setStatus(error, parent.errorColor),
                                    done -> {
                                        parent.statusWidget.setStatus(done, parent.completedColor);
                                        parent.loadoutScrollListWidget.populateLoadouts();
                                        //this.setSelectedLoadout(this.getLoadout(name));
                                    });
                        });

                return true;
            }

            if (parent.getNewLoadoutType() == LoadoutType.ABILITY_TREE) {
                Models.AbilityTree.saveCurrentAbilityTree(
                    name,
                    status -> parent.statusWidget.setStatus(status, parent.busyColor),
                    error -> parent.statusWidget.setStatus(error, parent.errorColor),
                    completed -> {
                        parent.statusWidget.setStatus(completed, parent.completedColor);
                        parent.loadoutScrollListWidget.populateLoadouts();
                        //this.setSelectedLoadout(this.getLoadout(name));
                    });
                return true;
            }

            if (parent.getNewLoadoutType() == LoadoutType.SKILL_POINT) {
                Models.SkillPoint.saveCurrentBuild(name);
                parent.loadoutScrollListWidget.populateLoadouts();
                return true;
            }

            if (parent.getNewLoadoutType() == LoadoutType.ASPECT) {
                Models.Aspect.saveCurrentAspectLoadout(
                    name,
                    status -> parent.statusWidget.setStatus(status, parent.busyColor),
                    error -> parent.statusWidget.setStatus(error, parent.errorColor),
                    completed -> {
                        parent.statusWidget.setStatus(completed, parent.completedColor);
                        parent.loadoutScrollListWidget.populateLoadouts();
                        //this.setSelectedLoadout(this.getLoadout(name));
                    });
                return true;
            }



        }
        return true;
    }

    public void setButtonConfirm(boolean confirm) {
        this.buttonConfirm = confirm;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
