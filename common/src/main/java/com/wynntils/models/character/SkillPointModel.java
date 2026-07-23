/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.handlers.container.scriptedquery.QueryBuilder;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.handlers.container.type.ContainerContentChangeType;
import com.wynntils.models.character.type.ClickAction;
import com.wynntils.models.character.type.SavableSkillPointSet;
import com.wynntils.models.containers.containers.CharacterInfoContainer;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.items.items.gui.SkillPointItem;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public final class SkillPointModel extends Model {
    private static final int[] SKILL_POINT_TOTAL_SLOTS = {11, 12, 13, 14, 15};
    private static final int SKILL_POINT_TOME_SLOT = 4;
    private static final int CONTENT_BOOK_SLOT = 62;

    private Map<Skill, Integer> totalSkillPoints = new EnumMap<>(Skill.class);
    private Map<Skill, Integer> gearSkillPoints = new EnumMap<>(Skill.class);
    private Map<Skill, Integer> craftedSkillPoints = new EnumMap<>(Skill.class);
    private Map<Skill, Integer> tomeSkillPoints = new EnumMap<>(Skill.class);
    private Map<Skill, Integer> statusEffectSkillPoints = new EnumMap<>(Skill.class);
    private Map<Skill, Integer> setBonusSkillPoints = new EnumMap<>(Skill.class);
    private Map<Skill, Integer> assignedSkillPoints = new EnumMap<>(Skill.class);

    public SkillPointModel() {
        super(List.of());
    }

    public void saveSkillPoints(String name, int[] skillPoints) {
        SavableSkillPointSet assignedSkillPointSet = new SavableSkillPointSet(skillPoints);
        Services.loadout.saveSkillPointLoadoutAndTomes(
                name, assignedSkillPointSet, Models.Character.getCurrentTomeSet());
        WynntilsMod.info("Saved skill point loadout: " + name + " " + assignedSkillPointSet);
    }

    /**
     * Saves only the current assigned skill points to the loadout list.
     */
    public void saveCurrentSkillPoints(String name) {
        saveSkillPoints(name, new int[] {
            getAssignedSkillPoints(Skill.STRENGTH),
            getAssignedSkillPoints(Skill.DEXTERITY),
            getAssignedSkillPoints(Skill.INTELLIGENCE),
            getAssignedSkillPoints(Skill.DEFENCE),
            getAssignedSkillPoints(Skill.AGILITY)
        });
    }

    /**
     * Saves the current equipped gear and provided skill points.
     */
    public void saveSkillPointsAndItems(String name, int[] skillPoints) {
        EncodingSettings encodingSettings = new EncodingSettings(true, true);
        List<ItemStack> equippedItems = Models.Inventory.getEquippedItems();

        String weaponEncodedString = null;
        List<String> armourEncodedStrings = new ArrayList<>(List.of("", "", "", ""));
        List<String> accessoryEncodedStrings = new ArrayList<>(List.of("", "", "", ""));

        for (int i = 0; i < equippedItems.size(); i++) {
            ItemStack itemStack = equippedItems.get(i);

            Optional<WynnItem> wynnItemOptional = Models.Item.getWynnItem(itemStack);
            if (wynnItemOptional.isEmpty()) continue;

            WynnItem wynnItem = wynnItemOptional.get();
            ErrorOr<EncodedByteBuffer> errorOrEncoded = Models.ItemEncoding.encodeItem(wynnItem, encodingSettings);
            if (errorOrEncoded.hasError()) {
                WynntilsMod.warn(
                        "Failed to encode " + itemStack.getHoverName().getString() + ": " + errorOrEncoded.getError());
                continue;
            }

            String encoded = Models.ItemEncoding.makeItemString(wynnItem, errorOrEncoded.getValue());

            if (i < 4) {
                armourEncodedStrings.set(i, encoded);
            } else if (i < 8) {
                accessoryEncodedStrings.set(i - 4, encoded);
            } else {
                weaponEncodedString = encoded;
            }
        }

        SavableSkillPointSet assignedSkillPointSet = new SavableSkillPointSet(
                skillPoints, weaponEncodedString, armourEncodedStrings, accessoryEncodedStrings);
        Services.loadout.saveSkillPointLoadoutAndTomes(
                name, assignedSkillPointSet, Models.Character.getCurrentTomeSet());
        WynntilsMod.info("Saved skill point build: " + name + " " + assignedSkillPointSet);
    }

    public void saveCurrentSkillPointsAndItems(String name) {
        saveSkillPointsAndItems(name, new int[] {
            getAssignedSkillPoints(Skill.STRENGTH),
            getAssignedSkillPoints(Skill.DEXTERITY),
            getAssignedSkillPoints(Skill.INTELLIGENCE),
            getAssignedSkillPoints(Skill.DEFENCE),
            getAssignedSkillPoints(Skill.AGILITY)
        });
    }

    public void loadLoadout(String name, Consumer<String> onError, Runnable onComplete) {
        ContainerUtils.closeBackgroundContainer();

        SavableSkillPointSet target = Services.loadout.getSkillPointLoadout(name);
        if (target == null) {
            if (onError != null) onError.accept("Loadout not found: " + name);
            return;
        }

        List<ClickAction> clickActions = calculateClickActions(target.getSkillPointsAsArray());
        int batchSize = 5; // tune this if needed (5 clicks = 1 packet burst)

        QueryBuilder builder = ScriptedContainerQuery.builder("Loading Skill Point Loadout Query")
                .onError(msg -> {
                    WynntilsMod.warn("Failed to load skill point loadout: " + msg);
                    if (onError != null) onError.accept(msg);
                })
                .then(QueryStep.useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                        .expectContainer(CharacterInfoContainer.class)
                        .verifyContentChange((container, changes, changeType) ->
                                verifyChange(container, changes, changeType, CONTENT_BOOK_SLOT))
                        .processIncomingContainer(c -> {}));

        for (int i = 0; i < clickActions.size(); i += batchSize) {
            List<ClickAction> batch = clickActions.subList(i, Math.min(i + batchSize, clickActions.size()));
            builder.then(QueryStep.runInSameContainer(container -> {
                        for (ClickAction click : batch) {
                            if (click.shift()) {
                                ContainerUtils.shiftClickOnSlot(
                                        click.slot(), container.containerId(), click.button(), container.items());
                            } else {
                                ContainerUtils.clickOnSlot(
                                        click.slot(), container.containerId(), click.button(), container.items());
                            }
                        }
                        return false;
                    })
                    .withNextOperationDelay(1));
        }

        builder.execute(() -> {
            // Update local cache directly instead of running a conflicting container query
            for (int i = 0; i < Skill.values().length; i++) {
                assignedSkillPoints.put(Skill.values()[i], target.getSkillPointsAsArray()[i]);
            }
            calculateTotalSkillPoints();

            if (onComplete != null) onComplete.run();
        });

        builder.build().executeQuery();
    }

    /**
     * Closes any open containers (but not the screen shown) then queries compass (and tome menus) depending on if
     * tomes have been unlocked.
     */
    public void populateSkillPoints() {
        ContainerUtils.closeBackgroundContainer();

        Managers.TickScheduler.scheduleNextTick(() -> {
            assignedSkillPoints = new EnumMap<>(Skill.class);
            calculateGearSkillPoints();
            calculateStatusEffectSkillPoints();
            Models.Character.queryAssignedAndTomeSkillPoints();
        });
    }

    public int getTotalSkillPoints(Skill skill) {
        return totalSkillPoints.getOrDefault(skill, 0);
    }

    public int getTotalSum() {
        return totalSkillPoints.values().stream().reduce(0, Integer::sum);
    }

    public int getGearSkillPoints(Skill skill) {
        return gearSkillPoints.getOrDefault(skill, 0);
    }

    public int getGearSum() {
        return gearSkillPoints.values().stream().reduce(0, Integer::sum);
    }

    public int getCraftedSkillPoints(Skill skill) {
        return craftedSkillPoints.getOrDefault(skill, 0);
    }

    public int getCraftedSum() {
        return craftedSkillPoints.values().stream().reduce(0, Integer::sum);
    }

    public int getTomeSkillPoints(Skill skill) {
        return tomeSkillPoints.getOrDefault(skill, 0);
    }

    public int getTomeSum() {
        return tomeSkillPoints.values().stream().reduce(0, Integer::sum);
    }

    public int getStatusEffectSkillPoints(Skill skill) {
        return statusEffectSkillPoints.getOrDefault(skill, 0);
    }

    public int getStatusEffectsSum() {
        return statusEffectSkillPoints.values().stream().reduce(0, Integer::sum);
    }

    public int getSetBonusSkillPoints(Skill skill) {
        return setBonusSkillPoints.getOrDefault(skill, 0);
    }

    public int getSetBonusSum() {
        return setBonusSkillPoints.values().stream().reduce(0, Integer::sum);
    }

    public int getAssignedSkillPoints(Skill skill) {
        return assignedSkillPoints.getOrDefault(skill, 0);
    }

    public int getAssignedSum() {
        return assignedSkillPoints.values().stream().reduce(0, Integer::sum);
    }

    /**
     * @return true if any skills are assigned outside of the 0-100 range.
     */
    public boolean hasIllegalAssigned() {
        for (Skill skill : Skill.values()) {
            if (getAssignedSkillPoints(skill) < 0 || getAssignedSkillPoints(skill) > 100) {
                return true;
            }
        }
        return false;
    }

    private void calculateGearSkillPoints() {
        gearSkillPoints = new EnumMap<>(Skill.class);
        craftedSkillPoints = new EnumMap<>(Skill.class);
        setBonusSkillPoints = new EnumMap<>(Skill.class);

        for (ItemStack itemStack : Models.Inventory.getEquippedItems()) {
            calculateSingleGearSkillPoints(itemStack);
        }

        Models.Set.getUniqueSetNames().forEach(name -> {
            int trueCount = Models.Set.getTrueCount(name);
            Models.Set.getSetInfo(name).getBonusForItems(trueCount).minor().forEach((bonus, value) -> {
                if (bonus instanceof SkillStatType skillStat) {
                    setBonusSkillPoints.merge(skillStat.getSkill(), value, Integer::sum);
                }
            });
        });
    }

    private void calculateSingleGearSkillPoints(ItemStack itemStack) {
        Optional<WynnItem> wynnItemOptional = Models.Item.getWynnItem(itemStack);
        if (wynnItemOptional.isEmpty()) return; // Empty slot

        if (wynnItemOptional.get() instanceof GearItem gear) {
            gear.getIdentifications().forEach(x -> {
                if (x.statType() instanceof SkillStatType skillStat) {
                    gearSkillPoints.merge(skillStat.getSkill(), x.value(), Integer::sum);
                }
            });

        } else if (wynnItemOptional.get() instanceof CraftedGearItem craftedGear) {
            craftedGear.getIdentifications().forEach(x -> {
                if (x.statType() instanceof SkillStatType skillStat) {
                    craftedSkillPoints.merge(skillStat.getSkill(), x.value(), Integer::sum);
                }
            });
        } else {
            WynntilsMod.warn("Skill Point Model failed to parse gear: " + LoreUtils.getStringLore(itemStack));
        }
    }

    private boolean verifyChange(
            ContainerContent content,
            Int2ObjectFunction<ItemStack> changes,
            ContainerContentChangeType changeType,
            int contentBookSlot) {
        // soul points resent last for both containers
        return changeType == ContainerContentChangeType.SET_CONTENT
                && changes.containsKey(contentBookSlot)
                && (content.items().get(contentBookSlot).getItem() == Items.POTION);
    }

    public void processAssignedSkillPoints(ContainerContent content) {
        for (Integer slot : SKILL_POINT_TOTAL_SLOTS) {
            Optional<WynnItem> wynnItemOptional =
                    Models.Item.getWynnItem(content.items().get(slot));
            if (wynnItemOptional.isPresent() && wynnItemOptional.get() instanceof SkillPointItem skillPoint) {
                assignedSkillPoints.merge(skillPoint.getSkill(), skillPoint.getAssignedAmount(), Integer::sum);
            } else {
                WynntilsMod.warn("Skill Point Model failed to parse skill point item: "
                        + LoreUtils.getStringLore(content.items().get(slot)));
            }
        }
    }

    public void processTomeSkillPoints(ContainerContent content) {
        ItemStack itemStack = content.items().get(SKILL_POINT_TOME_SLOT);
        Optional<WynnItem> wynnItemOptional = Models.Item.getWynnItem(itemStack);
        if (wynnItemOptional.isPresent() && wynnItemOptional.get() instanceof TomeItem tome) {
            tome.getIdentifications().forEach(x -> {
                if (x.statType() instanceof SkillStatType skillStat) {
                    tomeSkillPoints.merge(skillStat.getSkill(), x.value(), Integer::sum);
                }
            });
        } else if (LoreUtils.getStringLore(itemStack).contains("§6Requirements:")) {
            // no-op, this is a tome that has not been unlocked or is not used by the player
        } else {
            WynntilsMod.warn("Skill Point Model failed to parse tome: "
                    + LoreUtils.getStringLore(content.items().get(SKILL_POINT_TOME_SLOT)));
        }
    }

    private void calculateStatusEffectSkillPoints() {
        statusEffectSkillPoints = new EnumMap<>(Skill.class);
        Models.StatusEffect.getStatusEffects().forEach(statusEffect -> {
            for (Skill skill : Skill.values()) {
                if (statusEffect.getName().contains(skill.getDisplayName())) {
                    statusEffectSkillPoints.merge(
                            skill,
                            Integer.parseInt(statusEffect.getModifier().getStringWithoutFormatting()),
                            Integer::sum);
                }
            }
        });
    }

    public void calculateTotalSkillPoints() {
        for (Skill skill : Skill.values()) {
            totalSkillPoints.put(
                    skill,
                    getAssignedSkillPoints(skill)
                            + getGearSkillPoints(skill)
                            + getSetBonusSkillPoints(skill)
                            + getTomeSkillPoints(skill)
                            + getCraftedSkillPoints(skill)
                            + getStatusEffectSkillPoints(skill));
        }
    }

    public void resetAssignedAndTomeSkillPoints() {
        assignedSkillPoints = new EnumMap<>(Skill.class);
        tomeSkillPoints = new EnumMap<>(Skill.class);
    }

    private List<ClickAction> calculateClickActions(int[] targetSkillPoints) {
        List<ClickAction> actions = new ArrayList<>();
        Map<Skill, Integer> negatives = new EnumMap<>(Skill.class);
        Map<Skill, Integer> positives = new EnumMap<>(Skill.class);

        for (int i = 0; i < Skill.values().length; i++) {
            int difference = targetSkillPoints[i] - getAssignedSkillPoints(Skill.values()[i]);
            if (difference > 0) {
                positives.put(Skill.values()[i], difference);
            } else if (difference < 0) {
                negatives.put(Skill.values()[i], difference);
            }
        }

        boolean confirmationCompleted = false;
        for (Map.Entry<Skill, Integer> entry : negatives.entrySet()) {
            int slot = SKILL_POINT_TOTAL_SLOTS[entry.getKey().ordinal()];
            int diff = Math.abs(entry.getValue());
            int fives = diff / 5;
            int ones = diff % 5;

            for (int i = 0; i < fives; i++) {
                actions.add(new ClickAction(slot, GLFW.GLFW_MOUSE_BUTTON_RIGHT, true));
                if (!confirmationCompleted) {
                    actions.add(new ClickAction(slot, GLFW.GLFW_MOUSE_BUTTON_RIGHT, true));
                    confirmationCompleted = true;
                }
            }
            for (int i = 0; i < ones; i++) {
                actions.add(new ClickAction(slot, GLFW.GLFW_MOUSE_BUTTON_RIGHT, false));
                if (!confirmationCompleted) {
                    actions.add(new ClickAction(slot, GLFW.GLFW_MOUSE_BUTTON_RIGHT, false));
                    confirmationCompleted = true;
                }
            }
        }

        for (Map.Entry<Skill, Integer> entry : positives.entrySet()) {
            int slot = SKILL_POINT_TOTAL_SLOTS[entry.getKey().ordinal()];
            int diff = Math.abs(entry.getValue());
            int fives = diff / 5;
            int ones = diff % 5;

            for (int i = 0; i < fives; i++) {
                actions.add(new ClickAction(slot, GLFW.GLFW_MOUSE_BUTTON_LEFT, true));
            }
            for (int i = 0; i < ones; i++) {
                actions.add(new ClickAction(slot, GLFW.GLFW_MOUSE_BUTTON_LEFT, false));
            }
        }

        return actions;
    }
}
