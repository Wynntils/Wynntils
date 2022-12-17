/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Managers;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.widgets.BackButton;
import com.wynntils.gui.widgets.DiscoveryButton;
import com.wynntils.gui.widgets.DiscoveryFilterButton;
import com.wynntils.gui.widgets.DiscoveryProgressButton;
import com.wynntils.gui.widgets.PageSelectorButton;
import com.wynntils.gui.widgets.ReloadButton;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.event.DiscoveriesUpdatedEvent;
import com.wynntils.wynn.model.discoveries.objects.DiscoveryInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WynntilsDiscoveriesScreen extends WynntilsMenuListScreen<DiscoveryInfo, DiscoveryButton> {
    private static final List<Component> RELOAD_TOOLTIP = List.of(
            new TranslatableComponent("screens.wynntils.wynntilsDiscoveries.reload.name")
                    .withStyle(ChatFormatting.WHITE),
            new TranslatableComponent("screens.wynntils.wynntilsDiscoveries.reload.description")
                    .withStyle(ChatFormatting.GRAY));

    private final List<DiscoveryFilterButton> filterButtons = new ArrayList<>();

    // Filters
    private boolean showFoundSecrets = true;
    private boolean showUndiscoveredSecrets = false;
    private boolean showFoundWorld = true;
    private boolean showUndiscoveredWorld = false;
    private boolean showFoundTerritory = true;
    private boolean showUndiscoveredTerritory = false;

    private WynntilsDiscoveriesScreen() {
        super(new TranslatableComponent("screens.wynntils.wynntilsDiscoveries.name"));

        // Only register this once
        WynntilsMod.registerEventListener(this);
    }

    public static Screen create() {
        return WynntilsScreenWrapper.create(new WynntilsDiscoveriesScreen());
    }

    @SubscribeEvent
    public void onDiscoveryUpdate(DiscoveriesUpdatedEvent event) {
        if (McUtils.mc().screen == this) {
            this.reloadElements();
        }
    }

    @Override
    public void onClose() {
        McUtils.mc().keyboardHandler.setSendRepeatsToGui(false);
        WynntilsMod.unregisterEventListener(this);

        super.onClose();
    }

    @Override
    protected void init() {
        Managers.Discovery.reloadDiscoveries();

        McUtils.mc().keyboardHandler.setSendRepeatsToGui(true);

        super.init();

        filterButtons.clear();

        filterButtons.add(new DiscoveryFilterButton(
                35,
                125,
                30,
                30,
                Texture.DISCOVERED_TERRITORY,
                true,
                List.of(new TextComponent("[>] ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(new TranslatableComponent(
                                        "screens.wynntils.wynntilsDiscoveries.showFoundTerritory.name")
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(ChatFormatting.GREEN))),
                () -> {
                    showFoundTerritory = !showFoundTerritory;
                    reloadElements();
                },
                () -> showFoundTerritory));
        filterButtons.add(new DiscoveryFilterButton(
                70,
                125,
                30,
                30,
                Texture.DISCOVERED_WORLD,
                true,
                List.of(new TextComponent("[>] ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(new TranslatableComponent("screens.wynntils.wynntilsDiscoveries.showFoundWorld.name")
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(ChatFormatting.GREEN))),
                () -> {
                    showFoundWorld = !showFoundWorld;
                    reloadElements();
                },
                () -> showFoundWorld));
        filterButtons.add(new DiscoveryFilterButton(
                105,
                125,
                30,
                30,
                Texture.DISCOVERED_SECRET,
                true,
                List.of(new TextComponent("[>] ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(new TranslatableComponent("screens.wynntils.wynntilsDiscoveries.showFoundSecret.name")
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(ChatFormatting.GREEN))),
                () -> {
                    showFoundSecrets = !showFoundSecrets;
                    reloadElements();
                },
                () -> showFoundSecrets));
        filterButtons.add(new DiscoveryFilterButton(
                35,
                160,
                30,
                30,
                Texture.UNDISCOVERED_TERRITORY,
                true,
                List.of(new TextComponent("[>] ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(new TranslatableComponent(
                                        "screens.wynntils.wynntilsDiscoveries.showUnfoundTerritory.name")
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(ChatFormatting.GREEN))),
                () -> {
                    showUndiscoveredTerritory = !showUndiscoveredTerritory;
                    reloadElements();
                },
                () -> showUndiscoveredTerritory));
        filterButtons.add(new DiscoveryFilterButton(
                70,
                160,
                30,
                30,
                Texture.UNDISCOVERED_WORLD,
                true,
                List.of(new TextComponent("[>] ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(new TranslatableComponent("screens.wynntils.wynntilsDiscoveries.showUnfoundWorld.name")
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(ChatFormatting.GREEN))),
                () -> {
                    showUndiscoveredWorld = !showUndiscoveredWorld;
                    reloadElements();
                },
                () -> showUndiscoveredWorld));
        filterButtons.add(new DiscoveryFilterButton(
                105,
                160,
                30,
                30,
                Texture.UNDISCOVERED_SECRET,
                true,
                List.of(new TextComponent("[>] ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(new TranslatableComponent("screens.wynntils.wynntilsDiscoveries.showUnfoundSecret.name")
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(ChatFormatting.GREEN))),
                () -> {
                    showUndiscoveredSecrets = !showUndiscoveredSecrets;
                    reloadElements();
                },
                () -> showUndiscoveredSecrets));

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW.width() / 2,
                Texture.BACK_ARROW.height(),
                WynntilsMenuScreen.create()));

        this.addRenderableWidget(new ReloadButton(
                Texture.QUEST_BOOK_BACKGROUND.width() - 21,
                11,
                (int) (Texture.RELOAD_BUTTON.width() / 2 / 1.7f),
                (int) (Texture.RELOAD_BUTTON.height() / 1.7f),
                Managers.Discovery::reloadDiscoveries));

        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 50 - Texture.FORWARD_ARROW.width() / 2,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() - 50,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                true,
                this));

        for (DiscoveryFilterButton filterButton : filterButtons) {
            this.addRenderableWidget(filterButton);
        }

        this.addRenderableWidget(new DiscoveryProgressButton(50, 10, 20, 20, false));
        this.addRenderableWidget(new DiscoveryProgressButton(75, 10, 20, 20, true));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = getTranslationX();
        final float translationY = getTranslationY();
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsDiscoveries.name"));

        renderVersion(poseStack);

        renderButtons(poseStack, mouseX, mouseY, partialTick);

        if (elements.isEmpty()) {
            renderNoDiscoveries(poseStack);
        }

        renderDescription(
                poseStack,
                I18n.get("screens.wynntils.wynntilsDiscoveries.screenDescription"),
                I18n.get("screens.wynntils.wynntilsDiscoveries.filterHelper"));

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        poseStack.popPose();

        renderTooltip(poseStack, mouseX, mouseY);
    }

    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        List<Component> tooltipLines = List.of();

        if (this.hovered instanceof ReloadButton) {
            tooltipLines = RELOAD_TOOLTIP;
        } else if (this.hovered instanceof DiscoveryFilterButton filterButton) {
            tooltipLines = filterButton.getTooltipLines();
        } else if (this.hovered instanceof DiscoveryButton discoveryButton) {
            tooltipLines = discoveryButton.getTooltipLines();
        } else if (this.hovered instanceof DiscoveryProgressButton progressButton) {
            if (progressButton.isSecretDiscoveryButton()) {
                tooltipLines = Managers.Discovery.getSecretDiscoveriesTooltip();
            } else {
                tooltipLines = Managers.Discovery.getDiscoveriesTooltip();
            }
        }

        if (tooltipLines.isEmpty()) return;

        RenderUtils.drawTooltipAt(
                poseStack,
                mouseX,
                mouseY,
                100,
                tooltipLines,
                FontRenderer.getInstance().getFont(),
                true);
    }

    private static void renderNoDiscoveries(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        I18n.get("screens.wynntils.wynntilsDiscoveries.noDiscoveries"),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f + 15f,
                        Texture.QUEST_BOOK_BACKGROUND.width() - 15f,
                        0,
                        Texture.QUEST_BOOK_BACKGROUND.height(),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30f,
                        CommonColors.BLACK,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Middle,
                        FontRenderer.TextShadow.NONE);
    }

    private void renderDescription(PoseStack poseStack, String description, String filterHelper) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        description,
                        20,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 10,
                        80,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        FontRenderer.TextShadow.NONE);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        filterHelper,
                        20,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 10,
                        105,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        FontRenderer.TextShadow.NONE);
    }

    @Override
    protected DiscoveryButton getButtonFromElement(int i) {
        int offset = i % getElementsPerPage();
        return new DiscoveryButton(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 15,
                offset * 13 + 25,
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 - 37,
                9,
                elements.get(i));
    }

    @Override
    protected void reloadElementsList(String searchTerm) {
        // We need to filter duplicates
        elements.addAll(Stream.concat(
                        Managers.Discovery.getDiscoveryInfoList().stream()
                                .filter(discoveryInfo -> switch (discoveryInfo.getType()) {
                                    case TERRITORY -> showUndiscoveredTerritory;
                                    case WORLD -> showUndiscoveredWorld;
                                    case SECRET -> showUndiscoveredSecrets;
                                })
                                .filter(discoveryInfo -> Managers.Discovery.getAllDiscoveries()
                                        .noneMatch(
                                                discovery -> discovery.getName().equals(discoveryInfo.getName()))),
                        Managers.Discovery.getAllDiscoveries()
                                .filter(discoveryInfo -> switch (discoveryInfo.getType()) {
                                    case TERRITORY -> showFoundTerritory;
                                    case WORLD -> showFoundWorld;
                                    case SECRET -> showFoundSecrets;
                                }))
                .filter(info -> StringUtils.partialMatch(info.getName(), searchTerm))
                .sorted(Comparator.comparing(DiscoveryInfo::getMinLevel).thenComparing(DiscoveryInfo::getType))
                .toList());
    }
}
