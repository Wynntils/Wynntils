/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.models.characterstats.actionbar.CoordinatesSegment;
import com.wynntils.models.characterstats.actionbar.HealthSegment;
import com.wynntils.models.characterstats.actionbar.ManaSegment;
import com.wynntils.models.characterstats.actionbar.PowderSpecialSegment;
import com.wynntils.models.characterstats.actionbar.SprintSegment;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CharacterStatsModel extends Model {
    private final CoordinatesSegment coordinatesSegment = new CoordinatesSegment(this::centerSegmentCleared);
    private final HealthSegment healthSegment = new HealthSegment();
    private final ManaSegment manaSegment = new ManaSegment();
    private final PowderSpecialSegment powderSpecialSegment = new PowderSpecialSegment();
    private final SprintSegment sprintSegment = new SprintSegment();

    public CharacterStatsModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(coordinatesSegment);
        Handlers.ActionBar.registerSegment(healthSegment);
        Handlers.ActionBar.registerSegment(manaSegment);
        Handlers.ActionBar.registerSegment(powderSpecialSegment);
        Handlers.ActionBar.registerSegment(sprintSegment);
    }

    public CappedValue getHealth() {
        return healthSegment.getHealth();
    }

    public CappedValue getMana() {
        return manaSegment.getMana();
    }

    public CappedValue getSprint() {
        return sprintSegment.getSprint();
    }

    public float getPowderSpecialCharge() {
        return powderSpecialSegment.getPowderSpecialCharge();
    }

    public Powder getPowderSpecialType() {
        return powderSpecialSegment.getPowderSpecialType();
    }

    public void hideHealth(boolean shouldHide) {
        healthSegment.setHidden(shouldHide);
    }

    public void hideMana(boolean shouldHide) {
        manaSegment.setHidden(shouldHide);
    }

    public void hideCoordinates(boolean shouldHide) {
        coordinatesSegment.setHidden(shouldHide);
    }

    /**
     * Return the maximum number of soul points the character can currently have
     */
    private int getMaxSoulPoints() {
        if (Models.Character.isVeteran()) {
            return 15;
        }

        int maxIfNotVeteran =
                10 + MathUtils.clamp(Models.CombatXp.getCombatLevel().current() / 15, 0, 5);
        if (getCurrentSoulPoints() > maxIfNotVeteran) {
            return 15;
        }
        return maxIfNotVeteran;
    }

    /**
     * Return the current number of soul points of the character, or -1 if unable to determine
     */
    private int getCurrentSoulPoints() {
        ItemStack soulPoints = McUtils.inventory().getItem(8);
        if (soulPoints.getItem() == Items.NETHER_STAR || soulPoints.getItem() == Items.DIAMOND_AXE) {
            return soulPoints.getCount();
        }

        return -1;
    }

    public CappedValue getSoulPoints() {
        // FIXME: We should be able to cache this
        return new CappedValue(getCurrentSoulPoints(), getMaxSoulPoints());
    }

    /**
     * Return the time in game ticks (1/20th of a second, 50ms) until the next soul point is given
     *
     * Also check that {@code {@link #getMaxSoulPoints()} >= {@link #getSoulPoints()}},
     * in which case soul points are already full
     */
    public int getTicksToNextSoulPoint() {
        if (McUtils.mc().level == null) return -1;
        return 24000 - (int) (McUtils.mc().level.getDayTime() % 24000);
    }

    public List<GearInfo> getWornGear() {
        Player player = McUtils.player();

        // Check if main hand has valid weapon. We can hold weapons we can't yield, so we need
        // to check that it is indeed a valid and usable weapon
        List<GearInfo> wornGear = new ArrayList<>();
        Optional<GearItem> mainHandGearItem = Models.Item.asWynnItem(player.getMainHandItem(), GearItem.class);
        if (mainHandGearItem.isPresent()) {
            GearInfo gearInfo = mainHandGearItem.get().getGearInfo();
            if (gearInfo.type().isValidWeapon(Models.Character.getClassType())
                    && Models.CombatXp.getCombatLevel().current()
                            >= gearInfo.requirements().level()) {
                wornGear.add(gearInfo);
            }
        }

        // We trust that Wynncraft do not let us wear invalid gear, so no further validation checks are needed

        // Check armor slots
        player.getArmorSlots().forEach(itemStack -> {
            Optional<GearItem> armorGearItem = Models.Item.asWynnItem(itemStack, GearItem.class);
            if (armorGearItem.isPresent()) {
                GearInfo gearInfo = armorGearItem.get().getGearInfo();
                wornGear.add(gearInfo);
            }
        });

        // Check accessory slots
        InventoryUtils.getAccessories(player).forEach(itemStack -> {
            Optional<GearItem> accessoryGearItem = Models.Item.asWynnItem(itemStack, GearItem.class);
            if (accessoryGearItem.isPresent()) {
                GearInfo gearInfo = accessoryGearItem.get().getGearInfo();
                wornGear.add(gearInfo);
            }
        });

        return wornGear;
    }

    private void centerSegmentCleared() {
        powderSpecialSegment.replaced();
    }

    @SubscribeEvent
    public void onHeldItemChanged(ChangeCarriedItemEvent event) {
        // powders are always reset when held item is changed on Wynn, this ensures consistent behavior
        powderSpecialSegment.replaced();
    }
}
