/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.wynntilsmenu;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.features.ui.WynntilsContentBookFeature;
import com.wynntils.screens.base.WynntilsMenuScreenBase;
import com.wynntils.screens.crowdsourcing.WynntilsCrowdSourcingSettingsScreen;
import com.wynntils.screens.downloads.DownloadScreen;
import com.wynntils.screens.guides.WynntilsGuidesListScreen;
import com.wynntils.screens.itemsharing.SavedItemsScreen;
import com.wynntils.screens.lootrunpaths.WynntilsLootrunPathsScreen;
import com.wynntils.screens.maps.MainMapScreen;
import com.wynntils.screens.maps.PoiManagementScreen;
import com.wynntils.screens.overlays.selection.OverlaySelectionScreen;
import com.wynntils.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.screens.statistics.WynntilsStatisticsScreen;
import com.wynntils.screens.wynntilsmenu.widgets.WynntilsMenuButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class WynntilsMenuScreen extends WynntilsMenuScreenBase {
    private static final int BUTTON_SIZE = 30;

    private final List<List<WynntilsMenuButton>> buttons = new ArrayList<>();
    private WynntilsMenuButton hovered = null;

    // This makes sure we "save" our status on the settings & overlay screen, and we reopen it in the same state
    private static final Screen overlayScreenInstance = OverlaySelectionScreen.create();
    private static final Screen settingsScreenInstance = WynntilsBookSettingsScreen.create(null);

    private WynntilsMenuScreen() {
        super(Component.translatable("screens.wynntils.wynntilsMenu.name"));
    }

    public static Screen create() {
        return new WynntilsMenuScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();

        setup();
    }

    private void setup() {
        buttons.clear();

        // Add 4 rows of buttons
        for (int i = 0; i < 4; i++) {
            buttons.add(new ArrayList<>());
        }

        int x = ((BUTTON_SIZE + 5) + offsetX) - 15;
        int y = (BUTTON_SIZE + 5) + offsetY + 15;

        // region Row 1: Content / Activities
        buttons.getFirst()
                .add(new WynntilsMenuButton(
                        x,
                        y,
                        BUTTON_SIZE,
                        Texture.QUEST_BOOK_ICON,
                        true,
                        () -> ContainerUtils.openInventory(InventoryUtils.CONTENT_BOOK_SLOT_NUM),
                        List.of(
                                Component.literal("[>] ")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable("screens.wynntils.wynntilsMenu.questBook.name")
                                                .withStyle(ChatFormatting.BOLD)
                                                .withStyle(ChatFormatting.GOLD)),
                                Component.translatable("screens.wynntils.wynntilsMenu.questBook.description")
                                        .withStyle(ChatFormatting.GRAY),
                                Component.literal(""),
                                Component.translatable("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                        .withStyle(ChatFormatting.GREEN))));
        // endregion

        // region Row 2: Map
        x = ((BUTTON_SIZE + 5) + offsetX) - 15;
        y += (BUTTON_SIZE + 5);
        if (Managers.Feature.getFeatureInstance(MainMapFeature.class).isEnabled()) {
            buttons.get(1)
                    .add(new WynntilsMenuButton(
                            x,
                            y,
                            BUTTON_SIZE,
                            Texture.MAP_ICON,
                            true,
                            MainMapScreen.create(),
                            List.of(
                                    Component.literal("[>] ")
                                            .withStyle(ChatFormatting.GOLD)
                                            .append(Component.translatable(
                                                            "screens.wynntils.wynntilsQuestBook.mainMap.name")
                                                    .withStyle(ChatFormatting.BOLD)
                                                    .withStyle(ChatFormatting.GOLD)),
                                    Component.translatable("screens.wynntils.wynntilsQuestBook.mainMap.description")
                                            .withStyle(ChatFormatting.GRAY),
                                    Component.literal(""),
                                    Component.translatable("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                            .withStyle(ChatFormatting.GREEN))));
        }
        x += (BUTTON_SIZE + 5);
        buttons.get(1)
                .add(new WynntilsMenuButton(
                        x,
                        y,
                        BUTTON_SIZE,
                        Texture.LOOTRUN_ICON,
                        true,
                        WynntilsLootrunPathsScreen.create(),
                        List.of(
                                Component.literal("[>] ")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable(
                                                        "screens.wynntils.wynntilsQuestBook.lootruns.name")
                                                .withStyle(ChatFormatting.BOLD)
                                                .withStyle(ChatFormatting.GOLD)),
                                Component.translatable("screens.wynntils.wynntilsQuestBook.lootruns.description")
                                        .withStyle(ChatFormatting.GRAY),
                                Component.literal(""),
                                Component.translatable("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                        .withStyle(ChatFormatting.GREEN))));
        x += (BUTTON_SIZE + 5);
        buttons.get(1)
                .add(new WynntilsMenuButton(
                        x,
                        y,
                        BUTTON_SIZE,
                        Texture.WAYPOINT_MANAGER_ICON,
                        false,
                        PoiManagementScreen.create(),
                        List.of(
                                Component.literal("[>] ")
                                        .withStyle(ChatFormatting.GOLD)
                                        .withStyle(ChatFormatting.BOLD)
                                        .append(Component.translatable("screens.wynntils.map.manager.name")),
                                Component.translatable("screens.wynntils.map.manager.description")
                                        .withStyle(ChatFormatting.GRAY),
                                Component.literal(""),
                                Component.translatable("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                        .withStyle(ChatFormatting.GREEN))));

        // endregion

        // region Row 3: Guides
        x = ((BUTTON_SIZE + 5) + offsetX) - 15;
        y += (BUTTON_SIZE + 5);
        buttons.get(2)
                .add(new WynntilsMenuButton(
                        x,
                        y,
                        BUTTON_SIZE,
                        Texture.GUIDES_ICON,
                        true,
                        WynntilsGuidesListScreen.create(),
                        List.of(
                                Component.literal("[>] ")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable("screens.wynntils.wynntilsGuides.name")
                                                .withStyle(ChatFormatting.BOLD)
                                                .withStyle(ChatFormatting.GOLD)),
                                Component.translatable("screens.wynntils.wynntilsGuides.description")
                                        .withStyle(ChatFormatting.GRAY),
                                Component.literal(""),
                                Component.translatable("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                        .withStyle(ChatFormatting.GREEN))));
        x += (BUTTON_SIZE + 5);
        buttons.get(2)
                .add(new WynntilsMenuButton(
                        x,
                        y,
                        BUTTON_SIZE,
                        Texture.FAVORITE_ICON,
                        false,
                        WynntilsStatisticsScreen.create(),
                        List.of(
                                Component.literal("[>] ")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable("screens.wynntils.statistics.name")
                                                .withStyle(ChatFormatting.BOLD)
                                                .withStyle(ChatFormatting.GOLD)),
                                Component.translatable("screens.wynntils.statistics.description")
                                        .withStyle(ChatFormatting.GRAY),
                                Component.literal(""),
                                Component.translatable("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                        .withStyle(ChatFormatting.GREEN))));
        x += (BUTTON_SIZE + 5);
        buttons.get(2)
                .add(new WynntilsMenuButton(
                        x,
                        y,
                        BUTTON_SIZE,
                        Texture.ITEM_LOCK,
                        false,
                        SavedItemsScreen.create(),
                        List.of(
                                Component.literal("[>] ")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable("screens.wynntils.savedItems.name")
                                                .withStyle(ChatFormatting.BOLD)
                                                .withStyle(ChatFormatting.GOLD)),
                                Component.translatable("screens.wynntils.savedItems.description")
                                        .withStyle(ChatFormatting.GRAY),
                                Component.literal(""),
                                Component.translatable("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                        .withStyle(ChatFormatting.GREEN))));

        // endregion

        // region Row 4: Wynntils
        x = ((BUTTON_SIZE + 5) + offsetX) - 15;
        y += (BUTTON_SIZE + 5);
        buttons.get(3)
                .add(new WynntilsMenuButton(
                        x,
                        y,
                        BUTTON_SIZE,
                        Texture.SETTINGS_ICON,
                        true,
                        settingsScreenInstance,
                        List.of(
                                Component.literal("[>] ")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable("screens.wynntils.wynntilsMenu.configs.name")
                                                .withStyle(ChatFormatting.BOLD)
                                                .withStyle(ChatFormatting.GOLD)),
                                Component.translatable("screens.wynntils.wynntilsMenu.configs.description")
                                        .withStyle(ChatFormatting.GRAY),
                                Component.literal(""),
                                Component.translatable("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                        .withStyle(ChatFormatting.GREEN))));
        x += (BUTTON_SIZE + 5);
        buttons.get(3)
                .add(new WynntilsMenuButton(
                        x,
                        y,
                        BUTTON_SIZE,
                        Texture.OVERLAYS_ICON,
                        true,
                        overlayScreenInstance,
                        List.of(
                                Component.literal("[>] ")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable(
                                                        "screens.wynntils.wynntilsMenu.overlayConfig.name")
                                                .withStyle(ChatFormatting.BOLD)
                                                .withStyle(ChatFormatting.GOLD)),
                                Component.translatable("screens.wynntils.wynntilsMenu.overlayConfig.description")
                                        .withStyle(ChatFormatting.GRAY),
                                Component.literal(""),
                                Component.translatable("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                        .withStyle(ChatFormatting.GREEN))));
        x += (BUTTON_SIZE + 5);
        buttons.get(3)
                .add(new WynntilsMenuButton(
                        x,
                        y,
                        BUTTON_SIZE,
                        Texture.SHARE_ICON,
                        false,
                        WynntilsCrowdSourcingSettingsScreen.create(),
                        List.of(
                                Component.literal("[>] ")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable(
                                                        "screens.wynntils.wynntilsMenu.crowdSourcing.name")
                                                .withStyle(ChatFormatting.BOLD)
                                                .withStyle(ChatFormatting.GOLD)),
                                Component.translatable("screens.wynntils.wynntilsMenu.crowdSourcing.description")
                                        .withStyle(ChatFormatting.GRAY),
                                Component.literal(""),
                                Component.translatable("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                        .withStyle(ChatFormatting.GREEN))));
        x += (BUTTON_SIZE + 5);
        buttons.get(3)
                .add(new WynntilsMenuButton(
                        x,
                        y,
                        BUTTON_SIZE,
                        Texture.EDIT_NAME_ICON,
                        false,
                        DownloadScreen.create(null, null),
                        List.of(
                                Component.literal("[>] ")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable("screens.wynntils.wynntilsMenu.downloads.name")
                                                .withStyle(ChatFormatting.BOLD)
                                                .withStyle(ChatFormatting.GOLD)),
                                Component.translatable("screens.wynntils.wynntilsMenu.downloads.description")
                                        .withStyle(ChatFormatting.GRAY),
                                Component.literal(""),
                                Component.translatable("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                        .withStyle(ChatFormatting.GREEN))));

        // endregion

        assert buttons.size() == 4 && buttons.stream().allMatch(row -> row.size() <= 4);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        renderBackgroundTexture(poseStack);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsMenu.userProfile"));

        renderVersion(poseStack);

        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderPlayerInfo(guiGraphics, mouseX, mouseY);

        renderPlayer(guiGraphics, mouseX, mouseY);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTitle(PoseStack poseStack, String titleString) {
        int txWidth = Texture.CONTENT_BOOK_TITLE.width();
        int txHeight = Texture.CONTENT_BOOK_TITLE.height();
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.CONTENT_BOOK_TITLE.resource(),
                offsetX,
                15 + offsetY,
                0,
                txWidth,
                txHeight,
                txWidth,
                txHeight);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(titleString),
                        10 + offsetX,
                        21 + offsetY,
                        CommonColors.YELLOW,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL,
                        2f);
    }

    private void renderPlayerInfo(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        PoseStack poseStack = guiGraphics.pose();

        if (!Models.Guild.getGuildName().isEmpty()) {
            String rank = Models.Guild.getGuildRank().getGuildDescription();

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromString(rank + " of"),
                            Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + offsetX,
                            Texture.CONTENT_BOOK_BACKGROUND.width() + offsetX,
                            40 + offsetY,
                            0,
                            CommonColors.CYAN,
                            HorizontalAlignment.CENTER,
                            TextShadow.NONE);

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromString(Models.Guild.getGuildName()),
                            Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + offsetX,
                            Texture.CONTENT_BOOK_BACKGROUND.width() + offsetX,
                            50 + offsetY,
                            0,
                            CommonColors.CYAN,
                            HorizontalAlignment.CENTER,
                            TextShadow.NONE);
        }

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromComponent(McUtils.player().getDisplayName())
                                .withoutFormatting(),
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + offsetX,
                        Texture.CONTENT_BOOK_BACKGROUND.width() + offsetX,
                        60 + offsetY,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.CENTER,
                        TextShadow.NONE);
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(
                                "Level " + Models.CombatXp.getCombatLevel().current() + " "
                                        + Models.Character.getClassType().getName()),
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + offsetX,
                        Texture.CONTENT_BOOK_BACKGROUND.width() + offsetX,
                        145 + offsetY,
                        0,
                        CommonColors.PURPLE,
                        HorizontalAlignment.CENTER,
                        TextShadow.NONE);

        if (Managers.Feature.getFeatureInstance(WynntilsContentBookFeature.class)
                .displayOverallProgress
                .get()) {
            CappedValue progress = Models.Activity.getOverallProgress();
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromString(ChatFormatting.BLACK + "Progress: " + ChatFormatting.DARK_AQUA
                                    + progress.getPercentageInt() + "%" + ChatFormatting.BLACK + " ["
                                    + ChatFormatting.DARK_AQUA + progress + ChatFormatting.BLACK + "]"),
                            Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + offsetX,
                            Texture.CONTENT_BOOK_BACKGROUND.width() + offsetX,
                            160 + offsetY,
                            0,
                            CommonColors.BLACK,
                            HorizontalAlignment.CENTER,
                            TextShadow.NONE);
        }

        String currentSplash = Services.Splash.getCurrentSplash() == null ? "" : Services.Splash.getCurrentSplash();
        StyledText[] wrappedSplash = RenderedStringUtils.wrapTextBySize(
                StyledText.fromString(currentSplash), Texture.CONTENT_BOOK_BACKGROUND.width() / 2 - 20);

        for (int i = 0; i < wrappedSplash.length; i++) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            wrappedSplash[i],
                            Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + offsetX,
                            Texture.CONTENT_BOOK_BACKGROUND.width() + offsetX,
                            Texture.CONTENT_BOOK_BACKGROUND.height()
                                    - 45
                                    + i * (McUtils.mc().font.lineHeight + 1)
                                    + offsetY,
                            0,
                            CommonColors.MAGENTA,
                            HorizontalAlignment.CENTER,
                            TextShadow.NONE);
        }
    }

    private void renderPlayer(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int posX = (int) (Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + 50 + offsetX);
        int posY = (int) (Texture.CONTENT_BOOK_BACKGROUND.height() / 2f - 40 + offsetY);

        final int renderWidth = (int) (Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 100);
        final int renderHeight = 70;

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics,
                posX,
                posY,
                posX + renderWidth,
                posY + renderHeight,
                25,
                0.4f,
                mouseX,
                mouseY,
                McUtils.player());
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        if (this.hovered == null) return false;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.hovered.getClickAction().run();
        }

        return true;
    }

    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.hovered != null) {
            guiGraphics.renderComponentTooltip(
                    FontRenderer.getInstance().getFont(),
                    ComponentUtils.wrapTooltips(this.hovered.getTooltipList(), 250),
                    mouseX,
                    mouseY);
        }
    }

    private void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.hovered = null;

        for (WynntilsMenuButton button : buttons.stream().flatMap(List::stream).toList()) {
            button.render(guiGraphics, mouseX, mouseY, partialTick);

            if (button.isMouseOver(mouseX, mouseY)) {
                this.hovered = button;
            }
        }
    }
}
