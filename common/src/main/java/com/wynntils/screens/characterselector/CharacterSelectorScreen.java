/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.characterselector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.ClassInfo;
import com.wynntils.screens.characterselector.widgets.ChangeWorldButton;
import com.wynntils.screens.characterselector.widgets.ClassInfoButton;
import com.wynntils.screens.characterselector.widgets.ClassSelectionAddButton;
import com.wynntils.screens.characterselector.widgets.ClassSelectionDeleteButton;
import com.wynntils.screens.characterselector.widgets.ClassSelectionEditButton;
import com.wynntils.screens.characterselector.widgets.DisconnectButton;
import com.wynntils.screens.characterselector.widgets.PlayButton;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class CharacterSelectorScreen extends WynntilsScreen {
    private static final int CHARACTER_INFO_PER_PAGE = 7;

    private final AbstractContainerScreen<?> actualClassSelectionScreen;
    private List<ClassInfo> classInfoList = new ArrayList<>();
    private final List<ClassInfoButton> classInfoButtons = new ArrayList<>();
    private int firstNewCharacterSlot = -1;
    private float currentTextureScale = 1f;

    private int scrollOffset = 0;
    private boolean draggingScroll = false;
    private double lastMouseY = 0;
    private double mouseDrag = 0;
    private ClassInfoButton selected = null;

    private CharacterSelectorScreen() {
        super(Component.translatable("screens.wynntils.characterSelection.name"));

        if (McUtils.mc().screen instanceof AbstractContainerScreen<?> abstractContainerScreen) {
            actualClassSelectionScreen = abstractContainerScreen;
        } else {
            throw new IllegalStateException(
                    "Tried to open custom character selection screen when normal character selection screen is not open");
        }
    }

    public static Screen create() {
        return new CharacterSelectorScreen();
    }

    @Override
    public void onClose() {
        ContainerUtils.closeContainer(actualClassSelectionScreen.getMenu().containerId);
        super.onClose();
    }

    @Override
    protected void doInit() {
        currentTextureScale = (float) this.height / Texture.LIST_BACKGROUND.height();

        float listWidth = Texture.LIST_BACKGROUND.width() * currentTextureScale;

        float playButtonWidth = Texture.PLAY_BUTTON.width() * currentTextureScale;
        float playButtonHeight = Texture.PLAY_BUTTON.height() / 2f * currentTextureScale;
        this.addRenderableWidget(new PlayButton(
                (int) (this.width - playButtonWidth - (10f * currentTextureScale)),
                (int) (this.height - playButtonHeight - (10f * currentTextureScale)),
                (int) playButtonWidth,
                (int) playButtonHeight,
                this));

        float deleteButtonWidth = Texture.REMOVE_BUTTON.width() * currentTextureScale;
        float deleteButtonHeight = Texture.REMOVE_BUTTON.height() / 2f * currentTextureScale;
        this.addRenderableWidget(new ClassSelectionDeleteButton(
                (int) (listWidth * 0.6f),
                (int) (this.height * 0.915f),
                (int) deleteButtonWidth,
                (int) deleteButtonHeight,
                this));

        float editButtonWidth = Texture.EDIT_BUTTON.width() * currentTextureScale;
        float editButtonHeight = Texture.EDIT_BUTTON.height() * currentTextureScale;
        this.addRenderableWidget(new ClassSelectionEditButton(
                (int) (listWidth * 0.44f),
                (int) (this.height * 0.915f),
                (int) editButtonWidth,
                (int) editButtonHeight,
                this));

        float addButtonWidth = Texture.ADD_BUTTON.width() * currentTextureScale;
        float addButtonHeight = Texture.ADD_BUTTON.height() / 2f * currentTextureScale;
        this.addRenderableWidget(new ClassSelectionAddButton(
                (int) (listWidth * 0.22f),
                (int) (this.height * 0.915f),
                (int) addButtonWidth,
                (int) addButtonHeight,
                this));

        float disconnectButtonWidth = Texture.DISCONNECT_BUTTON.width() * currentTextureScale;
        float disconnectButtonHeight = Texture.DISCONNECT_BUTTON.height() / 2f * currentTextureScale;
        this.addRenderableWidget(new DisconnectButton(
                (int) (this.width - disconnectButtonWidth - (10f * currentTextureScale)),
                (int) (10f * currentTextureScale),
                (int) disconnectButtonWidth,
                (int) disconnectButtonHeight,
                this));

        float changeWorldButtonWidth = Texture.CHANGE_WORLD_BUTTON.width() * currentTextureScale;
        float changeWorldButtonHeight = Texture.CHANGE_WORLD_BUTTON.height() / 2f * currentTextureScale;
        this.addRenderableWidget(new ChangeWorldButton(
                (int) (this.width - changeWorldButtonWidth - (10f * currentTextureScale)),
                (int) ((15f * currentTextureScale) + changeWorldButtonHeight),
                (int) changeWorldButtonWidth,
                (int) changeWorldButtonHeight,
                this));

        reloadButtons();
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (draggingScroll) {
            mouseDrag += mouseY - lastMouseY;
            lastMouseY = mouseY;

            if (Math.abs(mouseDrag) > this.height / CHARACTER_INFO_PER_PAGE) {
                boolean positive = mouseDrag > 0;

                mouseDrag += (positive ? -1 : 1) * this.height / CHARACTER_INFO_PER_PAGE;
                setScrollOffset(positive ? -1 : 1);
            }
        }

        this.renderBackground(poseStack);

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.LIST_BACKGROUND.resource(),
                0,
                0,
                0,
                Texture.LIST_BACKGROUND.width() * currentTextureScale,
                Texture.LIST_BACKGROUND.height() * currentTextureScale,
                Texture.LIST_BACKGROUND.width(),
                Texture.LIST_BACKGROUND.height());

        renderWidgets(poseStack, mouseX, mouseY, partialTick);

        renderScrollButton(poseStack);

        renderPlayer(poseStack);

        if (selected == null) return;

        renderCharacterInfo(poseStack);
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.BACKGROUND_SPLASH.resource(),
                0,
                0,
                0,
                this.width,
                this.height,
                Texture.BACKGROUND_SPLASH.width(),
                Texture.BACKGROUND_SPLASH.height());
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener child : children()) {
            child.mouseClicked(mouseX, mouseY, button);
        }

        for (ClassInfoButton classInfoButton : classInfoButtons) {
            if (classInfoButton.isMouseOver(mouseX, mouseY)) {
                classInfoButton.mouseClicked(mouseX, mouseY, button);
                this.selected = classInfoButton;
            }
        }

        if (!draggingScroll) {
            float scrollButtonRenderX = Texture.LIST_BACKGROUND.width() * currentTextureScale * 0.916f;
            float scrollButtonRenderY = MathUtils.map(
                    scrollOffset,
                    0,
                    classInfoList.size() - CHARACTER_INFO_PER_PAGE,
                    Texture.LIST_BACKGROUND.height() * currentTextureScale * 0.01f,
                    Texture.LIST_BACKGROUND.height() * currentTextureScale * 0.92f);

            if (mouseX >= scrollButtonRenderX
                    && mouseX
                            <= scrollButtonRenderX
                                    + Texture.CHARACTER_SELECTION_SCROLL_BUTTON.width() * currentTextureScale
                    && mouseY >= scrollButtonRenderY
                    && mouseY
                            <= scrollButtonRenderY
                                    + Texture.CHARACTER_SELECTION_SCROLL_BUTTON.height() * currentTextureScale) {
                draggingScroll = true;
                lastMouseY = mouseY;
            }
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        setScrollOffset((int) delta);

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        KeyMapping[] keyHotbarSlots = McUtils.options().keyHotbarSlots;

        for (int i = 0; i < Math.min(keyHotbarSlots.length, classInfoList.size()); i++) {
            if (!keyHotbarSlots[i].matches(keyCode, scanCode)) continue;
            int slot = classInfoList.get(i).slot();
            Models.CharacterSelection.playWithCharacter(slot);
            return true;
        }

        return true;
    }

    private void renderCharacterInfo(PoseStack poseStack) {
        float renderWidth = Texture.CHARACTER_INFO.width() * currentTextureScale;
        float renderHeight = Texture.CHARACTER_INFO.height() * currentTextureScale;
        float renderX = (this.width * 0.6f) - renderWidth / 2f;
        float renderY = this.height / 8f;

        poseStack.pushPose();
        poseStack.translate(renderX, renderY, 0);

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.CHARACTER_INFO.resource(),
                0,
                0,
                0,
                renderWidth,
                renderHeight,
                Texture.CHARACTER_INFO.width(),
                Texture.CHARACTER_INFO.height());
        float offsetX = renderWidth * 0.028f;
        float offsetY = renderHeight * 0.02f;
        float scale = this.height * 0.0035f;

        poseStack.translate(offsetX, offsetY, 0);

        RenderUtils.drawProgressBar(
                poseStack,
                Texture.XP_BAR,
                renderWidth * 0.05f,
                renderHeight * 0.7f,
                renderWidth * 0.8f,
                renderHeight * 0.8f,
                0,
                0,
                Texture.XP_BAR.width(),
                Texture.XP_BAR.height(),
                selected.getClassInfo().xp() / 100f);

        poseStack.pushPose();
        poseStack.translate(renderWidth * 0.08f, renderHeight * 0.15f, 0);

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.SOUL_POINT_ICON.resource(),
                0,
                0,
                0,
                Texture.SOUL_POINT_ICON.width() * currentTextureScale,
                Texture.SOUL_POINT_ICON.height() * currentTextureScale,
                Texture.SOUL_POINT_ICON.width(),
                Texture.SOUL_POINT_ICON.height());

        poseStack.pushPose();
        poseStack.scale(scale, scale, 0f);
        poseStack.translate(renderWidth * 0.125f / scale, 0, 0);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(
                                String.valueOf(this.selected.getClassInfo().soulPoints())),
                        0,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        poseStack.popPose();

        poseStack.translate(renderWidth * 0.27f, 0, 0);
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.QUESTS_ICON.resource(),
                0,
                0,
                0,
                Texture.QUESTS_ICON.width() * currentTextureScale,
                Texture.QUESTS_ICON.height() * currentTextureScale,
                Texture.QUESTS_ICON.width(),
                Texture.QUESTS_ICON.height());

        poseStack.pushPose();
        poseStack.scale(scale, scale, 0f);
        poseStack.translate(renderWidth * 0.15f / scale, 0, 0);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(
                                String.valueOf(this.selected.getClassInfo().completedQuests())),
                        0,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        poseStack.popPose();

        poseStack.translate(renderWidth * 0.32f, 0, 0);
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.CHALLENGES_ICON.resource(),
                0,
                0,
                0,
                Texture.CHALLENGES_ICON.width() * currentTextureScale,
                Texture.CHALLENGES_ICON.height() * currentTextureScale,
                Texture.CHALLENGES_ICON.width(),
                Texture.CHALLENGES_ICON.height());
        // FIXME: Render character challenge mode info here

        poseStack.popPose();
        poseStack.popPose();
    }

    private void renderScrollButton(PoseStack poseStack) {
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.CHARACTER_SELECTION_SCROLL_BUTTON.resource(),
                Texture.LIST_BACKGROUND.width() * currentTextureScale * 0.916f,
                MathUtils.map(
                        scrollOffset,
                        0,
                        classInfoList.size() - CHARACTER_INFO_PER_PAGE,
                        Texture.LIST_BACKGROUND.height() * currentTextureScale * 0.01f,
                        Texture.LIST_BACKGROUND.height() * currentTextureScale * 0.92f),
                0,
                Texture.CHARACTER_SELECTION_SCROLL_BUTTON.width() * currentTextureScale,
                Texture.CHARACTER_SELECTION_SCROLL_BUTTON.height() * currentTextureScale,
                Texture.CHARACTER_SELECTION_SCROLL_BUTTON.width(),
                Texture.CHARACTER_SELECTION_SCROLL_BUTTON.height());
    }

    private void renderWidgets(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        for (Renderable renderable : this.renderables) {
            renderable.render(poseStack, mouseX, mouseY, partialTick);
        }

        for (ClassInfoButton classInfoButton : classInfoButtons) {
            classInfoButton.render(poseStack, mouseX, mouseY, partialTick);
        }
    }

    private void renderPlayer(PoseStack poseStack) {
        McUtils.player().setInvisible(false);
        // This is actually needed...
        McUtils.player().resetFallDistance();
        McUtils.player().setSwimming(false);

        int scale = this.height / 4;
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                poseStack, (int) (this.width * 0.6f), (int) (this.height * 0.85f), scale, 0, 0, McUtils.player());
    }

    private void setScrollOffset(int delta) {
        scrollOffset =
                MathUtils.clamp(scrollOffset - delta, 0, Math.max(0, classInfoList.size() - CHARACTER_INFO_PER_PAGE));

        reloadButtons();
    }

    private void reloadButtons() {
        classInfoButtons.clear();

        final float width = Texture.LIST_BACKGROUND.width() * currentTextureScale * 0.9f - 3;
        final int height = Math.round(Texture.LIST_BACKGROUND.height() * currentTextureScale / 8f);
        for (int i = scrollOffset; i < Math.min(classInfoList.size(), scrollOffset + CHARACTER_INFO_PER_PAGE); i++) {
            ClassInfo classInfo = classInfoList.get(i);
            ClassInfoButton newButton = new ClassInfoButton(
                    5, (5 + (classInfoButtons.size() * height)), (int) width, height, classInfo, this);
            classInfoButtons.add(newButton);

            if (selected != null && selected.getClassInfo() == classInfo) {
                selected = newButton;
            }
        }
    }

    public void setFirstNewCharacterSlot(int firstNewCharacterSlot) {
        this.firstNewCharacterSlot = firstNewCharacterSlot;
    }

    public void setClassInfoList(List<ClassInfo> classInfoList) {
        firstNewCharacterSlot = -1;

        this.classInfoList = classInfoList;

        reloadButtons();
    }

    public ClassInfoButton getSelected() {
        return selected;
    }

    public AbstractContainerScreen<?> getActualClassSelectionScreen() {
        return actualClassSelectionScreen;
    }

    public int getFirstNewCharacterSlot() {
        return firstNewCharacterSlot;
    }
}
