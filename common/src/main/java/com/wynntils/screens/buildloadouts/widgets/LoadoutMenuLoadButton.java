/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.MenuCategory;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.services.loadout.type.LoadoutLoadStep;
import com.wynntils.services.loadout.type.LoadoutType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class LoadoutMenuLoadButton extends AbstractButton implements TooltipProvider {
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;
    private List<Component> generatedTooltip = new ArrayList<>();
    private LoadType loadType = LoadType.BUILD;

    public LoadoutMenuLoadButton(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 79, 20, Component.literal("Loadout Menu Load Button"));
        this.x = x;
        this.y = y;
        this.parent = parent;
        buildTooltip();
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_GREEN, x, y, this.width, this.height);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.buildLoadouts.loadoutMenu.loadButton.text")),
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

        if (parent.getCurrentCategory() == MenuCategory.BUILD_LOADOUT) {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                loadType = loadType.next();
                buildTooltip();
                return true;
            }
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Loadout loadout = parent.getSelectedLoadout();
            if (loadout == null) return true;

            switch (loadType) {
                case BUILD -> loadFullLoadout(loadout);
                case SKILL_POINTS -> loadSkillPointsOnly(loadout);
                case ASPECTS -> loadAspectsOnly(loadout);
                case ABILITY_TREE -> loadAbilityTreeOnly(loadout);
            }
        }

        return true;
    }

    private void loadFullLoadout(Loadout loadout) {
        List<LoadoutLoadStep> steps = new ArrayList<>();
        steps.add(skillPointsStep(loadout));

        if (loadout.hasAspects()) {
            steps.add(aspectsStep(loadout));
        }
        if (loadout.hasAbilityTree()) {
            steps.add(abilityTreeStep(loadout));
        }

        runSteps(steps, 0);
    }

    private void loadSkillPointsOnly(Loadout loadout) {
        runSteps(List.of(skillPointsStep(loadout)), 0);
    }

    private void loadAspectsOnly(Loadout loadout) {
        runSteps(List.of(aspectsStep(loadout)), 0);
    }

    private void loadAbilityTreeOnly(Loadout loadout) {
        runSteps(List.of(abilityTreeStep(loadout)), 0);
    }

    private void runSteps(List<LoadoutLoadStep> steps, int index) {
        boolean isLast = index == steps.size() - 1;

        steps.get(index)
                .run(status -> parent.statusWidget.busy(status), error -> parent.statusWidget.error(error), message -> {
                    if (isLast) {
                        parent.statusWidget.completed(message);
                    } else {
                        runSteps(steps, index + 1);
                    }
                });
    }

    private LoadoutLoadStep skillPointsStep(Loadout loadout) {
        return (onStatus, onError, onComplete) -> {
            onStatus.accept("Loading skill points...");
            Models.SkillPoint.loadLoadout(
                    loadout.name(), onError, () -> onComplete.accept("Skill points loaded successfully!"));
        };
    }

    private LoadoutLoadStep aspectsStep(Loadout loadout) {
        return (onStatus, onError, onComplete) -> {
            onStatus.accept("Applying aspects...");
            Models.Aspect.loadAspectLoadout(loadout.name(), onStatus, onError, onComplete);
        };
    }

    private LoadoutLoadStep abilityTreeStep(Loadout loadout) {
        return (onStatus, onError, onComplete) -> {
            onStatus.accept("Applying ability tree...");
            Models.AbilityTree.loadAbilityTree(loadout.name(), onStatus, onError, onComplete);
        };
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return Collections.unmodifiableList(this.generatedTooltip);
    }

    private void buildTooltip() {
        this.generatedTooltip = new ArrayList<>();

        if (parent.getCurrentCategory() == MenuCategory.BUILD_LOADOUT) {
            this.generatedTooltip.add(Component.literal("Load Category").withStyle(ChatFormatting.GOLD));

            this.generatedTooltip.add(Component.literal("Choose what to load").withStyle(ChatFormatting.DARK_GRAY));

            this.generatedTooltip.add(Component.empty());

            for (LoadType type : LoadType.values()) {
                boolean selected = type == loadType;
                ChatFormatting color = selected ? ChatFormatting.WHITE : ChatFormatting.GRAY;

                Component label = Component.literal(type.getDisplayName()).withStyle(color);

                this.generatedTooltip.add(
                        Component.literal("- ").withStyle(ChatFormatting.GOLD).append(label));
            }

            this.generatedTooltip.add(Component.empty());

            this.generatedTooltip.add(Component.empty()
                    .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(Component.literal("Left-Click to load").withStyle(ChatFormatting.GREEN)));

            this.generatedTooltip.add(Component.empty()
                    .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(Component.literal("Right-Click to change category").withStyle(ChatFormatting.GREEN)));
        } else {
            this.generatedTooltip.add(
                    Component.literal("Load " + loadType.getDisplayName()).withStyle(ChatFormatting.GOLD));

            this.generatedTooltip.add(Component.empty());

            this.generatedTooltip.add(Component.empty()
                    .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(Component.literal("Left-Click to load").withStyle(ChatFormatting.GREEN)));
        }
    }

    public void syncLoadType() {
        if (parent.getCurrentCategory().getLoadoutType() == null) return;

        switch (parent.getCurrentCategory().getLoadoutType()) {
            case LoadoutType.BUILD -> loadType = LoadType.BUILD;
            case LoadoutType.ABILITY_TREE -> loadType = LoadType.ABILITY_TREE;
            case LoadoutType.ASPECT -> loadType = LoadType.ASPECTS;
            case LoadoutType.SKILL_POINT -> loadType = LoadType.SKILL_POINTS;
        }
        buildTooltip();
    }

    private enum LoadType {
        BUILD("Build"),
        ABILITY_TREE("Ability Tree"),
        ASPECTS("Aspects"),
        SKILL_POINTS("Skill Points");

        private static final LoadType[] VALUES = values();

        private final String displayName;

        LoadType(String displayName) {
            this.displayName = displayName;
        }

        public LoadType next() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
