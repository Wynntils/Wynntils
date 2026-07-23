package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.services.loadout.type.LoadoutSaveStep;
import com.wynntils.services.loadout.type.LoadoutType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
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
                        !buttonConfirm ?
                                StyledText.fromComponent(Component.translatable("screens.wynntils.buildLoadouts.newLoadoutMenu.makeNewLoadout.create"))
                                : StyledText.fromComponent(Component.translatable("screens.wynntils.buildLoadouts.newLoadoutMenu.makeNewLoadout.confirm")),
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
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());

        String name = parent.newLoadoutInputWidget.getTextBoxInput();

        if (name.isEmpty()) {
            parent.newLoadoutInfoWidget.setText(StyledText.fromComponent(Component.translatable("screens.wynntils.buildLoadouts.newLoadoutMenu.makeNewLoadout.noNameError")), false);
            buttonConfirm = false;
            return true;
        }

        LoadoutType type = parent.getNewLoadoutType();
        if (type == null) {
            parent.newLoadoutInfoWidget.setText(StyledText.fromComponent(Component.translatable("screens.wynntils.buildLoadouts.newLoadoutMenu.makeNewLoadout.noLoadoutTypeError")), false);
            buttonConfirm = false;
            return true;
        }

        if (Services.loadout.hasLoadout(name) && !buttonConfirm) {
            parent.newLoadoutInfoWidget.setText(
                    StyledText.fromComponent(Component.translatable("screens.wynntils.buildLoadouts.newLoadoutMenu.makeNewLoadout.overwriteError")), false);
            buttonConfirm = true;
            return true;
        }

        buttonConfirm = false;
        parent.newLoadoutInputWidget.setTextBoxInput("");

        saveLoadout(name, type);

        return true;
    }

    private void saveLoadout(String name, LoadoutType type) {
        List<LoadoutSaveStep> steps = new ArrayList<>();

        switch (type) {
            case BUILD -> {
                steps.add(skillPointsSaveStep(name));
                steps.add(abilityTreeSaveStep(name));
                steps.add(aspectsSaveStep(name));
            }
            case ABILITY_TREE -> steps.add(abilityTreeSaveStep(name));
            case SKILL_POINT -> steps.add(skillPointsSaveStep(name));
            case ASPECT -> steps.add(aspectsSaveStep(name));
        }

        runSaveSteps(steps, 0, name);
    }

    private void runSaveSteps(List<LoadoutSaveStep> steps, int index, String name) {
        boolean isLast = index == steps.size() - 1;

        steps.get(index)
                .run(
                        status -> parent.statusWidget.busy(status),
                        error -> parent.statusWidget.error(error),
                        message -> {
                            if (isLast) {
                                Loadout loadout = Services.loadout.getLoadout(name);
                                parent.setCurrentCategory(loadout.getMenuCategory());
                                parent.setSelectedLoadout(loadout);
                                parent.loadoutScrollListWidget.scrollOffset = 0;
                                parent.loadoutScrollListWidget.populateLoadouts();
                                parent.statusWidget.completed(message);
                            } else {
                                runSaveSteps(steps, index + 1, name);
                            }
                        });
    }

    private LoadoutSaveStep skillPointsSaveStep(String name) {
        return (onStatus, onError, onComplete) -> {
            onStatus.accept("Saving skill points...");
            try {
                Models.SkillPoint.saveCurrentSkillPointsAndItems(name);
                onComplete.accept("Skill points saved successfully!");
            } catch (Exception e) {
                onError.accept("Failed to save skill points: " + e.getMessage());
            }
        };
    }

    private LoadoutSaveStep abilityTreeSaveStep(String name) {
        return (onStatus, onError, onComplete) -> {
            onStatus.accept("Saving ability tree...");
            Models.AbilityTree.saveCurrentAbilityTree(name, onStatus, onError, onComplete);
        };
    }

    private LoadoutSaveStep aspectsSaveStep(String name) {
        return (onStatus, onError, onComplete) -> {
            onStatus.accept("Saving aspects...");
            Models.Aspect.saveCurrentAspectLoadout(name, onStatus, onError, onComplete);
        };
    }

    public void setButtonConfirm(boolean confirm) {
        this.buttonConfirm = confirm;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}