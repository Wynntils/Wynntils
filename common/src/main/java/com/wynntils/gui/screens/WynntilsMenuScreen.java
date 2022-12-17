/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.managers.Managers;
import com.wynntils.features.user.map.MapFeature;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.screens.maps.MainMapScreen;
import com.wynntils.gui.screens.overlays.OverlaySelectionScreen;
import com.wynntils.gui.screens.settings.WynntilsBookSettingsScreen;
import com.wynntils.gui.widgets.WynntilsMenuButton;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.model.CharacterManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class WynntilsMenuScreen extends WynntilsMenuScreenBase {
    private static final int BUTTON_SIZE = 30;
    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);

    private final List<WynntilsMenuButton> buttons = new ArrayList<>();
    private WynntilsMenuButton hovered = null;

    // This makes sure we "save" our status on the settings screen, and we reopen it in the same state
    private static final Screen settingsScreenInstance = WynntilsBookSettingsScreen.create();

    private WynntilsMenuScreen() {
        super(new TranslatableComponent("screens.wynntils.wynntilsMenu.name"));
        setup();
    }

    public static Screen create() {
        return WynntilsScreenWrapper.create(new WynntilsMenuScreen());
    }

    private void setup() {
        buttons.add(new WynntilsMenuButton(
                Texture.QUEST_BOOK_ICON,
                true,
                WynntilsQuestBookScreen.create(),
                List.of(
                        new TextComponent("[>] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(new TranslatableComponent("screens.wynntils.wynntilsMenu.questBook.name")
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.GOLD)),
                        new TranslatableComponent("screens.wynntils.wynntilsMenu.questBook.description")
                                .withStyle(ChatFormatting.GRAY),
                        new TextComponent(""),
                        new TranslatableComponent("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                .withStyle(ChatFormatting.GREEN))));
        buttons.add(new WynntilsMenuButton(
                Texture.SETTINGS_ICON,
                true,
                settingsScreenInstance,
                List.of(
                        new TextComponent("[>] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(new TranslatableComponent("screens.wynntils.wynntilsMenu.configs.name")
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.GOLD)),
                        new TranslatableComponent("screens.wynntils.wynntilsMenu.configs.description")
                                .withStyle(ChatFormatting.GRAY),
                        new TextComponent(""),
                        new TranslatableComponent("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                .withStyle(ChatFormatting.GREEN))));
        buttons.add(new WynntilsMenuButton(
                Texture.OVERLAYS_ICON,
                true,
                OverlaySelectionScreen.create(),
                List.of(
                        new TextComponent("[>] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(new TranslatableComponent("screens.wynntils.wynntilsMenu.overlayConfig.name")
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.GOLD)),
                        new TranslatableComponent("screens.wynntils.wynntilsMenu.overlayConfig.description")
                                .withStyle(ChatFormatting.GRAY),
                        new TextComponent(""),
                        new TranslatableComponent("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                .withStyle(ChatFormatting.GREEN))));
        buttons.add(new WynntilsMenuButton(
                Texture.DIALOGUE_BUTTON,
                false,
                WynntilsDialogueHistoryScreen.create(),
                List.of(
                        new TextComponent("[>] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(new TranslatableComponent(
                                                "screens.wynntils.wynntilsQuestBook.dialogueHistory.name")
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.GOLD)),
                        new TranslatableComponent("screens.wynntils.wynntilsQuestBook.dialogueHistory.description")
                                .withStyle(ChatFormatting.GRAY),
                        new TextComponent(""),
                        new TranslatableComponent("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                .withStyle(ChatFormatting.GREEN))));
        if (MapFeature.INSTANCE.isEnabled()) {
            buttons.add(new WynntilsMenuButton(
                    Texture.MAP_ICON,
                    true,
                    MainMapScreen.create(),
                    List.of(
                            new TextComponent("[>] ")
                                    .withStyle(ChatFormatting.GOLD)
                                    .append(new TranslatableComponent("screens.wynntils.wynntilsQuestBook.mainMap.name")
                                            .withStyle(ChatFormatting.BOLD)
                                            .withStyle(ChatFormatting.GOLD)),
                            new TranslatableComponent("screens.wynntils.wynntilsQuestBook.mainMap.description")
                                    .withStyle(ChatFormatting.GRAY),
                            new TextComponent(""),
                            new TranslatableComponent("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                    .withStyle(ChatFormatting.GREEN))));
        }
        buttons.add(new WynntilsMenuButton(
                Texture.LOOTRUN_ICON,
                true,
                WynntilsLootrunsScreen.create(),
                List.of(
                        new TextComponent("[>] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(new TranslatableComponent("screens.wynntils.wynntilsQuestBook.lootruns.name")
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.GOLD)),
                        new TranslatableComponent("screens.wynntils.wynntilsQuestBook.lootruns.description")
                                .withStyle(ChatFormatting.GRAY),
                        new TextComponent(""),
                        new TranslatableComponent("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                .withStyle(ChatFormatting.GREEN))));

        buttons.add(new WynntilsMenuButton(
                Texture.GUIDES_ICON,
                true,
                WynntilsGuidesListScreen.create(),
                List.of(
                        new TextComponent("[>] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(new TranslatableComponent("screens.wynntils.wynntilsGuides.name")
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.GOLD)),
                        new TranslatableComponent("screens.wynntils.wynntilsGuides.description")
                                .withStyle(ChatFormatting.GRAY),
                        new TextComponent(""),
                        new TranslatableComponent("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                .withStyle(ChatFormatting.GREEN))));

        buttons.add(new WynntilsMenuButton(
                Texture.DISCOVERIES_ICON,
                true,
                WynntilsDiscoveriesScreen.create(),
                List.of(
                        new TextComponent("[>] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(new TranslatableComponent("screens.wynntils.wynntilsDiscoveries.name")
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.GOLD)),
                        new TranslatableComponent("screens.wynntils.wynntilsDiscoveries.description")
                                .withStyle(ChatFormatting.GRAY),
                        new TextComponent(""),
                        new TranslatableComponent("screens.wynntils.wynntilsMenu.leftClickToSelect")
                                .withStyle(ChatFormatting.GREEN))));

        assert buttons.size() <= 8;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = (this.width - Texture.QUEST_BOOK_BACKGROUND.width()) / 2f;
        final float translationY = (this.height - Texture.QUEST_BOOK_BACKGROUND.height()) / 2f;
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsMenu.userProfile"));

        renderVersion(poseStack);

        renderButtons(poseStack, mouseX, mouseY);

        renderTooltip(poseStack, mouseX, mouseY, translationX, translationY);

        renderDescription(poseStack, I18n.get("screens.wynntils.wynntilsMenu.description"));

        renderPlayerInfo(poseStack, mouseX, mouseY, translationX, translationY);

        poseStack.popPose();
    }

    private static void renderPlayerInfo(
            PoseStack poseStack, int mouseX, int mouseY, float translationX, float translationY) {
        int posX = (int) (translationX + Texture.QUEST_BOOK_BACKGROUND.width()) - 85;
        int posY = (int) (translationY + Texture.QUEST_BOOK_BACKGROUND.height() / 2f) + 25;
        InventoryScreen.renderEntityInInventory(posX, posY, 30, posX - mouseX, posY - mouseY, McUtils.player());

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        ComponentUtils.getUnformatted(McUtils.player().getDisplayName()),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f,
                        Texture.QUEST_BOOK_BACKGROUND.width(),
                        60,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.Center,
                        FontRenderer.TextShadow.NONE);
        CharacterManager.CharacterInfo characterInfo = Managers.Character.getCharacterInfo();
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        characterInfo.getClassType().getName().toUpperCase(Locale.ROOT) + " Level "
                                + characterInfo.getLevel(),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f,
                        Texture.QUEST_BOOK_BACKGROUND.width(),
                        145,
                        0,
                        CommonColors.PURPLE,
                        HorizontalAlignment.Center,
                        FontRenderer.TextShadow.NONE);

        String currentSplash = Managers.Splash.getCurrentSplash();
        currentSplash = currentSplash == null ? "" : currentSplash;
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        currentSplash,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f,
                        Texture.QUEST_BOOK_BACKGROUND.width(),
                        Texture.QUEST_BOOK_BACKGROUND.height() - 45,
                        0,
                        CommonColors.MAGENTA,
                        HorizontalAlignment.Center,
                        FontRenderer.TextShadow.NONE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.hovered == null) return false;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            McUtils.mc().setScreen(this.hovered.openedScreen());
        }

        return true;
    }

    private void renderTooltip(PoseStack poseStack, int mouseX, int mouseY, float translationX, float translationY) {
        if (this.hovered != null) {
            poseStack.pushPose();
            poseStack.translate(mouseX - translationX, mouseY - translationY, 1);
            RenderUtils.drawTooltip(
                    poseStack,
                    this.hovered.tooltipList(),
                    FontRenderer.getInstance().getFont(),
                    true);
            poseStack.popPose();
        }
    }

    private void renderButtons(PoseStack poseStack, int mouseX, int mouseY) {
        int buttonCount = buttons.size();

        poseStack.pushPose();
        poseStack.translate(20, 65, 0);

        final float translationX = (this.width - Texture.QUEST_BOOK_BACKGROUND.width()) / 2f + 20;
        final float translationY = (this.height - Texture.QUEST_BOOK_BACKGROUND.height()) / 2f + 65;

        mouseX -= translationX;
        mouseY -= translationY;

        this.hovered = null;

        for (int i = 0; i < buttonCount; i++) {
            final int x = i % 4 * (BUTTON_SIZE + 5);
            final int y = (i / 4) * (BUTTON_SIZE + 5);

            boolean hovered = x <= mouseX && x + BUTTON_SIZE >= mouseX && y <= mouseY && y + BUTTON_SIZE >= mouseY;

            RenderUtils.drawRect(
                    poseStack, hovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR, x, y, 0, BUTTON_SIZE, BUTTON_SIZE);
            WynntilsMenuButton button = buttons.get(i);
            Texture texture = button.buttonTexture();

            if (hovered) {
                this.hovered = button;
            }

            if (!button.dynamicTexture()) {
                RenderUtils.drawTexturedRect(
                        poseStack,
                        texture.resource(),
                        x + (BUTTON_SIZE - texture.width()) / 2f,
                        y + (BUTTON_SIZE - texture.height()) / 2f,
                        1,
                        texture.width(),
                        texture.height(),
                        0,
                        0,
                        texture.width(),
                        texture.height(),
                        texture.width(),
                        texture.height());
                continue;
            }

            if (hovered) {
                RenderUtils.drawTexturedRect(
                        poseStack,
                        texture.resource(),
                        x + (BUTTON_SIZE - texture.width()) / 2f,
                        y + (BUTTON_SIZE - texture.height() / 2f) / 2f,
                        1,
                        texture.width(),
                        texture.height() / 2f,
                        0,
                        texture.height() / 2,
                        texture.width(),
                        texture.height() / 2,
                        texture.width(),
                        texture.height());
            } else {
                RenderUtils.drawTexturedRect(
                        poseStack,
                        texture.resource(),
                        x + (BUTTON_SIZE - texture.width()) / 2f,
                        y + (BUTTON_SIZE - texture.height() / 2f) / 2f,
                        1,
                        texture.width(),
                        texture.height() / 2f,
                        0,
                        0,
                        texture.width(),
                        texture.height() / 2,
                        texture.width(),
                        texture.height());
            }
        }

        poseStack.popPose();
    }
}
