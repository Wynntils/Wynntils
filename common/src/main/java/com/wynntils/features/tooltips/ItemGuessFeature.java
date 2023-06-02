/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.tooltips;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.screens.base.WynntilsMenuScreenBase;
import com.wynntils.screens.guides.gear.WynntilsItemGuideScreen;
import com.wynntils.utils.mc.LoreUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.TOOLTIPS)
public class ItemGuessFeature extends Feature {

    @RegisterConfig
    public final Config<Boolean> showGuessesPrice = new Config<>(true);

    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        Optional<GearBoxItem> gearBoxItemOpt = Models.Item.asWynnItem(event.getItemStack(), GearBoxItem.class);
        if (gearBoxItemOpt.isEmpty()) return;

        List<Component> tooltips = LoreUtils.appendTooltip(
                event.getItemStack(), event.getTooltips(), getTooltipAddon(gearBoxItemOpt.get()));
        event.setTooltips(tooltips);
    }

    public void displayHoveredItemInGuide(Slot hoveredSlot) {
        if (hoveredSlot == null) return;
        Optional<GearBoxItem> gearBoxItemOpt = Models.Item.asWynnItem(hoveredSlot.getItem(), GearBoxItem.class);
        if (gearBoxItemOpt.isEmpty()) return;

        WynntilsItemGuideScreen itemGuide = new WynntilsItemGuideScreen();
        itemGuide.setTextBoxInput(getPossibleGearNames(gearBoxItemOpt.get()));
        WynntilsMenuScreenBase.openBook(itemGuide);
    }

    private String getPossibleGearNames(GearBoxItem gearBoxItem) {
        List<GearInfo> possibleGear = Models.Gear.getPossibleGears(gearBoxItem);
        return possibleGear.stream().map(GearInfo::name).collect(Collectors.joining(", "));
    }

    private List<Component> getTooltipAddon(GearBoxItem gearBoxItem) {
        List<Component> addon = new ArrayList<>();
        List<GearInfo> possibleGear = Models.Gear.getPossibleGears(gearBoxItem);
        GearTier gearTier = gearBoxItem.getGearTier();

        if (possibleGear.isEmpty()) return addon; // nothing to put in tooltip

        addon.add(Component.literal("- ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.translatable("feature.wynntils.itemGuess.possibilities")
                        .withStyle(ChatFormatting.GRAY)));

        Map<Integer, List<MutableComponent>> levelToItems = new TreeMap<>();

        for (GearInfo gearInfo : possibleGear) {
            int level = (gearInfo != null) ? gearInfo.requirements().level() : -1;

            MutableComponent itemDesc = Component.literal(gearInfo.name()).withStyle(gearTier.getChatFormatting());

            if (Models.Favorites.isFavorite(gearInfo.name())) {
                itemDesc.withStyle(ChatFormatting.UNDERLINE);
            }

            levelToItems.computeIfAbsent(level, i -> new ArrayList<>()).add(itemDesc);
        }

        for (Map.Entry<Integer, List<MutableComponent>> entry : levelToItems.entrySet()) {
            int level = entry.getKey();
            List<MutableComponent> itemsForLevel = entry.getValue();

            MutableComponent guesses = Component.literal("    ");

            guesses.append(Component.literal("- ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.translatable("feature.wynntils.itemGuess.levelLine", level == -1 ? "?" : level)
                            .withStyle(ChatFormatting.GRAY)));

            if (showGuessesPrice.get() && level != -1) {
                guesses.append(Component.literal(" [")
                        .append(Component.literal((gearTier.getGearIdentificationCost(level) + " "
                                        + EmeraldUnits.EMERALD.getSymbol()))
                                .withStyle(ChatFormatting.GREEN))
                        .append(Component.literal("]"))
                        .withStyle(ChatFormatting.GRAY));
            }

            guesses.append(StyledText.fromString("§7: ").getComponent());

            Optional<MutableComponent> itemsComponent = itemsForLevel.stream()
                    .reduce((i, j) -> i.append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                            .append(j));

            if (itemsComponent.isPresent()) {
                guesses.append(itemsComponent.get());

                addon.add(guesses);
            }
        }

        return addon;
    }
}
