/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.containers.containers.CraftingStationContainer;
import com.wynntils.models.elements.ElementModel;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.PowderTierInfo;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.ingredients.type.IngredientPosition;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.items.items.game.PowderItem;
import com.wynntils.models.profession.type.CraftedItemRecipe;
import com.wynntils.models.profession.type.CraftedItemType;
import com.wynntils.models.profession.type.RecipeInfo;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * A lot of the logic for determing the stats of items when crafted were done with the assistance of WynnBuilder
 * https://github.com/wynnbuilder/wynnbuilder.github.io
 */
public class CraftingModel extends Model {
    private static final Pattern CRAFTING_LEVEL_PATTERN =
            Pattern.compile("§7 - §f(?<type>.+?) §7\\[Lv\\. (?<minLevel>\\d+)(\\.0)? to (?<maxLevel>\\d+)(\\.0)?\\]");

    // Modifier values sourced from
    // https://github.com/wynnbuilder/wynnbuilder.github.io/blob/39b38ebc6886f64985a67266e6a307ecc9b7dcd3/js/load_ing.js#L112
    private static final List<PowderCraftingModifiers> POWDER_CRAFTING_MODIFIERS = List.of(
            new PowderCraftingModifiers(35, 0),
            new PowderCraftingModifiers(52.5, 0),
            new PowderCraftingModifiers(70, 10),
            new PowderCraftingModifiers(91, 20),
            new PowderCraftingModifiers(112, 28),
            new PowderCraftingModifiers(133, 36),
            new PowderCraftingModifiers(154, 44));

    private final RecipeInfoRegistry recipeInfoRegistry = new RecipeInfoRegistry();

    private CraftedItemType craftedItemType;
    private MaterialItem materialOne;
    private MaterialItem materialTwo;
    private Map<Integer, IngredientItem> ingredientItems = new HashMap<>();
    private Map<Integer, PowderItem> powderItems = new HashMap<>();
    private int minLevel = -1;
    private int maxLevel = -1;

    private CraftedItemRecipe currentCraftedItem;
    private boolean validCraft = false;
    private GearAttackSpeed gearAttackSpeed = GearAttackSpeed.NORMAL;

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
                craftedItemType = CraftedItemType.fromString(matcher.group("type"));
                minLevel = Integer.parseInt(matcher.group("minLevel"));
                maxLevel = Integer.parseInt(matcher.group("maxLevel"));
            } else {
                craftedItemType = null;
                minLevel = -1;
                maxLevel = -1;
            }
        }

        validCraft = craftedItemType != null
                && materialOne != null
                && materialTwo != null
                && minLevel != -1
                && maxLevel != -1;

        if (validCraft) {
            currentCraftedItem = buildCraftedItem(
                    craftedItemType,
                    materialOne,
                    materialTwo,
                    ingredientItems,
                    powderItems,
                    minLevel,
                    maxLevel,
                    gearAttackSpeed);
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

    public static int[][] createPositionModifierGrid(Map<IngredientPosition, Integer> modifiers) {
        int left = modifiers.getOrDefault(IngredientPosition.LEFT, 0);
        int right = modifiers.getOrDefault(IngredientPosition.RIGHT, 0);
        int above = modifiers.getOrDefault(IngredientPosition.ABOVE, 0);
        int under = modifiers.getOrDefault(IngredientPosition.UNDER, 0);
        int touching = modifiers.getOrDefault(IngredientPosition.TOUCHING, 0);
        int notTouching = modifiers.getOrDefault(IngredientPosition.NOT_TOUCHING, 0);

        return new int[][] {
            {notTouching, above + notTouching, notTouching},
            {notTouching, above + touching, notTouching},
            {left + touching, 0, right + touching},
            {notTouching, under + touching, notTouching},
            {notTouching, under + notTouching, notTouching}
        };
    }

    public boolean hasValidCraft() {
        return validCraft;
    }

    public CraftedItemRecipe getCurrentCraftedItem() {
        return currentCraftedItem;
    }

    public void setAttackSpeed(GearAttackSpeed attackSpeed) {
        this.gearAttackSpeed = switch (attackSpeed) {
            case SLOW, NORMAL, FAST -> attackSpeed;
            default -> {
                WynntilsMod.warn("Unsupported crafted weapon attack speed: " + attackSpeed + ", using Normal");
                yield GearAttackSpeed.NORMAL;
            }
        };
    }

    public CraftedItemRecipe buildCraftedItem(
            CraftedItemType craftedItemType,
            MaterialItem materialOne,
            MaterialItem materialTwo,
            Map<Integer, IngredientItem> ingredientItems,
            Map<Integer, PowderItem> powderItems,
            int minLevel,
            int maxLevel,
            GearAttackSpeed attackSpeed) {
        String recipeName = StringUtils.capitalizeFirst(craftedItemType.name().toLowerCase(Locale.ROOT)) + minLevel
                + "-" + maxLevel;
        RecipeInfo recipeInfo = recipeInfoRegistry.getFromRecipeName(recipeName);

        if (recipeInfo == null) {
            WynntilsMod.warn("Recipe not found: " + recipeName);
            return null;
        }

        double statMultiplier = getStatMultiplier(recipeInfo, materialOne, materialTwo);
        List<PowderTierInfo> powders = craftedItemType.isGear()
                ? powderItems.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(entry -> entry.getValue().getPowderProfile())
                        .toList()
                : List.of();
        List<Pair<DamageType, List<RangedValue>>> damages = craftedItemType.isWeapon()
                ? getWeaponDamages(recipeInfo.healthOrDamage(), statMultiplier, attackSpeed, powders)
                : List.of();
        Pair<Double, Double> averageDps =
                craftedItemType.isWeapon() ? calculateAverageDps(damages, attackSpeed) : Pair.of(0d, 0d);
        RangedValue health = craftedItemType.isWeapon()
                ? RangedValue.NONE
                : getMultipliedStat(recipeInfo.healthOrDamage(), statMultiplier);
        double powderDurabilityCost = 0;
        int powderHealth = 0;
        Map<Element, Integer> defences = new EnumMap<>(Element.class);
        Map<Skill, Integer> skillRequirements = new EnumMap<>(Skill.class);
        for (PowderTierInfo powder : powders) {
            PowderCraftingModifiers stats = POWDER_CRAFTING_MODIFIERS.get(powder.tier() - 1);
            powderDurabilityCost += stats.durabilityCost();
            Element element = powder.element().getElement();

            if (stats.skillRequirement() > 0) {
                skillRequirements.merge(Skill.fromElement(element), stats.skillRequirement(), Integer::sum);
            }

            if (!craftedItemType.isWeapon()) {
                powderHealth += powder.health() / 2;
                defences.merge(element, powder.addedDefence() / 2, Integer::sum);
                defences.merge(ElementModel.getOpposingElement(element), -(powder.removedDefence() / 2), Integer::sum);
            }
        }

        health = RangedValue.of(health.low() + powderHealth, health.high() + powderHealth);
        RangedValue baseDurabilityOrDuration = recipeInfo.durabilityOrDuration();
        RangedValue durabilityOrDuration = craftedItemType.isGear()
                ? RangedValue.of(
                        getPowderedDurability(baseDurabilityOrDuration.low(), statMultiplier, powderDurabilityCost),
                        getPowderedDurability(baseDurabilityOrDuration.high(), statMultiplier, powderDurabilityCost))
                : getMultipliedStat(baseDurabilityOrDuration, statMultiplier);

        return new CraftedItemRecipe(
                craftedItemType,
                averageDps,
                attackSpeed,
                damages,
                health,
                recipeInfo.level(),
                durabilityOrDuration,
                defences.entrySet().stream()
                        .filter(entry -> entry.getValue() != 0)
                        .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                        .toList(),
                skillRequirements.entrySet().stream()
                        .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                        .toList());
    }

    private static RangedValue getMultipliedStat(RangedValue stat, double multiplier) {
        return RangedValue.of((int) (stat.low() * multiplier), (int) (stat.high() * multiplier));
    }

    private static int getPowderedDurability(int baseDurability, double multiplier, double powderCost) {
        return Math.max(0, (int) Math.floor(Math.round(baseDurability * multiplier) - powderCost));
    }

    private Pair<Double, Double> calculateAverageDps(
            List<Pair<DamageType, List<RangedValue>>> damages, GearAttackSpeed attackSpeed) {
        double lowRollDamage = 0;
        double highRollDamage = 0;

        for (Pair<DamageType, List<RangedValue>> damage : damages) {
            RangedValue lowRoll = damage.b().getFirst();
            RangedValue highRoll = damage.b().getLast();

            lowRollDamage += lowRoll.low() + lowRoll.high();
            highRollDamage += highRoll.low() + highRoll.high();
        }

        return Pair.of(
                lowRollDamage * attackSpeed.getHitsPerSecond() / 2,
                highRollDamage * attackSpeed.getHitsPerSecond() / 2);
    }

    private static List<Pair<DamageType, List<RangedValue>>> getWeaponDamages(
            RangedValue baseDamage, double statMultiplier, GearAttackSpeed attackSpeed, List<PowderTierInfo> powders) {
        double speedRatio = GearAttackSpeed.NORMAL.getHitsPerSecond() / attackSpeed.getHitsPerSecond();

        RangedValue lowRoll = getNeutralDamage(baseDamage.low(), statMultiplier, speedRatio);
        RangedValue highRoll = getNeutralDamage(baseDamage.high(), statMultiplier, speedRatio);

        Map<DamageType, PowderDamage> powderDamage = new LinkedHashMap<>();
        for (PowderTierInfo powder : powders) {
            DamageType damageType = DamageType.fromElement(powder.element().getElement());
            PowderDamage bonus =
                    new PowderDamage(powder.convertedFromNeutral() / 200.0, powder.min() / 2, powder.max() / 2);
            powderDamage.merge(damageType, bonus, PowderDamage::add);
        }

        Map<DamageType, RangedValue> lowRollDamages = applyPowderDamage(lowRoll, powderDamage);
        Map<DamageType, RangedValue> highRollDamages = applyPowderDamage(highRoll, powderDamage);
        List<Pair<DamageType, List<RangedValue>>> damages = new ArrayList<>();

        for (Map.Entry<DamageType, RangedValue> entry : lowRollDamages.entrySet()) {
            damages.add(Pair.of(entry.getKey(), List.of(entry.getValue(), highRollDamages.get(entry.getKey()))));
        }

        return damages;
    }

    private static Map<DamageType, RangedValue> applyPowderDamage(
            RangedValue neutral, Map<DamageType, PowderDamage> powderDamage) {
        Map<DamageType, RangedValue> damages = new EnumMap<>(DamageType.class);
        double neutralMin = neutral.low();
        double neutralMax = neutral.high();

        for (Map.Entry<DamageType, PowderDamage> entry : powderDamage.entrySet()) {
            PowderDamage powder = entry.getValue();
            double conversion = Math.min(1, powder.conversion());
            double convertedMin = neutralMin * conversion;
            double convertedMax = neutralMax * conversion;

            neutralMin -= convertedMin;
            neutralMax -= convertedMax;

            damages.put(entry.getKey(), RangedValue.of((int) Math.floor(convertedMin + powder.min()), (int)
                    Math.floor(convertedMax + powder.max())));
        }

        damages.put(DamageType.NEUTRAL, RangedValue.of((int) Math.floor(neutralMin), (int) Math.floor(neutralMax)));

        return damages;
    }

    private static RangedValue getNeutralDamage(int baseDamage, double statMultiplier, double speedRatio) {
        double scaledDamage = Math.floor(baseDamage * statMultiplier);
        int speedAdjustedDamage = (int) Math.floor(scaledDamage * speedRatio);

        return RangedValue.of((int) Math.floor(speedAdjustedDamage * 0.9), (int) Math.floor(speedAdjustedDamage * 1.1));
    }

    private static double getStatMultiplier(RecipeInfo recipeInfo, MaterialItem materialOne, MaterialItem materialTwo) {
        String nameOne = materialOne.getMaterialInfo().apiName();
        String nameTwo = materialTwo.getMaterialInfo().apiName();

        Integer amountOne = recipeInfo.recipe().get(nameOne);
        Integer amountTwo = recipeInfo.recipe().get(nameTwo);

        double multiplierOne = getMaterialTierMultiplier(materialOne.getQualityTier());
        double multiplierTwo = getMaterialTierMultiplier(materialTwo.getQualityTier());

        return (amountOne * multiplierOne + amountTwo * multiplierTwo) / (amountOne + amountTwo);
    }

    private static double getMaterialTierMultiplier(int tier) {
        return switch (tier) {
            case 1 -> 1.0;
            case 2 -> 1.25;
            case 3 -> 1.4;
            default -> {
                WynntilsMod.warn("Invalid material tier: " + tier);
                yield 1.0;
            }
        };
    }

    private record PowderCraftingModifiers(double durabilityCost, int skillRequirement) {}

    private record PowderDamage(double conversion, int min, int max) {
        private PowderDamage add(PowderDamage other) {
            return new PowderDamage(conversion + other.conversion, min + other.min, max + other.max);
        }
    }
}
