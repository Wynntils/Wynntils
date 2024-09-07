/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.tooltips;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.event.TooltipRenderEvent;
import com.wynntils.models.containers.containers.TradeMarketContainer;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.TooltipUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.InventoryUtils;
import com.wynntils.utils.wynn.ItemUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.TOOLTIPS)
public class ItemCompareFeature extends Feature {
    @Persisted
    public final Config<Integer> maxCompareSelectedCount = new Config<>(4);

    @Persisted
    public final Config<Boolean> removeFlavourText = new Config<>(true);

    @Persisted
    public final Config<Boolean> removeSetInfoText = new Config<>(true);

    @Persisted
    public final Config<Boolean> displayTag = new Config<>(true);

    @Persisted
    public final Config<Boolean> centerItemName = new Config<>(false);

    @RegisterKeyBind
    private final KeyBind holdToCompareKeyBind =
            new KeyBind("Hold to compare", GLFW.GLFW_KEY_KP_ENTER, false, null, null);

    @RegisterKeyBind
    private final KeyBind selectCompareKeyBind =
            new KeyBind("Select for comparing", GLFW.GLFW_KEY_KP_ADD, true, null, this::onSelectKeyPress);

    private final Map<WynnItem, ItemStack> selectedItems = new HashMap<>();
    private static final int COMPARE_ITEM_PAD = 6;
    private static final String EQUIPPED_KEY = "feature.wynntils.itemCompare.tag.equipped";
    private static final String HOVERED_KEY = "feature.wynntils.itemCompare.tag.hovered";
    private static final String SELECTED_KEY = "feature.wynntils.itemCompare.tag.selected";
    private static final String HOVERED_SELECTED_KEY = "feature.wynntils.itemCompare.tag.hovered_selected";

    // First equippedCount items in itemsToCompare will have "Equipped" tag, others will have "Selected" tag
    private int equippedCount = 0;
    private boolean changePositioner = false;

    @SubscribeEvent
    public void onWorldStateChangeEvent(WorldStateEvent event) {
        selectedItems.clear();
    }

    @SubscribeEvent
    public void onContainerCloseEvent(ContainerCloseEvent.Post event) {
        selectedItems.clear();
    }

    @SubscribeEvent
    public void onInventoryClickEvent(ContainerClickEvent event) {
        Models.Item.getWynnItem(event.getItemStack()).ifPresent(selectedItems::remove);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTooltipRendering(TooltipRenderEvent event) {
        if (!changePositioner) return;

        event.setPositioner(PassiveTooltipPositioner.INSTANCE);
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Post event) {
        Slot slot = event.getSlot();
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(slot.getItem());
        if (wynnItemOpt.isEmpty()) return;
        if (selectedItems.containsKey(wynnItemOpt.get())) {
            RenderUtils.drawArc(event.getPoseStack(), CommonColors.LIGHT_BLUE, slot.x, slot.y, 200, 1, 6, 8);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onItemTooltipRenderEvent(ItemTooltipRenderEvent.Pre event) {
        if (!KeyboardUtils.isKeyDown(holdToCompareKeyBind.getKeyMapping().key.getValue())) return;
        if (McUtils.mc().screen == null
                || !(McUtils.mc().screen instanceof AbstractContainerScreen<?> abstractContainerScreen)) return;

        if (abstractContainerScreen.hoveredSlot == null) return;

        ItemStack hoveredItemStack = abstractContainerScreen.hoveredSlot.getItem();

        if (event.getItemStack() != hoveredItemStack) return;

        Optional<WynnItem> hoveredWynnItemOpt = Models.Item.getWynnItem(hoveredItemStack);
        if (hoveredWynnItemOpt.isEmpty()) return;
        WynnItem hoveredWynnItem = hoveredWynnItemOpt.get();

        if (!(hoveredWynnItem instanceof GearTypeItemProperty hoveredGearItemProperty)) return;

        List<Pair<WynnItem, ItemStack>> itemsToCompare = new ArrayList<>();

        // If hovered over selected item, only compare with other selected items
        if (!selectedItems.containsKey(hoveredWynnItem)) {
            switch (hoveredGearItemProperty.getGearType()) {
                case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> {
                    List<ItemStack> armors = McUtils.inventory().armor;

                    Optional<ItemStack> matchingArmorOpt = armors.stream()
                            .filter(itemStack -> isMatchingType(itemStack, hoveredGearItemProperty))
                            .findFirst();
                    if (matchingArmorOpt.isPresent()) {
                        ItemStack armor = matchingArmorOpt.get();
                        Models.Item.getWynnItem(armor).ifPresent(wynnItem -> {
                            if (armor != hoveredItemStack) {
                                itemsToCompare.add(new Pair<>(wynnItem, armor));
                                ++equippedCount;
                            }
                        });
                    }
                }
                case RING, BRACELET, NECKLACE -> {
                    List<ItemStack> accessories = InventoryUtils.getAccessories(McUtils.player());

                    List<ItemStack> matchingAccessories = accessories.stream()
                            .filter(itemStack -> isMatchingType(itemStack, hoveredGearItemProperty))
                            .filter(itemStack -> itemStack != hoveredItemStack)
                            .filter(itemStack -> !ItemUtils.isEmptyAccessorySlot(itemStack))
                            .toList();
                    matchingAccessories.forEach(itemStack -> {
                        itemsToCompare.add(
                                new Pair<>(Models.Item.getWynnItem(itemStack).get(), itemStack));
                        ++equippedCount;
                    });
                }
                case SPEAR, WAND, DAGGER, BOW, RELIK -> {
                    ItemStack inHand = McUtils.player().getMainHandItem();
                    if (inHand != ItemStack.EMPTY && inHand != hoveredItemStack) {
                        Models.Item.getWynnItem(inHand).ifPresent(wynnItem -> {
                            if (isMatchingType(wynnItem, hoveredGearItemProperty)) {
                                itemsToCompare.add(new Pair<>(wynnItem, inHand));
                                ++equippedCount;
                            }
                        });
                    }
                }
                // TODO: allow comparing with equipped/selected tomes and selected charms
            }
        }

        List<Pair<WynnItem, ItemStack>> selectedMatchingHovered = selectedItems.entrySet().stream()
                .filter(entry -> isMatchingType(entry.getKey(), hoveredGearItemProperty))
                .filter(entry -> entry.getKey() != hoveredWynnItem)
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .filter(pair -> !itemsToCompare.contains(pair))
                .collect(Collectors.toCollection(ArrayList::new));

        while (selectedMatchingHovered.size() > maxCompareSelectedCount.get()) {
            selectedMatchingHovered.removeLast();
        }

        itemsToCompare.addAll(selectedMatchingHovered);

        if (itemsToCompare.isEmpty()) return;

        Window window = McUtils.mc().getWindow();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = FontRenderer.getInstance().getFont();
        final PoseStack poseStack = guiGraphics.pose();
        float universalScale = Managers.Feature.getFeatureInstance(TooltipFittingFeature.class)
                .universalScale
                .get();
        int twoPad = COMPARE_ITEM_PAD * 2;

        poseStack.pushPose();
        poseStack.translate(0, 0, 300);

        List<Component> hoveredLines = new ArrayList<>(event.getTooltips());
        if (removeFlavourText.get()) removeFlavourText(hoveredLines);
        if (removeSetInfoText.get()) removeSetInfoText(hoveredLines);
        List<ClientTooltipComponent> hoveredClientComponents = TooltipUtils.getClientTooltipComponent(hoveredLines);
        int hoveredTooltipWidth = TooltipUtils.getTooltipWidth(hoveredClientComponents, font);
        int hoveredTooltipHeight = TooltipUtils.getTooltipHeight(hoveredClientComponents);

        if (centerItemName.get()) centerItemName(hoveredLines, hoveredTooltipWidth);

        if (displayTag.get()) {
            if (selectedItems.containsValue(event.getItemStack())) {
                hoveredLines.addFirst(getPaddedComponent(getTag(HOVERED_SELECTED_KEY), hoveredTooltipWidth));
            } else {
                hoveredLines.addFirst(getPaddedComponent(getTag(HOVERED_KEY), hoveredTooltipWidth));
            }
            hoveredTooltipHeight += font.lineHeight;
        }

        float hoveredScaleFactor = universalScale;
        if (hoveredTooltipHeight + twoPad > window.getGuiScaledHeight()) {
            float scaleFactor = ((float) (window.getGuiScaledHeight() - twoPad) / hoveredTooltipHeight);
            hoveredScaleFactor *= scaleFactor;
            hoveredTooltipWidth *= scaleFactor;
            hoveredTooltipHeight *= scaleFactor;
        }
        if (hoveredTooltipWidth + twoPad > (window.getGuiScaledWidth() / 3.0f)) {
            float scaleFactor = (((window.getGuiScaledWidth() / 3.0f) - twoPad) / hoveredTooltipWidth);
            hoveredScaleFactor *= scaleFactor;
            hoveredTooltipWidth *= scaleFactor;
            hoveredTooltipHeight *= scaleFactor;
        }

        int hoveredX = window.getGuiScaledWidth() - hoveredTooltipWidth - COMPARE_ITEM_PAD;
        int hoveredY = (int) ((window.getGuiScaledHeight() / 2.0f) - (hoveredTooltipHeight / 2.0f));

        List<Tooltip> tooltips = new ArrayList<>();
        int prevX = hoveredX;

        for (Pair<WynnItem, ItemStack> pair : itemsToCompare) {
            List<Component> lines = getWynnOrVanillaLines(abstractContainerScreen, pair.key(), pair.value());
            if (removeFlavourText.get()) removeFlavourText(lines);
            if (removeSetInfoText.get()) removeSetInfoText(lines);
            List<ClientTooltipComponent> clientTooltipComponents = TooltipUtils.getClientTooltipComponent(lines);

            int tooltipWidth = TooltipUtils.getTooltipWidth(clientTooltipComponents, font);
            int tooltipHeight = TooltipUtils.getTooltipHeight(clientTooltipComponents);

            if (centerItemName.get()) centerItemName(lines, tooltipWidth);

            if (displayTag.get()) {
                if (equippedCount > 0) {
                    lines.addFirst(getPaddedComponent(getTag(EQUIPPED_KEY), tooltipWidth));
                    --equippedCount;
                } else {
                    lines.addFirst(getPaddedComponent(getTag(SELECTED_KEY), tooltipWidth));
                }
                tooltipHeight += font.lineHeight;
            }

            float tooltipScaleFactor = universalScale;
            if (tooltipHeight + twoPad > window.getGuiScaledHeight()) {
                float scaleFactor = ((float) (window.getGuiScaledHeight() - twoPad) / tooltipHeight);
                tooltipScaleFactor *= scaleFactor;
                tooltipWidth *= scaleFactor;
                tooltipHeight *= scaleFactor;
            }
            int maxWidth = Math.round(window.getGuiScaledWidth() / 3.0f);
            if (tooltipWidth + twoPad > maxWidth) {
                float scaleFactor = ((float) (maxWidth - twoPad) / tooltipWidth);
                tooltipScaleFactor *= scaleFactor;
                tooltipWidth *= scaleFactor;
                tooltipHeight *= scaleFactor;
            }

            int x = prevX - twoPad - tooltipWidth;
            int y = (int) ((window.getGuiScaledHeight() / 2.0f) - (tooltipHeight / 2.0f));

            tooltips.add(new Tooltip(
                    lines,
                    x,
                    y,
                    tooltipWidth,
                    tooltipHeight,
                    tooltipScaleFactor,
                    pair.value().getTooltipImage()));
            prevX = x;
        }

        // Fix tooltips overflow by rendering tooltips in two rows and slightly scaling them down, if possible
        if (!tooltips.isEmpty() && tooltips.getLast().getX() < 0) {
            fixTooltipOverflow(tooltips, window.getGuiScaledHeight());
        }

        // Centering all tooltips horizontally
        int d1 = tooltips.getLast().getX();
        int d2 = window.getGuiScaledWidth() - (hoveredX + hoveredTooltipWidth);
        int offsetX = Math.floorDiv((d1 + d2), 2) - Math.max(d1, d2);
        tooltips.forEach(tooltip -> tooltip.offsetX(offsetX));
        hoveredX += offsetX;

        event.setCanceled(true);

        changePositioner = true;
        poseStack.pushPose();
        poseStack.scale(hoveredScaleFactor, hoveredScaleFactor, 1);
        guiGraphics.renderTooltip(
                font,
                hoveredLines,
                hoveredItemStack.getTooltipImage(),
                (int) (hoveredX / hoveredScaleFactor),
                (int) (hoveredY / hoveredScaleFactor));
        poseStack.popPose();

        for (Tooltip tooltip : tooltips) {
            poseStack.pushPose();
            float scaleFactor = tooltip.getScaleFactor();
            poseStack.scale(scaleFactor, scaleFactor, 1);
            guiGraphics.renderTooltip(
                    font,
                    tooltip.getLines(),
                    tooltip.getVisualTooltipComponent(),
                    (int) (tooltip.getX() / scaleFactor),
                    (int) (tooltip.getY() / scaleFactor));
            poseStack.popPose();
        }
        changePositioner = false;

        poseStack.popPose();
    }

    private boolean isMatchingType(WynnItem wynnItem, GearTypeItemProperty gearItemReference) {
        if (wynnItem instanceof GearItem gearItem) {
            return gearItem.getGearType() == gearItemReference.getGearType();
        }
        return false;
    }

    private boolean isMatchingType(ItemStack itemStack, GearTypeItemProperty gearItemReference) {
        Optional<GearTypeItemProperty> gearOpt = Models.Item.asWynnItemProperty(itemStack, GearTypeItemProperty.class);
        return gearOpt.isEmpty() ? false : gearOpt.get().getGearType() == gearItemReference.getGearType();
    }

    private void onSelectKeyPress(Slot hoveredSlot) {
        if (hoveredSlot == null) return;
        // Selecting items behaves weirdly on tm updates, so no selecting in tm
        if (Models.Container.getCurrentContainer() instanceof TradeMarketContainer) return;
        int slot = hoveredSlot.getContainerSlot();

        // Selecting armor and accessory slots is not allowed
        if (slot == 9 || slot == 10 || slot == 11 || slot == 12 || slot == 36 || slot == 37 || slot == 38 || slot == 39) return;

        ItemStack itemStack = hoveredSlot.getItem();
        if (itemStack.isEmpty()) return;

        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return;
        WynnItem wynnItem = wynnItemOpt.get();
        if (!(wynnItem instanceof GearItem)) return;

        if (selectedItems.containsKey(wynnItem)) {
            selectedItems.remove(wynnItem);
        } else {
            selectedItems.put(wynnItem, itemStack);
        }
    }

    private String getTag(String key) {
        return I18n.get(key);
    }

    private List<Component> getWynnOrVanillaLines(
            AbstractContainerScreen<?> screen, WynnItem wynnItem, ItemStack itemStack) {
        List<Component> wynnTooltip = TooltipUtils.getWynnItemTooltip(itemStack, wynnItem);
        return wynnTooltip.isEmpty() ? screen.getTooltipFromItem(McUtils.mc(), itemStack) : wynnTooltip;
    }

    private MutableComponent getPaddedComponent(String string, int tooltipWidth) {
        Font font = FontRenderer.getInstance().getFont();
        // This is not the exact center, but good enough
        int count = Math.round((tooltipWidth - font.width(string)) / 2.0f / font.width(" "));
        return Component.literal(" ".repeat(count) + string);
    }

    private void removeFlavourText(List<Component> lines) {
        while (lines.getLast().getString().startsWith("§8")) {
            lines.removeLast();
        }
    }

    private void removeSetInfoText(List<Component> lines) {
        for (int i = lines.size() - 1; i >= 0; --i) {
            StyledText line = StyledText.fromComponent(lines.get(i)).getNormalized();
            if (line.getMatcher(WynnItemParser.SET_ITEM_PATTERN).matches()) {
                lines.remove(i);
            } else if (line.getMatcher(WynnItemParser.SET_PATTERN).matches()) {
                lines.remove(i);
                lines.remove(i - 1);
                return;
            }
        }
    }

    private void centerItemName(List<Component> lines, int tooltipWidth) {
        Component title = lines.removeFirst();

        String string = getPaddedComponent(title.getString(), tooltipWidth).getString();
        int count = string.indexOf(string.trim());

        MutableComponent newTitle = Component.literal(" ".repeat(count));
        title.getSiblings().forEach(newTitle::append);

        lines.addFirst(newTitle);
    }

    private void fixTooltipOverflow(List<Tooltip> tooltips, int screenHeight) {
        List<Tooltip> overflowTooltips = new ArrayList<>();
        while (tooltips.getLast().getX() < 0) {
            overflowTooltips.add(tooltips.removeLast());
        }
        overflowTooltips.sort(Comparator.comparingInt(Tooltip::getHeight));

        List<Tooltip> heightSortedTooltips = new ArrayList<>(tooltips);
        heightSortedTooltips.sort(Comparator.comparingInt(Tooltip::getHeight));
        Collections.reverse(heightSortedTooltips);

        if (heightSortedTooltips.size() > overflowTooltips.size()) {
            heightSortedTooltips
                    .subList(0, (heightSortedTooltips.size() - overflowTooltips.size()))
                    .clear();
        } else if (heightSortedTooltips.size() < overflowTooltips.size()) {
            overflowTooltips
                    .subList(heightSortedTooltips.size(), overflowTooltips.size() - 1)
                    .clear();
        }

        for (int i = 0; i < heightSortedTooltips.size(); ++i) {
            Tooltip overflow = overflowTooltips.get(i);
            Tooltip tooltip = heightSortedTooltips.get(i);
            int index = tooltips.indexOf(tooltip);

            float tooltipScaleFactor = 1.0f;
            float overflowScaleFactor = 1.0f;
            int tooltipHeight = tooltip.getHeight();
            int overflowHeight = overflow.getHeight();
            int tooltipWidth = tooltip.getWidth();
            int overflowWidth = overflow.getWidth();

            if (tooltipHeight + overflowHeight + (COMPARE_ITEM_PAD * 4.0f) > screenHeight) {
                float scaleFactor = (screenHeight - (COMPARE_ITEM_PAD * 4.0f)) / (tooltipHeight + overflowHeight);
                if (scaleFactor < 0.5f) continue;
                tooltipScaleFactor *= scaleFactor;
                overflowScaleFactor *= scaleFactor;

                int offsetX = (int) (tooltipWidth * (1 - scaleFactor));
                for (int j = index; j < tooltips.size(); ++j) {
                    Tooltip t = tooltips.get(j);
                    t.offsetX(offsetX);
                }
                tooltipHeight *= scaleFactor;
                overflowHeight *= scaleFactor;
                tooltipWidth *= scaleFactor;
                overflowWidth *= scaleFactor;
            }
            if (overflowWidth > tooltipWidth) {
                float scaleFactor = (float) tooltipWidth / overflowWidth;
                overflowScaleFactor *= scaleFactor;
                overflowHeight *= scaleFactor;
            }

            int tooltipY = COMPARE_ITEM_PAD;
            int overflowY = tooltipY + tooltipHeight + (COMPARE_ITEM_PAD * 2);
            int d1 = tooltipY;
            int d2 = screenHeight - overflowY - overflowHeight;
            int offsetY = Math.floorDiv((d1 + d2), 2) - Math.min(d1, d2);
            tooltipY += offsetY;
            overflowY += offsetY;

            tooltip.setY(tooltipY);
            tooltip.multScaleFactor(tooltipScaleFactor);

            overflow.setX(tooltip.getX());
            overflow.setY(overflowY);
            overflow.multScaleFactor(overflowScaleFactor);

            tooltips.add(index, overflow);
        }
    }

    private static final class PassiveTooltipPositioner implements ClientTooltipPositioner {
        private static final ClientTooltipPositioner INSTANCE = new PassiveTooltipPositioner();

        @Override
        public Vector2ic positionTooltip(
                int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {
            return new Vector2i(mouseX, mouseY);
        }
    }

    private static final class Tooltip {
        private final List<Component> lines;
        private int x;
        private int y;
        private final int width;
        private final int height;
        private float scaleFactor;
        private final Optional<TooltipComponent> visualTooltipComponent;

        private Tooltip(
                List<Component> lines,
                int x,
                int y,
                int width,
                int height,
                float scaleFactor,
                Optional<TooltipComponent> visualTooltipComponent) {
            this.lines = lines;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.scaleFactor = scaleFactor;
            this.visualTooltipComponent = visualTooltipComponent;
        }

        public List<Component> getLines() {
            return Collections.unmodifiableList(lines);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public float getScaleFactor() {
            return scaleFactor;
        }

        public Optional<TooltipComponent> getVisualTooltipComponent() {
            return visualTooltipComponent;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public void offsetX(int offset) {
            this.x += offset;
        }

        public void multScaleFactor(float k) {
            this.scaleFactor *= k;
        }
    }
}
