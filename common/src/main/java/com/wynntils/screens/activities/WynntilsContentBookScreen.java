/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.wrappedscreen.WrappedScreen;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.models.activities.type.ActivityInfo;
import com.wynntils.models.activities.type.ActivityRewardType;
import com.wynntils.screens.activities.widgets.ContentBookActionWidget;
import com.wynntils.screens.activities.widgets.ContentBookRewardWidget;
import com.wynntils.screens.activities.widgets.ContentBookScrollButton;
import com.wynntils.screens.activities.widgets.ContentBookSearchWidget;
import com.wynntils.screens.activities.widgets.ContentBookWidget;
import com.wynntils.screens.activities.widgets.WynntilsMenuTag;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

public class WynntilsContentBookScreen extends WynntilsScreen implements WrappedScreen {
    private static final Integer MAX_COLUMNS = 3;
    private static final Integer MAX_ROWS = 6;
    private static final int WIDGETS_PER_PAGE = MAX_ROWS * MAX_COLUMNS;

    private static final StyledText VARIOUS_EMERALDS = StyledText.fromString("Various Emeralds");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+) (?:.*)");

    private static final ResourceLocation BUTTON_CLICK_ID = ResourceLocation.withDefaultNamespace("wynn.ui.click");
    private static final SoundEvent BUTTON_CLICK_SOUND = SoundEvent.createVariableRangeEvent(BUTTON_CLICK_ID);

    private List<List<ContentBookWidget>> contentBookWidgets = new ArrayList<>();
    private final Map<Integer, ContentBookActionWidget> contentBookActionWidgets = new TreeMap<>();

    private ActivityInfo trackedActivity = null;
    private boolean goToFinalPage = false;
    private int currentPage = 0;
    private StyledText trackedDescription = null;
    private List<ContentBookRewardWidget> trackedRewards = new ArrayList<>();

    private boolean scrollUpActive = false;
    private boolean scrollDownActive = false;
    private int offsetX;
    private int offsetY;
    private int trackedRewardsX;

    private ContentBookScrollButton scrollUpButton;
    private ContentBookScrollButton scrollDownButton;
    private SearchWidget searchWidget;

    // WrappedScreen fields
    private final WrappedScreenInfo wrappedScreenInfo;
    private final ContentBookHolder holder;

    public WynntilsContentBookScreen(WrappedScreenInfo wrappedScreenInfo, ContentBookHolder holder) {
        super(Component.translatable("screens.wynntils.content.name"));
        this.wrappedScreenInfo = wrappedScreenInfo;
        this.holder = holder;
    }

    @Override
    protected void doInit() {
        super.doInit();

        offsetX = (int) ((this.width - Texture.CUSTOM_CONTENT_BOOK_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.CUSTOM_CONTENT_BOOK_BACKGROUND.height()) / 2f);

        searchWidget = new ContentBookSearchWidget(
                offsetX + 29, offsetY + 6, 235, 20, (s) -> reloadContentBookWidgets(false), this);
        this.addRenderableWidget(searchWidget);

        scrollUpButton = new ContentBookScrollButton(
                offsetX + 173,
                offsetY + 153,
                16,
                16,
                Texture.UP_COLORED_ICON,
                (b) -> {
                    if (currentPage == 0) {
                        holder.scrollUp();
                        goToFinalPage = true;
                    } else {
                        scrollPage(-1);
                    }
                },
                List.of(Component.literal("Scroll Up")));
        this.addRenderableWidget(scrollUpButton);

        scrollDownButton = new ContentBookScrollButton(
                offsetX + 245,
                offsetY + 153,
                16,
                16,
                Texture.DOWN_COLORED_ICON,
                (b) -> {
                    if (currentPage == contentBookWidgets.size() - 1) {
                        holder.scrollDown();
                        goToFinalPage = false;
                    } else {
                        scrollPage(1);
                    }
                },
                List.of(Component.literal("Scroll Down")));
        this.addRenderableWidget(scrollDownButton);

        this.addRenderableWidget(new WynntilsMenuTag(offsetX + 333, offsetY + 151, (b) -> {
            // We should call McUtils.player().closeContainer() here but that will cause the mouse to recenter as that
            // will close the screen before opening the new one which is a bit disorienting
            McUtils.sendPacket(new ServerboundContainerClosePacket(wrappedScreenInfo.containerId()));
            McUtils.player().containerMenu = McUtils.player().inventoryMenu;
            McUtils.setScreen(WynntilsMenuScreen.create());
        }));

        holder.reloadActions();
        reloadContentBookWidgets(true);
    }

    @Override
    public void onClose() {
        McUtils.player().closeContainer();
        super.onClose();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        PoseStack poseStack = guiGraphics.pose();

        renderBackgroundTexture(poseStack);

        contentBookWidgets
                .get(currentPage)
                .forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));
        contentBookActionWidgets
                .values()
                .forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));

        renderables.forEach(renderable -> renderable.render(guiGraphics, mouseX, mouseY, partialTick));

        if (!Models.CombatXp.getCombatLevel().isAtCap()) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(Component.translatable(
                                    "screens.wynntils.contentBook.xpToLevelUp",
                                    String.format(Locale.ROOT, "%,d", Models.CombatXp.getXpPointsNeededToLevelUp()),
                                    (Models.CombatXp.getCombatLevel().current() + 1))),
                            offsetX + 18,
                            offsetY + 160,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

        if (trackedActivity != null) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(
                                    EnumUtils.toNiceString(trackedActivity.type()) + " - " + trackedActivity.name()),
                            offsetX + 24,
                            offsetY + 178,
                            trackedActivity.type().getColor(),
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NORMAL,
                            0.9f);

            if (trackedDescription != null) {
                FontRenderer.getInstance()
                        .renderAlignedTextInBox(
                                poseStack,
                                trackedDescription,
                                offsetX + 24,
                                offsetX + 410,
                                offsetY + 188,
                                offsetY + 208,
                                386,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.TOP,
                                TextShadow.NORMAL,
                                0.9f);
            }

            if (!trackedRewards.isEmpty()) {
                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                StyledText.fromComponent(
                                        Component.translatable("screens.wynntils.contentBook.rewards")),
                                trackedRewardsX,
                                offsetY + 213,
                                CustomColor.fromChatFormatting(ChatFormatting.LIGHT_PURPLE),
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.TOP,
                                TextShadow.NORMAL,
                                0.9f);

                trackedRewards.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
            }
        } else {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.contentBook.selectToTrack")),
                            offsetX + 4,
                            offsetX + 410,
                            offsetY + 178,
                            offsetY + 213,
                            386,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }

        renderTooltips(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener listener : getWidgetsForIteration().toList()) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return listener.mouseClicked(mouseX, mouseY, button);
            }
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        double scrollValue = -Math.signum(deltaY);

        if (scrollValue == 1.0) {
            if (currentPage == contentBookWidgets.size() - 1) {
                holder.pressSlot(holder.SCROLL_DOWN_SLOTS.b());
                goToFinalPage = false;
            } else {
                scrollPage(1);
            }
        } else if (scrollValue == -1.0) {
            if (currentPage == 0) {
                holder.pressSlot(holder.SCROLL_UP_SLOTS.b());
                goToFinalPage = true;
            } else {
                scrollPage(-1);
            }
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public WrappedScreenInfo getWrappedScreenInfo() {
        return wrappedScreenInfo;
    }

    public void reloadContentBookWidgets(boolean resetPage) {
        contentBookWidgets = new ArrayList<>();

        Map<Integer, Pair<ItemStack, ActivityInfo>> activities = holder.getActivities();

        int baseX = offsetX + 18;
        int baseY = offsetY + 35;

        List<ContentBookWidget> currentWidgets = new ArrayList<>();
        contentBookWidgets.add(currentWidgets);

        int widgetIndex = 0;

        for (Map.Entry<Integer, Pair<ItemStack, ActivityInfo>> entry : activities.entrySet()) {
            int indexInPage = widgetIndex % WIDGETS_PER_PAGE;
            int row = indexInPage / MAX_COLUMNS;
            int col = indexInPage % MAX_COLUMNS;
            int x = baseX + col * 133;
            int y = baseY + row * 18;

            boolean searchMatch = StringUtils.partialMatch(entry.getValue().b().name(), searchWidget.getTextBoxInput());

            currentWidgets.add(new ContentBookWidget(x, y, entry.getValue(), entry.getKey(), holder, searchMatch));

            widgetIndex++;

            if (indexInPage == WIDGETS_PER_PAGE - 1 && widgetIndex < activities.size()) {
                currentWidgets = new ArrayList<>();
                contentBookWidgets.add(currentWidgets);
            }
        }

        ActivityInfo newTrackedActivity = holder.getTrackedActivityInfo().orElse(null);

        if (newTrackedActivity != null) {
            trackedActivity = newTrackedActivity;

            if (trackedActivity.description().isPresent()) {
                trackedDescription = trackedActivity.description().get().map(part -> {
                    if (part.getPartStyle().getColor().equals(CustomColor.fromChatFormatting(ChatFormatting.GRAY))) {
                        return part.withStyle(partStyle -> partStyle.withColor(CommonColors.WHITE));
                    } else if (part.getPartStyle()
                            .getColor()
                            .equals(CustomColor.fromChatFormatting(ChatFormatting.WHITE))) {
                        return part.withStyle(partStyle -> partStyle.withColor(CommonColors.AQUA));
                    } else {
                        return part;
                    }
                });
            }

            if (!trackedActivity.rewards().isEmpty()) {
                createRewardWidgets(trackedActivity.rewards());
            }
        }

        if (!resetPage) {
            goToFinalPage = false;
            return;
        }

        currentPage = goToFinalPage ? contentBookWidgets.size() - 1 : 0;
        updateScrollButtonsVisibility();
    }

    public void setDialogueHistoryItem(ItemStack item, int slot) {
        contentBookActionWidgets.put(
                slot, new ContentBookActionWidget(offsetX + 399, offsetY + 8, item, (b) -> holder.pressSlot(slot, b)));
    }

    public void setScrollUpItem(ItemStack item) {
        scrollUpActive = !item.isEmpty();

        updateScrollButtonsVisibility();
    }

    public void setFilterItem(ItemStack item, int slot) {
        contentBookActionWidgets.put(
                slot, new ContentBookActionWidget(offsetX + 297, offsetY + 8, item, (b) -> holder.pressSlot(slot, b)));
    }

    public void setSortItem(ItemStack item, int slot) {
        contentBookActionWidgets.put(
                slot, new ContentBookActionWidget(offsetX + 315, offsetY + 8, item, (b) -> holder.pressSlot(slot, b)));
    }

    public void setPlayerProgressItem(ItemStack item, int slot) {
        contentBookActionWidgets.put(
                slot, new ContentBookActionWidget(offsetX + 381, offsetY + 8, item, (b) -> holder.pressSlot(slot, b)));
    }

    public void setScrollDownItem(ItemStack item) {
        scrollDownActive = !item.isEmpty();

        updateScrollButtonsVisibility();
    }

    public void removeTrackedActivity() {
        trackedActivity = null;
    }

    private void createRewardWidgets(Map<ActivityRewardType, List<StyledText>> rewards) {
        trackedRewards = new ArrayList<>();

        int x = offsetX + 410;

        List<Map.Entry<ActivityRewardType, List<StyledText>>> reversedEntries = new ArrayList<>(rewards.entrySet());
        Collections.reverse(reversedEntries);

        for (Map.Entry<ActivityRewardType, List<StyledText>> entry : reversedEntries) {
            List<MutableComponent> tooltip =
                    entry.getValue().stream().map(StyledText::getComponent).toList();

            StyledText renderedText = null;
            int widgetWidth = 10;

            ActivityRewardType type = entry.getKey();
            StyledText firstText =
                    entry.getValue().isEmpty() ? null : entry.getValue().getFirst();

            if ((type == ActivityRewardType.EMERALDS && !VARIOUS_EMERALDS.equals(firstText))
                    || type == ActivityRewardType.XP) {
                Matcher matcher = firstText.getMatcher(NUMBER_PATTERN);
                if (matcher.matches()) {
                    String numberText = StringUtils.integerToShortString(Long.parseLong(matcher.group(1)))
                            + (type == ActivityRewardType.XP ? " XP" : "²");
                    renderedText = StyledText.fromString(numberText);
                    int textWidth = FontRenderer.getInstance().getFont().width(numberText);

                    widgetWidth += 2 + textWidth;
                } else {
                    WynntilsMod.error("Failed to extract reward number from " + firstText);
                }
            }

            x -= widgetWidth;

            trackedRewards.add(new ContentBookRewardWidget(
                    x, offsetY + 213, widgetWidth, type.getTexture(), tooltip, renderedText));

            x -= 2;
        }

        trackedRewardsX = (int) (x
                - FontRenderer.getInstance().getFont().width(I18n.get("screens.wynntils.contentBook.rewards")) * 0.9f);
    }

    private void scrollPage(int direction) {
        currentPage += direction;

        McUtils.playSoundMaster(BUTTON_CLICK_SOUND);

        updateScrollButtonsVisibility();
    }

    private void updateScrollButtonsVisibility() {
        scrollUpButton.visible = scrollUpActive || currentPage != 0;
        scrollDownButton.visible = scrollDownActive || currentPage < contentBookWidgets.size() - 1;
    }

    private void renderBackgroundTexture(PoseStack poseStack) {
        RenderUtils.drawTexturedRect(poseStack, Texture.CUSTOM_CONTENT_BOOK_BACKGROUND, offsetX, offsetY);
    }

    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (GuiEventListener child : getWidgetsForIteration().toList()) {
            if (child instanceof TooltipProvider tooltipProvider && child.isMouseOver(mouseX, mouseY)) {
                guiGraphics.renderComponentTooltip(
                        FontRenderer.getInstance().getFont(), tooltipProvider.getTooltipLines(), mouseX, mouseY);
                break;
            }
        }
    }

    private Stream<GuiEventListener> getWidgetsForIteration() {
        return Stream.concat(
                children().stream(),
                Stream.concat(
                        contentBookWidgets.get(currentPage).stream(), contentBookActionWidgets.values().stream()));
    }
}
