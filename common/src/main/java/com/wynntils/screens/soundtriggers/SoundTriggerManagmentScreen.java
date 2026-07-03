/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.soundtriggers;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.utilities.SoundTriggersFeature;
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.screens.soundtriggers.widgets.TriggerButton;
import com.wynntils.screens.soundtriggers.widgets.TriggerSettingButton;
import com.wynntils.screens.soundtriggers.widgets.TriggerSettingFunctionInput;
import com.wynntils.screens.soundtriggers.widgets.TriggerSettingNumberInput;
import com.wynntils.screens.soundtriggers.widgets.TriggerSideButton;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.soundtriggers.SoundTrigger;
import com.wynntils.utils.soundtriggers.TriggerType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class SoundTriggerManagmentScreen extends WynntilsScreen {
    private static final Texture BACKGROUND_TEXTURE = Texture.OVERLAY_SELECTION_GUI;
    private static final Texture SIDE_BUTTON_TEXTURE = Texture.BUTTON_LEFT;
    private static final Texture SCROLL_TEXTURE = Texture.SCROLL_BUTTON;
    private static final int MAX_TRIGGERS_PER_PAGE = 8;
    private static final int CONFIG_MASK_TOP_Y = 25;
    private static final int CONFIG_MASK_BOTTOM_Y = 197;

    private final Screen previousScreen;

    public final Storage<List<SoundTrigger>> soundTriggers;

    private final SearchWidget searchWidget;

    private StyledText[] text = {
        StyledText.fromString("Select a Sound Trigger from the list on the left or add new one."),
        StyledText.fromString(""),
        StyledText.fromString("Clicking on selected trigger will let you rename it.")
    };

    private final List<TriggerButton> triggerButtons = new ArrayList<>();
    private SoundTrigger selectedTrigger = null;
    private int scrollOffset = 0;
    private boolean scrolling = false;
    private float scrollY;

    private TriggerSideButton addButton;
    private TriggerSideButton deleteButton;

    // region Setting Widgets
    private static final int GAP_SIZE = 2;

    private int settingX;
    private int settingY;
    private int settingWidth;
    private int settingHeight;

    private TriggerSettingButton enabledButton;
    private TriggerSettingButton typeButton;
    private TriggerSettingFunctionInput controllerFunctionField;
    private TriggerSettingFunctionInput identifierFunctionField;
    private TriggerSettingNumberInput volumeField;
    private TriggerSettingNumberInput pitchField;
    private TriggerSettingNumberInput intervalField;
    // endregion

    private SoundTriggerManagmentScreen(Screen previousScreen) {
        super(Component.literal("Sound Triggers Managment Screen"));
        this.previousScreen = previousScreen;
        this.soundTriggers =
                Managers.Feature.getFeatureInstance(SoundTriggersFeature.class).getRegisteredTriggers();

        this.searchWidget = new SearchWidget(
                7 + getTranslationXint(),
                6 + getTranslationYint(),
                120,
                20,
                (s) -> {
                    scrollOffset = 0;
                    populateTriggers();
                },
                this);
    }

    public static Screen screen(Screen previousScreen) {
        return new SoundTriggerManagmentScreen(previousScreen);
    }

    @Override
    protected void doInit() {
        searchWidget.setX(7 + getTranslationXint());
        searchWidget.setY(6 + getTranslationYint());
        this.addButton = new TriggerSideButton(
                getTranslationXint() - SIDE_BUTTON_TEXTURE.width() + 4,
                getTranslationYint() + 28,
                SIDE_BUTTON_TEXTURE.width(),
                SIDE_BUTTON_TEXTURE.height() / 2,
                SIDE_BUTTON_TEXTURE,
                i -> {
                    SoundTrigger newTrigger =
                            new SoundTrigger("", "", "New Sound Trigger " + (triggerButtons.size() + 1), 100);
                    soundTriggers.get().add(newTrigger);
                    soundTriggers.touched();
                    setSelectedTrigger(newTrigger);
                },
                Collections.singletonList(Component.literal("Add trigger")),
                StyledText.fromString("Add"));

        this.deleteButton = new TriggerSideButton(
                getTranslationXint() - SIDE_BUTTON_TEXTURE.width() + 4,
                getTranslationYint() + BACKGROUND_TEXTURE.height() - SIDE_BUTTON_TEXTURE.height() / 2 - 7,
                SIDE_BUTTON_TEXTURE.width(),
                SIDE_BUTTON_TEXTURE.height() / 2,
                SIDE_BUTTON_TEXTURE,
                i -> deleteTrigger(),
                Collections.singletonList(Component.literal("Add trigger")),
                StyledText.fromString("Delete"));

        this.addRenderableWidget(searchWidget);
        populateTriggers();

        setFocusedTextInput(searchWidget);

        // region Settings
        settingX = getTranslationXint() + 145;
        settingY = getTranslationYint() + 29;
        settingWidth = BACKGROUND_TEXTURE.width() / 2 + 15;
        settingHeight = (BACKGROUND_TEXTURE.height() / 2 + 66) / 4;

        this.enabledButton = new TriggerSettingButton(
                settingX + GAP_SIZE,
                settingY + GAP_SIZE,
                settingWidth / 2 - GAP_SIZE,
                settingHeight - GAP_SIZE,
                StyledText.fromString("Enabled"),
                List.of(Component.literal("Toggle enabled")),
                trigger -> StyledText.fromComponent(Component.literal(String.valueOf(trigger.isEnabled()))
                        .withColor(trigger.isEnabled() ? CommonColors.GREEN.asInt() : CommonColors.RED.asInt())),
                trigger -> trigger.setEnabled(!trigger.isEnabled()),
                selectedTrigger);
        this.addRenderableWidget(enabledButton);

        this.typeButton = new TriggerSettingButton(
                settingX + settingWidth / 2 + GAP_SIZE,
                settingY + GAP_SIZE,
                settingWidth / 2 - GAP_SIZE * 2,
                settingHeight - GAP_SIZE,
                StyledText.fromString("Type"),
                List.of(Component.literal("Change type")),
                trigger -> StyledText.fromString(EnumUtils.toNiceString(trigger.getType())),
                trigger -> trigger.setType(
                        trigger.getType() == TriggerType.SINGULAR ? TriggerType.CONTINUOUS : TriggerType.SINGULAR),
                selectedTrigger);
        this.addRenderableWidget(typeButton);

        this.controllerFunctionField = new TriggerSettingFunctionInput(
                settingX + GAP_SIZE,
                settingY + settingHeight + GAP_SIZE,
                settingWidth - GAP_SIZE,
                settingHeight - GAP_SIZE / 2,
                StyledText.fromString("Controller Function"),
                List.of(Component.literal("Controller")),
                SoundTrigger::getControllerFunction,
                (string, trigger) -> trigger.setControllerFunction(string),
                (trigger -> trigger.getControllerFunctionResult().hasError()),
                this,
                selectedTrigger);
        this.addRenderableWidget(controllerFunctionField);

        this.identifierFunctionField = new TriggerSettingFunctionInput(
                settingX + GAP_SIZE,
                settingY + settingHeight * 2 + GAP_SIZE / 2,
                settingWidth - GAP_SIZE,
                settingHeight - GAP_SIZE / 2,
                StyledText.fromString("Identifier Function"),
                List.of(Component.literal("Identifier")),
                SoundTrigger::getIdentifierFunction,
                (string, trigger) -> trigger.setIdentifierFunction(string),
                (trigger -> trigger.getIdentifierFunctionResult().hasError()),
                this,
                selectedTrigger);
        this.addRenderableWidget(identifierFunctionField);

        this.volumeField = new TriggerSettingNumberInput(
                settingX + GAP_SIZE,
                settingY + settingHeight * 3 + GAP_SIZE / 2,
                settingWidth / 3 - GAP_SIZE / 2,
                settingHeight - GAP_SIZE,
                StyledText.fromString("Volume"),
                List.of(Component.literal("Set Volume")),
                SoundTrigger::getVolume,
                (string, trigger) -> trigger.setVolume(Integer.parseInt(string)),
                this,
                selectedTrigger);
        this.addRenderableWidget(volumeField);

        this.pitchField = new TriggerSettingNumberInput(
                settingX + settingWidth / 3 + GAP_SIZE / 2,
                settingY + settingHeight * 3 + GAP_SIZE / 2,
                settingWidth / 3 - GAP_SIZE / 2,
                settingHeight - GAP_SIZE,
                StyledText.fromString("Pitch"),
                List.of(Component.literal("Set Pitch")),
                SoundTrigger::getPitch,
                (string, trigger) -> trigger.setPitch(Integer.parseInt(string)),
                this,
                selectedTrigger);
        this.addRenderableWidget(pitchField);

        this.intervalField = new TriggerSettingNumberInput(
                settingX + (settingWidth / 3) * 2 + GAP_SIZE / 2,
                settingY + settingHeight * 3 + GAP_SIZE / 2,
                settingWidth / 3 - GAP_SIZE,
                settingHeight - GAP_SIZE,
                StyledText.fromString("Interval"),
                List.of(Component.literal("Set Interval, only for Continuous type.")),
                SoundTrigger::getInterval,
                (string, trigger) -> trigger.setInterval(Integer.parseInt(string)),
                this,
                selectedTrigger);
        this.addRenderableWidget(intervalField);

        // endregion
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        addButton.render(guiGraphics, mouseX, mouseY, partialTick);
        if (selectedTrigger != null) {
            deleteButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        RenderUtils.drawTexturedRect(guiGraphics, BACKGROUND_TEXTURE, getTranslationX(), getTranslationY());

        searchWidget.render(guiGraphics, mouseX, mouseY, partialTick);

        if (selectedTrigger == null) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            text,
                            getTranslationXint() + 145,
                            getTranslationXint() + 145 + BACKGROUND_TEXTURE.width() / 2f + 15,
                            getTranslationYint() + 29,
                            getTranslationYint() + 29 + BACKGROUND_TEXTURE.height() / 2f + 66,
                            (BACKGROUND_TEXTURE.width() / 2f + 15) / 1.1f,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            1.1f);
        }
        //        RenderUtils.drawRect(
        //                guiGraphics,
        //                CommonColors.WHITE.withAlpha(0.6f),
        //                getTranslationXint() + 145,
        //                getTranslationYint() + 29,
        //                BACKGROUND_TEXTURE.width() / 2f + 15,
        //                BACKGROUND_TEXTURE.height() / 2f + 66);

        RenderUtils.enableScissor(
                guiGraphics, 6 + getTranslationXint(), 28 + getTranslationYint(), 122, MAX_TRIGGERS_PER_PAGE * 21 + 2);
        for (TriggerButton button : triggerButtons) {
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        RenderUtils.disableScissor(guiGraphics);

        if (triggerButtons.size() > MAX_TRIGGERS_PER_PAGE) {
            renderScroll(guiGraphics);
        }

        if (scrolling) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        }

        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previousScreen);
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!scrolling && triggerButtons.size() > MAX_TRIGGERS_PER_PAGE) {
            if (MathUtils.isInside(
                    (int) event.x(),
                    (int) event.y(),
                    getTranslationXint() + 133,
                    getTranslationXint() + 133 + SCROLL_TEXTURE.width(),
                    (int) scrollY,
                    (int) (scrollY + SCROLL_TEXTURE.height()))) {
                scrolling = true;
                return true;
            }
        }

        for (GuiEventListener listener : getAllWidgets()) {
            if (listener.isMouseOver(event.x(), event.y())) {
                listener.mouseClicked(event, isDoubleClick);
                System.out.println("Clicking " + listener.getClass().getSimpleName());
                return true;
            }
        }

        return super.doMouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        if (scrolling) {
            int scrollAreaStartY = 24 + 10 + getTranslationYint();
            int scrollAreaHeight = MAX_TRIGGERS_PER_PAGE * 21 - SCROLL_TEXTURE.height();

            int newOffset = Math.round(MathUtils.map(
                    (float) event.y(),
                    scrollAreaStartY,
                    scrollAreaStartY + scrollAreaHeight,
                    0,
                    getMaxTriggerScrollOffset()));

            newOffset = Math.clamp(newOffset, 0, getMaxTriggerScrollOffset());

            scrollTriggers(newOffset);

            return true;
        }

        return super.mouseDragged(event, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int scrollAmount = (int) (-scrollY * 10f);

        scrollTriggers(Math.clamp(this.scrollOffset + scrollAmount, 0, getMaxTriggerScrollOffset()));

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for (GuiEventListener listener : getAllWidgets()) {
            if (listener.isMouseOver(event.x(), event.y())) {
                listener.mouseReleased(event);
                return true;
            }
        }

        scrolling = false;

        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if ((getFocusedTextInput() == searchWidget || getFocusedTextInput() == null)
                && event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        for (TriggerButton button : triggerButtons) {
            if (button.keyPressed(event)) return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void added() {
        searchWidget.opened();
        super.added();
    }

    private void populateTriggers() {
        for (TriggerButton button : triggerButtons) {
            this.removeWidget(button);
        }

        triggerButtons.clear();

        String search = searchWidget.getTextBoxInput();
        List<SoundTrigger> sortedTriggers = soundTriggers.get().stream()
                .filter(trigger -> trigger.getName().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))
                .sorted(SoundTrigger::compareTo)
                .toList();

        int yPos = 31 + getTranslationYint();
        for (SoundTrigger trigger : sortedTriggers) {
            triggerButtons.add(new TriggerButton(7 + getTranslationXint(), yPos, 120, 18, trigger, this));
            yPos += 22;
        }

        scrollTriggers(scrollOffset);
    }

    private void scrollTriggers(int newOffset) {
        scrollOffset = newOffset;

        for (TriggerButton trigger : triggerButtons) {
            int newY = 31 + getTranslationYint() + (triggerButtons.indexOf(trigger) * 21) - scrollOffset;

            trigger.setY(newY);
            trigger.visible = newY >= (31 + getTranslationY() - 21)
                    && newY <= (31 + getTranslationY() + (MAX_TRIGGERS_PER_PAGE) * 21);
        }
    }

    private float getTranslationX() {
        return (this.width - BACKGROUND_TEXTURE.width()) / 2f;
    }

    private float getTranslationY() {
        return (this.height - BACKGROUND_TEXTURE.height()) / 2f;
    }

    private int getTranslationXint() {
        return (int) getTranslationX();
    }

    private int getTranslationYint() {
        return (int) getTranslationY();
    }

    private void deleteTrigger() {
        if (selectedTrigger == null) return;

        soundTriggers.get().remove(selectedTrigger);
        soundTriggers.touched();
        setSelectedTrigger(null);
        scrollTriggers(0);
        populateTriggers();
    }

    private List<GuiEventListener> getAllWidgets() {
        List<GuiEventListener> list = new ArrayList<>(triggerButtons);
        list.add(addButton);
        list.add(deleteButton);
        if (selectedTrigger != null) {
            list.addAll(children());
        }
        return list;
    }

    private void renderScroll(GuiGraphics guiGraphics) {
        scrollY = 24
                + getTranslationYint()
                + MathUtils.map(
                        scrollOffset,
                        0,
                        getMaxTriggerScrollOffset(),
                        0,
                        177 - Texture.CONFIG_BOOK_SCROLL_BUTTON.height());

        RenderUtils.drawTexturedRect(guiGraphics, SCROLL_TEXTURE, 133 + getTranslationX(), scrollY);
    }

    private int getMaxTriggerScrollOffset() {
        return (triggerButtons.size() - MAX_TRIGGERS_PER_PAGE) * 21;
    }

    public SoundTrigger getSelectedTrigger() {
        return selectedTrigger;
    }

    public void setSelectedTrigger(SoundTrigger selectedTrigger) {
        if (this.selectedTrigger == selectedTrigger) return;

        if (this.selectedTrigger != null) {
            for (TriggerButton button : triggerButtons) {
                if (button.getTrigger() == this.selectedTrigger) {
                    button.hideEditInput();
                    break;
                }
            }
        }
        this.selectedTrigger = selectedTrigger;

        enabledButton.setTrigger(selectedTrigger);
        typeButton.setTrigger(selectedTrigger);
        controllerFunctionField.setTrigger(selectedTrigger);
        identifierFunctionField.setTrigger(selectedTrigger);
        volumeField.setTrigger(selectedTrigger);
        pitchField.setTrigger(selectedTrigger);
        intervalField.setTrigger(selectedTrigger);

        populateTriggers();
    }

    public int getConfigMaskTopY() {
        return getTranslationYint() + CONFIG_MASK_TOP_Y;
    }

    public int getConfigMaskBottomY() {
        return getTranslationYint() + CONFIG_MASK_BOTTOM_Y;
    }
}
