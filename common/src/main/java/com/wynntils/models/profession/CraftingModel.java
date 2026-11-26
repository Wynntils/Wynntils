/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.containers.containers.CraftingStationContainer;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.items.items.game.PowderItem;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public class CraftingModel extends Model {
    private static final Pattern CRAFTING_LEVEL_PATTERN =
            Pattern.compile("§7 - §f(?:.+?) §7\\[Lv\\. (?<minLevel>\\d+)(\\.0)? to (?<maxLevel>\\d+)(\\.0)?\\]");

    private final RecipeInfoRegistry recipeInfoRegistry = new RecipeInfoRegistry();

    private MaterialItem materialOne;
    private MaterialItem materialTwo;
    private Map<Integer, IngredientItem> ingredientItems = new HashMap<>();
    private Map<Integer, PowderItem> powderItems = new HashMap<>();
    private int minLevel = -1;
    private int maxLevel = -1;

    private boolean validCraft = false;

    public CraftingModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        recipeInfoRegistry.registerDownloads(registry);
    }

    @SubscribeEvent
    public void onSetSlot(SetSlotEvent.Post event) {
        if (Objects.equals(event.getContainer(), McUtils.inventory())) return;
        if (!(Models.Container.getCurrentContainer() instanceof CraftingStationContainer container)) return;

        int slot = event.getSlot();
        ItemStack itemStack = event.getItemStack();
        if (slot == container.MATERIAL_SLOT_ONE) {
            Optional<MaterialItem> materialItemOpt = Models.Item.asWynnItem(itemStack, MaterialItem.class);
            if (materialItemOpt.isPresent()) {
                materialOne = materialItemOpt.get();
            } else {
                materialOne = null;
            }
        } else if (slot == container.MATERIAL_SLOT_TWO) {
            Optional<MaterialItem> materialItemOpt = Models.Item.asWynnItem(itemStack, MaterialItem.class);
            if (materialItemOpt.isPresent()) {
                materialTwo = materialItemOpt.get();
            } else {
                materialTwo = null;
            }
        } else if (container.INGREDIENT_SLOTS.contains(slot)) {
            int ingredientSlot = container.INGREDIENT_SLOTS.indexOf(slot);
            Optional<IngredientItem> ingredientItemOpt = Models.Item.asWynnItem(itemStack, IngredientItem.class);
            Optional<PowderItem> powderItemOpt = Models.Item.asWynnItem(itemStack, PowderItem.class);

            if (ingredientItemOpt.isPresent()) {
                ingredientItems.put(ingredientSlot, ingredientItemOpt.get());
            } else if (powderItemOpt.isPresent()) {
                powderItems.put(ingredientSlot, powderItemOpt.get());
            } else {
                ingredientItems.remove(ingredientSlot);
                powderItems.remove(ingredientSlot);
            }
        } else if (slot == container.CRAFT_SLOT) {
            Matcher matcher = LoreUtils.matchLoreLine(itemStack, 1, CRAFTING_LEVEL_PATTERN);
            if (matcher.matches()) {
                minLevel = Integer.parseInt(matcher.group("minLevel"));
                maxLevel = Integer.parseInt(matcher.group("maxLevel"));
            } else {
                minLevel = -1;
                maxLevel = -1;
            }
        }

        validCraft = materialOne != null && materialTwo != null && minLevel != -1 && maxLevel != -1;

        if (validCraft) {
            // TODO: Determine crafted item
        }
    }

    @SubscribeEvent
    public void onClose(ContainerCloseEvent.Post event) {
        materialOne = null;
        materialTwo = null;
        ingredientItems.clear();
        powderItems.clear();
        minLevel = -1;
        maxLevel = -1;

        validCraft = false;
    }

    public boolean hasValidCraft() {
        return validCraft;
    }
}
