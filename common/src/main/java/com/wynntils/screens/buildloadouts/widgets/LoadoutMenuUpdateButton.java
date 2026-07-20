package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.MenuCategory;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoadoutMenuUpdateButton extends AbstractButton implements TooltipProvider {
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;
    private List<Component> generatedTooltip = new ArrayList<>();
    private UpdateType updateType = UpdateType.BUILD;

    public LoadoutMenuUpdateButton(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 79, 20, Component.literal("Loadout Menu Update Button"));
        this.x = x;
        this.y = y;
        this.parent = parent;
        buildTooltip();
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_BLUE,
                x,
                y,
                this.width,
                this.height);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString("Update"),
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
                updateType = updateType.next();
                buildTooltip();
                return true;
            }
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Loadout loadout = parent.getSelectedLoadout();
            if (loadout == null) return true;

            switch (updateType) {
                case BUILD -> updateFullLoadout(loadout);
                case SKILL_POINTS -> updateSkillPointsOnly(loadout);
                case ASPECTS -> updateAspectsOnly(loadout);
                case ABILITY_TREE -> updateAbilityTreeOnly(loadout);
            }
        }

        return true;
    }

    private void updateFullLoadout(Loadout loadout) {
        List<LoadoutSaveStep> steps = new ArrayList<>();
        steps.add(skillPointsSaveStep(loadout.name()));
        steps.add(abilityTreeSaveStep(loadout.name()));
        steps.add(aspectsSaveStep(loadout.name()));

        runSteps(loadout, steps, 0);
    }

    private void updateSkillPointsOnly(Loadout loadout) {
        runSteps(loadout, List.of(skillPointsSaveStep(loadout.name())), 0);
    }

    private void updateAspectsOnly(Loadout loadout) {
        runSteps(loadout, List.of(aspectsSaveStep(loadout.name())), 0);
    }

    private void updateAbilityTreeOnly(Loadout loadout) {
        runSteps(loadout, List.of(abilityTreeSaveStep(loadout.name())), 0);
    }

    private void runSteps(Loadout loadout, List<LoadoutSaveStep> steps, int index) {
        boolean isLast = index == steps.size() - 1;

        steps.get(index)
                .run(
                        status -> parent.statusWidget.busy(status),
                        error -> parent.statusWidget.error(error),
                        message -> {
                            if (isLast) {
                                parent.statusWidget.completed(message);
                                parent.setSelectedLoadout(Services.loadout.getLoadout(loadout.name()));
                                parent.loadoutScrollListWidget.populateLoadouts();
                            } else {
                                runSteps(loadout, steps, index + 1);
                            }
                        });
    }

    private LoadoutSaveStep skillPointsSaveStep(String name) {
        return (onStatus, onError, onComplete) -> {
            onStatus.accept("Saving skill points...");
            try {
                Models.SkillPoint.saveCurrentBuild(name);
                onComplete.accept("Skill points saved successfully!");
            } catch (Exception e) {
                onError.accept("Failed to save skill points: " + e.getMessage());
            }
        };
    }

    private LoadoutSaveStep abilityTreeSaveStep(String name) {
        return (onStatus, onError, onComplete) -> {
            onStatus.accept("Updating ability tree...");
            Models.AbilityTree.saveCurrentAbilityTree(name, onStatus, onError, onComplete);
        };
    }

    private LoadoutSaveStep aspectsSaveStep(String name) {
        return (onStatus, onError, onComplete) -> {
            onStatus.accept("Updating aspects...");
            Models.Aspect.saveCurrentAspectLoadout(name, onStatus, onError, onComplete);
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
            this.generatedTooltip.add(Component.literal("Update Category")
                    .withStyle(ChatFormatting.GOLD));

            this.generatedTooltip.add(Component.literal("Choose what to update")
                    .withStyle(ChatFormatting.DARK_GRAY));

            this.generatedTooltip.add(Component.empty());

            for (UpdateType type : UpdateType.values()) {
                boolean selected = type == updateType;
                ChatFormatting color = selected ? ChatFormatting.WHITE : ChatFormatting.GRAY;

                Component label = Component.literal(type.getDisplayName()).withStyle(color);

                this.generatedTooltip.add(Component.literal("- ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(label));
            }

            this.generatedTooltip.add(Component.empty());

            this.generatedTooltip.add(Component.empty()
                    .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(Component.literal("Left-Click to update").withStyle(ChatFormatting.GREEN)));

            this.generatedTooltip.add(Component.empty()
                    .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(Component.literal("Right-Click to change category").withStyle(ChatFormatting.GREEN)));
        } else {
            this.generatedTooltip.add(Component.literal("Update " + updateType.getDisplayName())
                    .withStyle(ChatFormatting.GOLD));

            this.generatedTooltip.add(Component.empty());

            this.generatedTooltip.add(Component.empty()
                    .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(Component.literal("Left-Click to update").withStyle(ChatFormatting.GREEN)));
        }
    }

    public void syncUpdateType() {
        if (parent.getCurrentCategory().getLoadoutType() == null) return;

        switch (parent.getCurrentCategory().getLoadoutType()) {
            case LoadoutType.BUILD -> updateType = UpdateType.BUILD;
            case LoadoutType.ABILITY_TREE -> updateType = UpdateType.ABILITY_TREE;
            case LoadoutType.ASPECT -> updateType = UpdateType.ASPECTS;
            case LoadoutType.SKILL_POINT -> updateType = UpdateType.SKILL_POINTS;
        }
        buildTooltip();
    }

    private enum UpdateType {
        BUILD("Build"),
        ABILITY_TREE("Ability Tree"),
        ASPECTS("Aspects"),
        SKILL_POINTS("Skill Points & Items");

        private static final UpdateType[] VALUES = values();

        private final String displayName;

        UpdateType(String displayName) {
            this.displayName = displayName;
        }

        public UpdateType next() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}