/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.google.common.collect.Lists;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.models.abilitytree.type.AbilityPointProgression;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.containers.containers.CharacterInfoContainer;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.WYNNTILS)
public class CharacterInfoIndicatorFeature extends Feature {
    // It takes a while for the compass to actually have the correct ability points,
    // otherwise it is max and then the calculation fails and an error shows up in the chat.
    private static final int DELAY_TICKS = 200;

    private static final Pattern UNUSED_ABILITY_POINTS_PATTERN =
            Pattern.compile("§3✦ Unused Ability Points: §f(\\d+)");

    @Persisted(i18nKey = "feature.wynntils.characterInfoIndicator.rescanMessage")
    private final Config<Boolean> rescanMessage = new Config<>(true);

    public CharacterInfoIndicatorFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent
    public void onCharacterInfoScreenOpened(ScreenOpenedEvent.Post e) {
        if (!(e.getScreen() instanceof ContainerScreen screen)) return;
        if (!(Models.Container.getCurrentContainer() instanceof CharacterInfoContainer)) return;

        screen.addRenderableWidget(
                new InfoIndicatorButton(screen.leftPos + screen.imageWidth + 26, screen.topPos, this));
    }

    private static final class InfoIndicatorButton extends AbstractButton implements TooltipProvider {
        private static final int BUTTON_WIDTH = 20;
        private static final int BUTTON_HEIGHT = 20;
        private final CharacterInfoIndicatorFeature parent;

        private List<Component> generatedTooltip = new ArrayList<>();

        public InfoIndicatorButton(int x, int y, CharacterInfoIndicatorFeature parent) {
            super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Info Indicator Button"));
            this.parent = parent;
            buildTooltip();
        }

        @Override
        public void onPress(InputWithModifiers input) {}

        @Override
        protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            renderDefaultSprite(guiGraphics);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(Component.literal("\uE027")
                                    .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT))),
                            (this.getX() + this.width / 2f),
                            (this.getY() + this.height / 2f),
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            if (isMouseOver(mouseX, mouseY)) {
                guiGraphics.setTooltipForNextFrame(
                        Lists.transform(getTooltipLines(), Component::getVisualOrderText), mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
            if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

            this.playDownSound(McUtils.mc().getSoundManager());

            Handlers.Command.sendCommandImmediately("wynntils rescan");

            return true;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

        @Override
        public List<Component> getTooltipLines() {
            return Collections.unmodifiableList(this.generatedTooltip);
        }

        private void buildTooltip() {
            this.generatedTooltip = new ArrayList<>();

            this.generatedTooltip.add(Component.translatable("feature.wynntils.characterInfoIndicator.tooltip.title")
                    .withStyle(ChatFormatting.GOLD));

            this.generatedTooltip.add(Component.empty());

            this.generatedTooltip.add(Component.literal("- ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.translatable("feature.wynntils.characterInfoIndicator.tooltip.update")
                            .withStyle(ChatFormatting.WHITE)));

            this.generatedTooltip.add(Component.empty());

            this.generatedTooltip.add(Component.empty()
                    .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(Component.translatable("feature.wynntils.characterInfoIndicator.tooltip.leftClick")
                            .withStyle(ChatFormatting.GREEN)));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldStateChanged(WorldStateEvent e) {
        if (!rescanMessage.get()) return;

        if (e.getNewState() == WorldState.WORLD) {
            Managers.TickScheduler.scheduleLater(this::handleAbilityPointsFromCompass, DELAY_TICKS);
        }
    }

    private void handleAbilityPointsFromCompass() {
        ItemStack compassItem = McUtils.inventory().items.get(Models.Character.CHARACTER_INFO_SLOT);
        Matcher matcher = LoreUtils.matchLoreLine(compassItem, 6, UNUSED_ABILITY_POINTS_PATTERN);

        if (!matcher.matches()) {
            WynntilsMod.warn("Compass item had unexpected unused ability points line");
            return;
        }

        int unusedAbilityPoints = Integer.parseInt(matcher.group(1));

        ClassType classType = Models.Character.getClassType();
        List<String> equippedNames = Models.AbilityTree.getUnlockedAbilities();

        int usedAbilityPoints = 0;
        for (String abilityName : equippedNames) {
            AbilityTreeSkillNode node = Models.AbilityTree.getNodeFromNameAndClass(abilityName, classType);
            if (node != null) {
                usedAbilityPoints += node.cost();
            }
        }

        int totalAbilityPoints = usedAbilityPoints + unusedAbilityPoints;

        int combatLevel = Models.CombatXp.getCombatLevel().current();
        int maxAbilityPoints = AbilityPointProgression.getPointsAtLevel(combatLevel);

        boolean isMismatch = totalAbilityPoints != maxAbilityPoints;

        WynntilsMod.info(String.format(
                "Ability points: used=%d, unused=%d, total=%d, max=%d for level=%d.%s",
                usedAbilityPoints,
                unusedAbilityPoints,
                totalAbilityPoints,
                maxAbilityPoints,
                combatLevel,
                isMismatch ? " Mismatch detected, tracked ability tree state is out of sync." : ""));

        if (!isMismatch) return;


        WynntilsMod.info(String.format(
                "Ability point mismatch: used=%d, unused=%d, total=%d, max=%d for level=%d."
                        + " Tracked ability tree state is out of sync.",
                usedAbilityPoints, unusedAbilityPoints, totalAbilityPoints, maxAbilityPoints, combatLevel));


        Component clickableHere = Component.translatable(
                        "feature.wynntils.characterInfoIndicator.rescanMessage.message.clickHere")
                .withStyle(Style.EMPTY
                        .withUnderlined(true)
                        .withColor(CommonColors.RED.asInt())
                        .withClickEvent(new ClickEvent.RunCommand("/wynntils rescan")));

        Component fullMessage = Component.empty()
                .append(Component.translatable("feature.wynntils.characterInfoIndicator.rescanMessage.message.prefix")
                        .withStyle(Style.EMPTY.withColor(CommonColors.RED.asInt())))
                .append(clickableHere)
                .append(Component.translatable("feature.wynntils.characterInfoIndicator.rescanMessage.message.suffix")
                        .withStyle(Style.EMPTY.withColor(CommonColors.RED.asInt())));

        McUtils.sendWynntilsPrefixMessage(fullMessage);
    }
}
