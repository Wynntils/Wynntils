/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.character.type.SavableSkillPointSet;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.screens.base.WynntilsGridLayoutScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.buildloadouts.type.Loadout;
import com.wynntils.screens.buildloadouts.type.LoadoutType;
import com.wynntils.screens.buildloadouts.widgets.ConvertButton;
import com.wynntils.screens.buildloadouts.widgets.DeleteButton;
import com.wynntils.screens.buildloadouts.widgets.LoadButton;
import com.wynntils.screens.buildloadouts.widgets.LoadoutWidget;
import com.wynntils.screens.buildloadouts.widgets.SaveButton;
import com.wynntils.screens.buildloadouts.widgets.ScrollBar;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class BuildLoadoutsScreen extends WynntilsGridLayoutScreen {
    private static final int MAX_LOADOUTS_PER_PAGE = 11;
    private List<LoadoutWidget> loadoutWidgets = new ArrayList<>();

    private boolean firstInit = true;
    private final List<Pair<Supplier<String>, Function<Skill, Integer>>> summaryParts = new ArrayList<>();

    private SaveButton saveBuildButton;
    private SaveButton saveSkillPointsButton;
    private SaveButton saveAbilityTreeButton;
    private SaveButton saveAspectsButton;
    public TextInputBoxWidget saveNameInput;
    public boolean hasSaveNameConflict = false;

    private Loadout selectedLoadout;
    private WynntilsButton loadButton;
    private WynntilsButton deleteButton;
    private WynntilsButton convertButton;

    private ScrollBar scrollBar;
    private float scrollPercent = 0;

    private String statusMessage = "";
    private CustomColor statusColor = CommonColors.WHITE;

    private BuildLoadoutsScreen() {
        super(Component.literal("Build Loadouts Screen"));
    }

    public static Screen create() {
        return new BuildLoadoutsScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();
        if (firstInit) {
            firstInit = false;

            summaryParts.add(Pair.of(
                    () -> (Models.SkillPoint.hasIllegalAssigned() ? ChatFormatting.RED : "")
                            + I18n.get("screens.wynntils.buildLoadouts.assigned", Models.SkillPoint.getAssignedSum()),
                    Models.SkillPoint::getAssignedSkillPoints));
            summaryParts.add(Pair.of(
                    () -> I18n.get("screens.wynntils.buildLoadouts.gear", Models.SkillPoint.getGearSum()),
                    Models.SkillPoint::getGearSkillPoints));
            summaryParts.add(Pair.of(
                    () -> I18n.get("screens.wynntils.buildLoadouts.setBonus", Models.SkillPoint.getSetBonusSum()),
                    Models.SkillPoint::getSetBonusSkillPoints));
            summaryParts.add(Pair.of(
                    () -> I18n.get("screens.wynntils.buildLoadouts.tomes", Models.SkillPoint.getTomeSum()),
                    Models.SkillPoint::getTomeSkillPoints));
            summaryParts.add(Pair.of(
                    () -> I18n.get("screens.wynntils.buildLoadouts.crafted", Models.SkillPoint.getCraftedSum()),
                    Models.SkillPoint::getCraftedSkillPoints));
            summaryParts.add(Pair.of(
                    () -> I18n.get(
                            "screens.wynntils.buildLoadouts.statusEffects", Models.SkillPoint.getStatusEffectsSum()),
                    Models.SkillPoint::getStatusEffectSkillPoints));
            summaryParts.add(Pair.of(
                    () -> I18n.get("screens.wynntils.buildLoadouts.total", Models.SkillPoint.getTotalSum()),
                    Models.SkillPoint::getTotalSkillPoints));

            Models.SkillPoint.populateSkillPoints();
        }

        populateLoadouts();

        // region Widget initialization
        saveNameInput = new TextInputBoxWidget(
                (int) (dividedWidth * 35),
                (int) (dividedHeight * 19),
                (int) ((dividedWidth * 48) - (dividedWidth * 35)),
                BUTTON_SIZE,
                (x) -> {
                    saveBuildButton.active = !x.isBlank();
                    saveSkillPointsButton.active = !x.isBlank();
                    saveAbilityTreeButton.active = !x.isBlank();
                    saveAspectsButton.active = !x.isBlank();
                    hasSaveNameConflict = false;
                    resetSaveButtons();
                },
                this,
                saveNameInput);
        this.addRenderableWidget(saveNameInput);

        saveBuildButton = new SaveButton(
                (int) (dividedWidth * 35),
                (int) (dividedHeight * 23),
                (int) ((dividedWidth * 43) - (dividedWidth * 35)),
                BUTTON_SIZE,
                Component.translatable("screens.wynntils.buildLoadouts.saveBuild"),
                this,
                (String name) -> {
                    Models.SkillPoint.saveCurrentBuild(name);
                    Models.AbilityTree.saveCurrentAbilityTree(
                            name,
                            status -> this.setStatus(status, CommonColors.YELLOW),
                            error -> this.setStatus(error, CommonColors.RED),
                            completed -> {
                                this.setStatus(completed, CommonColors.GREEN);
                                this.populateLoadouts();
                                this.setSelectedLoadout(this.getLoadout(name));
                            });
                });
        this.addRenderableWidget(saveBuildButton);

        saveSkillPointsButton = new SaveButton(
                (int) (dividedWidth * 44),
                (int) (dividedHeight * 23),
                (int) ((dividedWidth * 52) - (dividedWidth * 44)),
                BUTTON_SIZE,
                Component.translatable("screens.wynntils.buildLoadouts.save"),
                this,
                Models.SkillPoint::saveCurrentSkillPoints);
        this.addRenderableWidget(saveSkillPointsButton);

        saveAbilityTreeButton = new SaveButton(
                (int) (dividedWidth * 35),
                (int) (dividedHeight * 27),
                (int) ((dividedWidth * 43) - (dividedWidth * 35)),
                BUTTON_SIZE,
                Component.translatable("screens.wynntils.buildLoadouts.saveAbilityTree"),
                this,
                name -> Models.AbilityTree.saveCurrentAbilityTree(
                        name,
                        status -> this.setStatus(status, CommonColors.YELLOW),
                        error -> this.setStatus(error, CommonColors.RED),
                        completed -> {
                            this.setStatus(completed, CommonColors.GREEN);
                            this.populateLoadouts();
                            this.setSelectedLoadout(this.getLoadout(name));
                        }));
        this.addRenderableWidget(saveAbilityTreeButton);

        saveAspectsButton = new SaveButton(
                (int) (dividedWidth * 44),
                (int) (dividedHeight * 27),
                (int) ((dividedWidth * 52) - (dividedWidth * 44)),
                BUTTON_SIZE,
                Component.translatable("screens.wynntils.buildLoadouts.saveAspects"),
                this,
                name -> Models.AbilityTree.saveCurrentAbilityTree(
                        name,
                        status -> this.setStatus(status, CommonColors.YELLOW),
                        error -> this.setStatus(error, CommonColors.RED),
                        completed -> {
                            this.setStatus(completed, CommonColors.GREEN);
                            this.populateLoadouts();
                            this.setSelectedLoadout(this.getLoadout(name));
                        }));
        this.addRenderableWidget(saveAspectsButton);

        loadButton = new LoadButton(
                (int) (dividedWidth * 35),
                (int) (dividedHeight * 52),
                (int) ((dividedWidth * 44) - (dividedWidth * 35)),
                BUTTON_SIZE,
                Component.translatable("screens.wynntils.buildLoadouts.load"),
                this);
        this.addRenderableWidget(loadButton);

        deleteButton = new DeleteButton(
                (int) (dividedWidth * 45),
                (int) (dividedHeight * 52),
                (int) ((dividedWidth * 51) - (dividedWidth * 45)),
                BUTTON_SIZE,
                Component.translatable("screens.wynntils.buildLoadouts.delete").withStyle(ChatFormatting.RED),
                this);
        this.addRenderableWidget(deleteButton);

        convertButton = new ConvertButton(
                (int) (dividedWidth * 52),
                (int) (dividedHeight * 52),
                (int) ((dividedWidth * 59) - (dividedWidth * 52)),
                BUTTON_SIZE,
                Component.translatable("screens.wynntils.buildLoadouts.convert"),
                this);
        this.addRenderableWidget(convertButton);
        // endregion

        scrollBar = new ScrollBar(dividedWidth * 30, dividedHeight * 4, dividedWidth * 0.5f, 0, this, dividedHeight);
        this.addRenderableWidget(scrollBar);

        setSelectedLoadout(null);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        // region Loadout headers
        RenderUtils.drawRect(
                guiGraphics,
                CommonColors.WHITE,
                dividedWidth * 4,
                dividedHeight * 4,
                dividedWidth * 30 - dividedWidth * 4,
                1);
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(I18n.get("screens.wynntils.buildLoadouts.loadoutName")),
                        dividedWidth * 4,
                        dividedHeight * 4,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        for (int i = 0; i < 5; i++) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(Component.literal(Skill.values()[i].getSymbol())
                                    .withStyle(Style.EMPTY
                                            .withColor(Skill.values()[i].getColorCode())
                                            .withFont(new FontDescription.Resource(
                                                    Identifier.withDefaultNamespace("common"))))),
                            (int) (dividedWidth * (23 + i * 1.5)),
                            dividedHeight * 4,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }
        // endregion

        // region Summary
        RenderUtils.drawRectBorders(
                guiGraphics,
                CommonColors.WHITE,
                dividedWidth * 34,
                dividedHeight * 4,
                dividedWidth * 60,
                dividedHeight * 30,
                1);
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(I18n.get("screens.wynntils.buildLoadouts.summary")),
                        dividedWidth * 34,
                        dividedHeight * 4 - 1,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        for (int i = 0; i < 5; i++) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(Component.literal(Skill.values()[i].getSymbol())
                                    .withStyle(Style.EMPTY
                                            .withColor(Skill.values()[i].getColorCode())
                                            .withFont(new FontDescription.Resource(
                                                    Identifier.withDefaultNamespace("common"))))),
                            (int) (dividedWidth * (53 + i * 1.5)),
                            dividedHeight * 4,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }

        // i iterates over the summary parts populating top to bottom
        // j iterates over the skills populating left to right
        for (int i = 0; i < summaryParts.size(); i++) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(summaryParts.get(i).key().get()),
                            dividedWidth * 35,
                            dividedHeight * (6 + i * 2),
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
            for (int j = 0; j < 5; j++) {
                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromString(Skill.values()[j].getColorCode() + ""
                                        + summaryParts.get(i).value().apply(Skill.values()[j])),
                                (int) (dividedWidth * (53 + j * 1.5)),////(int) (dividedWidth * (53 + j * 1.5))
                                dividedHeight * (6 + i * 2),
                                CommonColors.WHITE,
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.BOTTOM,
                                TextShadow.NORMAL);
            }
        }

        if (hasSaveNameConflict) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(I18n.get("screens.wynntils.buildLoadouts.saveNameConflict")),
                            dividedWidth * 35,
                            dividedHeight * 31,
                            CommonColors.RED,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }
        // endregion

        // region Selected loadout
        if (selectedLoadout != null) {
            RenderUtils.drawRectBorders(
                    guiGraphics,
                    CommonColors.WHITE,
                    dividedWidth * 34,
                    dividedHeight * 34,
                    dividedWidth * 60,
                    dividedHeight * 56,
                    1);

            // Name
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(selectedLoadout.name()),
                            dividedWidth * 34,
                            dividedHeight * 34,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);

            float currentY = 37;

            // --- Skill Points section ---
            if (selectedLoadout.hasSkillPoints()) {
                // 5 skill icons
                for (int i = 0; i < 5; i++) {
                    FontRenderer.getInstance()
                            .renderText(
                                    guiGraphics,
                                    StyledText.fromComponent(Component.literal(Skill.values()[i].getSymbol())
                                            .withStyle(Style.EMPTY
                                                    .withColor(Skill.values()[i].getColorCode())
                                                    .withFont(new FontDescription.Resource(
                                                            Identifier.withDefaultNamespace("common"))))),
                                    (int) (dividedWidth * (53 + i * 1.5)),
                                    dividedHeight * 34,
                                    CommonColors.WHITE,
                                    HorizontalAlignment.CENTER,
                                    VerticalAlignment.BOTTOM,
                                    TextShadow.NORMAL);
                }

                // Assigned row
                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromString(I18n.get(
                                        "screens.wynntils.buildLoadouts.assigned",
                                        selectedLoadout.skillPoints().getSkillPointsSum())),
                                dividedWidth * 35,
                                dividedHeight * currentY,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.BOTTOM,
                                TextShadow.NORMAL);
                for (int i = 0; i < 5; i++) {
                    FontRenderer.getInstance()
                            .renderText(
                                    guiGraphics,
                                    StyledText.fromString(Skill.values()[i].getColorCode() + ""
                                            + selectedLoadout.skillPoints().getSkillPointsAsArray()[i]),
                                    (int) (dividedWidth * (53 + i * 1.5)),
                                    dividedHeight * currentY,
                                    CommonColors.WHITE,
                                    HorizontalAlignment.CENTER,
                                    VerticalAlignment.BOTTOM,
                                    TextShadow.NORMAL);
                }
                currentY += 4;
            }

            // --- Ability Tree section ---
            if (selectedLoadout.hasAbilityTree()) {
                ClassType classType = selectedLoadout.abilityTree().getClassType();
                String className = (classType != null && classType != ClassType.NONE)
                        ? classType.getName()
                        : I18n.get("screens.wynntils.buildLoadouts.unknownClass");

                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromString(className),
                                dividedWidth * 52,
                                dividedHeight * 41,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.BOTTOM,
                                TextShadow.NORMAL);

                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromString(selectedLoadout.abilityTree().getMainArchetype()),
                                dividedWidth * 52,
                                dividedHeight * 43,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.BOTTOM,
                                TextShadow.NORMAL);

                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromString(
                                        selectedLoadout.abilityTree().getNodeCount() + " nodes"),
                                dividedWidth * 52,
                                dividedHeight * 45,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.BOTTOM,
                                TextShadow.NORMAL);

                int level = selectedLoadout.abilityTree().getDisplayLevel();
                String levelColor =
                        level > Models.CombatXp.getCombatLevel().current() ? ChatFormatting.RED.toString() : "";
                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromString(levelColor + "Level: " + level),
                                dividedWidth * 52,
                                dividedHeight * 47,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.BOTTOM,
                                TextShadow.NORMAL);
            }

            // --- Gear section ---
            if (selectedLoadout.hasSkillPoints()
                    && selectedLoadout.skillPoints().isBuild()) {
                if (selectedLoadout.skillPoints().weapon() != null) {
                    FontRenderer.getInstance()
                            .renderText(
                                    guiGraphics,
                                    StyledText.fromString(
                                            selectedLoadout.skillPoints().weapon()),
                                    dividedWidth * 35,
                                    dividedHeight * currentY,
                                    CommonColors.WHITE,
                                    HorizontalAlignment.LEFT,
                                    VerticalAlignment.BOTTOM,
                                    TextShadow.NORMAL);
                    currentY += 2;
                }

                List<TextRenderTask> tasks = new ArrayList<>();
                for (String armour : selectedLoadout.skillPoints().armourNames()) {
                    tasks.add(new TextRenderTask(
                            StyledText.fromString(armour),
                            new TextRenderSetting(
                                    dividedWidth * 9,
                                    CommonColors.WHITE,
                                    HorizontalAlignment.LEFT,
                                    VerticalAlignment.BOTTOM,
                                    TextShadow.NORMAL)));
                    tasks.add(new TextRenderTask(
                            StyledText.EMPTY,
                            new TextRenderSetting(
                                    0,
                                    CommonColors.WHITE,
                                    HorizontalAlignment.LEFT,
                                    VerticalAlignment.BOTTOM,
                                    TextShadow.NORMAL)));
                }
                FontRenderer.getInstance().renderTexts(guiGraphics, dividedWidth * 35, dividedHeight * currentY, tasks);

                tasks = new ArrayList<>();
                for (String accessory : selectedLoadout.skillPoints().accessoryNames()) {
                    tasks.add(new TextRenderTask(
                            StyledText.fromString(accessory),
                            new TextRenderSetting(
                                    dividedWidth * 9,
                                    CommonColors.WHITE,
                                    HorizontalAlignment.LEFT,
                                    VerticalAlignment.BOTTOM,
                                    TextShadow.NORMAL)));
                    tasks.add(new TextRenderTask(
                            StyledText.EMPTY,
                            new TextRenderSetting(
                                    0,
                                    CommonColors.WHITE,
                                    HorizontalAlignment.LEFT,
                                    VerticalAlignment.BOTTOM,
                                    TextShadow.NORMAL)));
                }
                float accessoryX = selectedLoadout.skillPoints().armourNames().isEmpty() ? 35 : 44;
                FontRenderer.getInstance()
                        .renderTexts(guiGraphics, dividedWidth * accessoryX, dividedHeight * currentY, tasks);
            } else {
                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromString(I18n.get("screens.wynntils.buildLoadouts.notBuild")),
                                dividedWidth * 35,
                                dividedHeight * currentY,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.BOTTOM,
                                TextShadow.NORMAL);
            }
        }
        // endregion

        // region Status
        if (!statusMessage.isEmpty()) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString(statusMessage),
                            dividedWidth * 34,
                            dividedHeight * 58,
                            dividedWidth * 26,
                            statusColor,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }
        // endregion

        // region Scrollbar
        if (loadoutWidgets.size() > MAX_LOADOUTS_PER_PAGE) {
            scrollBar.visible = true;
            scrollBar.active = true;
            float visibleRatio = Math.min(1, (float) MAX_LOADOUTS_PER_PAGE / loadoutWidgets.size());
            float scrollbarLength = dividedHeight * 48 * visibleRatio + 1;
            scrollBar.setY((int) (dividedHeight * 4 + dividedHeight * 48 * scrollPercent));
            scrollBar.setHeight((int) scrollbarLength);
        } else {
            scrollBar.visible = false;
            scrollBar.active = false;
        }
        // Only render from 4 to 56 for scrollable area
        // -/+ 1 to not overlap/cut off content
        RenderUtils.enableScissor(
                guiGraphics,
                (int) (dividedWidth * 4) - 1,
                (int) (dividedHeight * 4) + 1,
                (int) (dividedWidth * 26) + 1,
                (int) (dividedHeight * 48) + 1);
        loadoutWidgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
        RenderUtils.disableScissor(guiGraphics);
        // endregion
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        for (LoadoutWidget widget : loadoutWidgets) {
            if (widget.isMouseOver(event.x(), event.y())) {
                widget.mouseClicked(event, isDoubleClick);
                return true;
            }
        }

        if (scrollBar.isMouseOver(event.x(), event.y())) {
            scrollBar.mouseClicked(event, isDoubleClick);
        }

        return super.doMouseClicked(event, isDoubleClick);
    }

    // baseYPosition - dividedHeight * maxScrollOffset * scrollPercent = dividedHeight * 52
    // Solve for maxScrollOffset
    // Full explanation in #artemis-dev
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // In terms of grid divisions
        // 11 loadouts are fully displayed from 5 to 56
        // 12th one is very slightly cut off (it needs 57)
        if (loadoutWidgets.size() <= MAX_LOADOUTS_PER_PAGE) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        doScroll(scrollY);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void doScroll(double scrollAmount) {
        int scrollableWidgets = Math.max(0, loadoutWidgets.size() - MAX_LOADOUTS_PER_PAGE);
        if (scrollableWidgets == 0) return;
        float scrollableRatio = (float) scrollableWidgets / loadoutWidgets.size();
        float maxScrollOffset = (4 * (loadoutWidgets.size() - 1) - 43) / scrollableRatio;
        scrollPercent = (float) Math.max(0, Math.min(scrollableRatio, scrollPercent - scrollAmount / 50));

        loadoutWidgets.forEach(widget -> {
            float baseYPosition = dividedHeight * (5f + loadoutWidgets.indexOf(widget) * 4f);

            float scrollOffset = dividedHeight * maxScrollOffset * scrollPercent;
            widget.setY((int) (baseYPosition - scrollOffset));
            widget.visible = !(widget.getY() <= dividedHeight * 1) && !(widget.getY() >= dividedHeight * 52);
        });
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_DELETE && deleteButton.active) {
            deleteButton.onPress(event);
            return true;
        } else if (event.key() == GLFW.GLFW_KEY_END) {
            doScroll(Float.NEGATIVE_INFINITY);
        } else if (event.key() == GLFW.GLFW_KEY_HOME) {
            doScroll(Float.POSITIVE_INFINITY);
        }

        return super.keyPressed(event);
    }

    public void setSelectedLoadout(Loadout loadout) {
        if (loadout == null) {
            selectedLoadout = null;
            loadButton.active = false;
            loadButton.visible = false;
            deleteButton.active = false;
            deleteButton.visible = false;
            convertButton.active = false;
            convertButton.visible = false;
            return;
        }

        selectedLoadout = loadout;
        loadButton.active = true;
        loadButton.visible = true;
        deleteButton.active = true;
        deleteButton.visible = true;

        boolean isAbilityTreeOnly = loadout.type() == LoadoutType.ABILITY_TREE;
        convertButton.active = !isAbilityTreeOnly;
        convertButton.visible = !isAbilityTreeOnly;



        if (loadout.getMaxLevel() > Models.CombatXp.getCombatLevel().current()) {
            loadButton.setTooltip(
                    Tooltip.create(Component.translatable("screens.wynntils.buildLoadouts.levelIncompatible")
                            .withStyle(ChatFormatting.RED)));
        } else {
            loadButton.setTooltip(null);
        }
    }

    public Loadout getSelectedLoadout() {
        return selectedLoadout;
    }

    public void resetSaveButtons() {
        saveBuildButton.reset();
        saveSkillPointsButton.reset();
        saveAbilityTreeButton.reset();
        saveAspectsButton.reset();
    }

    public void populateLoadouts() {
        loadoutWidgets = new ArrayList<>();

        Map<String, SavableSkillPointSet> spLoadouts = new TreeMap<>(Models.SkillPoint.getLoadouts());
        Map<String, SavableAbilityTree> atLoadouts = Models.AbilityTree.getAbilityTreeLoadouts();

        Set<String> allNames = new HashSet<>();
        allNames.addAll(spLoadouts.keySet());
        allNames.addAll(atLoadouts.keySet());

        for (String name : new TreeSet<>(allNames)) {
            SavableSkillPointSet sp = spLoadouts.get(name);
            SavableAbilityTree at = atLoadouts.get(name);
            Loadout loadout = new Loadout(name, sp, at, determineLoadoutType(sp, at));

            loadoutWidgets.add(new LoadoutWidget(
                    (int) (dividedWidth * 4),
                    (int) (dividedHeight * (5 + loadoutWidgets.size() * 4)),
                    (int) (dividedWidth * 26),
                    (int) (dividedHeight * 4),
                    dividedWidth,
                    loadout,
                    this));
        }
    }

    private LoadoutType determineLoadoutType(SavableSkillPointSet sp, SavableAbilityTree at) {
        boolean hasSp = sp != null;
        boolean hasAt = at != null;
        if (hasSp && sp.isBuild()) return LoadoutType.BUILD;
        if (hasAt && !hasSp) return LoadoutType.ABILITY_TREE;
        return LoadoutType.SKILL_POINT;
    }

    public Loadout getLoadout(String name) {
        SavableSkillPointSet sp = Models.SkillPoint.getLoadouts().get(name);
        SavableAbilityTree at = Models.AbilityTree.getAbilityTreeLoadout(name);
        if (sp == null && at == null) return null;
        return new Loadout(name, sp, at, determineLoadoutType(sp, at));
    }

    public void setStatus(String message, CustomColor color) {
        this.statusMessage = message;
        this.statusColor = color;
    }
}
