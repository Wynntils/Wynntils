/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.embellishments;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.inventory.InventoryInteraction;
import com.wynntils.handlers.inventory.event.InventoryInteractionEvent;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.AmplifierItem;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.CorruptedCacheItem;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.DungeonKeyItem;
import com.wynntils.models.items.items.game.EmeraldItem;
import com.wynntils.models.items.items.game.EmeraldPouchItem;
import com.wynntils.models.items.items.game.GatheringToolItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.HorseItem;
import com.wynntils.models.items.items.game.InsulatorItem;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.items.items.game.MultiHealthPotionItem;
import com.wynntils.models.items.items.game.PotionItem;
import com.wynntils.models.items.items.game.PowderItem;
import com.wynntils.models.items.items.game.RuneItem;
import com.wynntils.models.items.items.game.SimulatorItem;
import com.wynntils.models.items.items.game.TeleportScrollItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.items.items.game.TrinketItem;
import com.wynntils.models.items.items.game.UnknownGearItem;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.ShinyItemProperty;
import com.wynntils.models.profession.type.MaterialProfile;
import com.wynntils.utils.mc.McUtils;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.EMBELLISHMENTS)
public class ItemInteractionSoundsFeature extends Feature {
    private static final SoundEvent PICK_UP_SOUND = newItemSound("pick-up");
    private static final SoundEvent PLACE_GENERIC_SOUND = newItemSound("place-generic");

    private static final SoundEvent MYTHIC_LAYER_SOUND = newItemSound("mythic-layer");
    private static final SoundEvent SHINY_LAYER_SOUND = newItemSound("shiny-layer");

    private static final SoundEvent BOOK_SOUND = newItemSound("book");
    private static final SoundEvent BOTTLE_SOUND = newItemSound("bottle");
    private static final SoundEvent CLOTH_ITEM_SOUND = newItemSound("cloth-item");
    private static final SoundEvent DEVICE_SOUND = newItemSound("device");
    private static final SoundEvent DISH_SOUND = newItemSound("dish");
    private static final SoundEvent EMERALDS_SOUND = newItemSound("emeralds");
    private static final SoundEvent GEM_ITEM_SOUND = newItemSound("gem-item");
    private static final SoundEvent ITEM_BAG_SOUND = newItemSound("item-bag");
    private static final SoundEvent ITEM_BOX_SOUND = newItemSound("item-box");
    private static final SoundEvent METAL_ITEM_SOUND = newItemSound("metal-item");
    private static final SoundEvent PAPER_ITEM_SOUND = newItemSound("paper-item");
    private static final SoundEvent SADDLE_SOUND = newItemSound("saddle");
    private static final SoundEvent STONE_ITEM_SOUND = newItemSound("stone-item");
    private static final SoundEvent WOOD_ITEM_SOUND = newItemSound("wood-item");

    private static final SoundEvent SPEAR_SOUND = newItemSound("weapon.spear");
    private static final SoundEvent BOW_SOUND = newItemSound("weapon.bow");
    private static final SoundEvent DAGGER_SOUND = newItemSound("weapon.dagger");
    private static final SoundEvent WAND_SOUND = newItemSound("weapon.wand");
    private static final SoundEvent RELIK_SOUND = newItemSound("weapon.relik");

    private static final SoundEvent LEATHER_ARMOR_SOUND = newItemSound("armor.leather");
    private static final SoundEvent CHAIN_ARMOR_SOUND = newItemSound("armor.chain");
    private static final SoundEvent METAL_ARMOR_SOUND = newItemSound("armor.metal");

    private static final SoundEvent RING_SOUND = newItemSound("accessory.ring");
    private static final SoundEvent BRACELET_SOUND = newItemSound("accessory.bracelet");
    private static final SoundEvent NECKLACE_SOUND = newItemSound("accessory.necklace");

    private static final SoundEvent WOOD_TOOL_SOUND = newItemSound("tool.wood");
    private static final SoundEvent STONE_TOOL_SOUND = newItemSound("tool.stone");
    private static final SoundEvent METAL_TOOL_SOUND = newItemSound("tool.metal");

    private static SoundEvent newItemSound(String key) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath("wynntils", "inventory.item." + key));
    }

    @Persisted
    private final Config<Float> soundVolume = new Config<>(1.0f);

    @Persisted
    private final Config<Boolean> mythicLayer = new Config<>(true);

    @Persisted
    private final Config<Boolean> shinyLayer = new Config<>(true);

    @SubscribeEvent
    public void onInventoryInteraction(InventoryInteractionEvent event) {
        switch (event.getInteraction()) {
            case InventoryInteraction.PickUp ixn -> playSoundEvent(PICK_UP_SOUND);
            case InventoryInteraction.Place ixn -> playItemSound(ixn.placed());
            case InventoryInteraction.Spread ixn -> playItemSound(ixn.stack());
            case InventoryInteraction.Swap ixn -> playItemSound(ixn.placed());
            case InventoryInteraction.Transfer ixn -> playItemSound(ixn.transferred());
            default -> {}
        }
    }

    public void playItemSound(ItemStack stack) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(stack);
        if (wynnItemOpt.isPresent()) {
            playItemSound(wynnItemOpt.get());
        } else {
            playSoundEvent(PLACE_GENERIC_SOUND);
        }
    }

    public void playItemSound(WynnItem item) {
        playSoundEvent(getItemSound(item));
        if (mythicLayer.get()
                && item instanceof GearTierItemProperty tiered
                && tiered.getGearTier() == GearTier.MYTHIC) {
            playSoundEvent(MYTHIC_LAYER_SOUND);
        }
        if (shinyLayer.get()
                && item instanceof ShinyItemProperty shiny
                && shiny.getShinyStat().isPresent()) {
            playSoundEvent(SHINY_LAYER_SOUND);
        }
    }

    public SoundEvent getItemSound(WynnItem item) {
        return switch (item) {
            case AmplifierItem amp -> DEVICE_SOUND;
            case CharmItem charm -> STONE_ITEM_SOUND;
            case CorruptedCacheItem cache -> ITEM_BOX_SOUND;
            case CraftedConsumableItem cons -> switch (cons.getConsumableType()) {
                case POTION -> BOTTLE_SOUND;
                case FOOD -> DISH_SOUND;
                case SCROLL -> PAPER_ITEM_SOUND;
                default -> PLACE_GENERIC_SOUND;
            };
            case CraftedGearItem gear -> getGearSound(gear.getGearType(), gear.getLevel());
            case DungeonKeyItem key -> METAL_ITEM_SOUND;
            case EmeraldItem emerald -> EMERALDS_SOUND;
            case EmeraldPouchItem pouch -> ITEM_BAG_SOUND;
            case GatheringToolItem tool -> getGatheringToolSound(tool.getTier());
            case GearBoxItem box -> ITEM_BOX_SOUND;
            case GearItem gear -> getGearSound(gear.getGearType(), gear.getLevel());
            case HorseItem horse -> SADDLE_SOUND;
            case InsulatorItem ins -> DEVICE_SOUND;
            case MaterialItem mat -> getMaterialSound(mat.getMaterialProfile().getResourceType());
            case MultiHealthPotionItem potion -> BOTTLE_SOUND;
            case PotionItem potion -> BOTTLE_SOUND;
            case PowderItem powder -> CLOTH_ITEM_SOUND;
            case RuneItem rune -> STONE_ITEM_SOUND;
            case SimulatorItem sim -> DEVICE_SOUND;
            case TeleportScrollItem scroll -> PAPER_ITEM_SOUND;
            case TomeItem tome -> BOOK_SOUND;
            case TrinketItem trinket -> GEM_ITEM_SOUND;
            case UnknownGearItem gear -> getGearSound(gear.getGearType(), gear.getLevel());
            default -> PLACE_GENERIC_SOUND;
        };
    }

    public SoundEvent getGearSound(GearType type, int level) {
        return switch (type) {
            case SPEAR -> SPEAR_SOUND;
            case WAND -> WAND_SOUND;
            case DAGGER -> DAGGER_SOUND;
            case BOW -> BOW_SOUND;
            case RELIK -> RELIK_SOUND;
            case RING -> RING_SOUND;
            case BRACELET -> BRACELET_SOUND;
            case NECKLACE -> NECKLACE_SOUND;
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> getArmorSound(level);
            case MASTERY_TOME -> BOOK_SOUND;
            case CHARM -> STONE_ITEM_SOUND;
            default -> PLACE_GENERIC_SOUND;
        };
    }

    public SoundEvent getArmorSound(int level) {
        if (level < 20) {
            return LEATHER_ARMOR_SOUND;
        } else if (level >= 40 && level < 60) {
            return CHAIN_ARMOR_SOUND;
        } else {
            return METAL_ARMOR_SOUND;
        }
    }

    public SoundEvent getGatheringToolSound(int tier) {
        if (tier <= 3) {
            return WOOD_TOOL_SOUND;
        } else if (tier <= 6) {
            return STONE_TOOL_SOUND;
        } else {
            return METAL_TOOL_SOUND;
        }
    }

    public SoundEvent getMaterialSound(MaterialProfile.ResourceType type) {
        return switch (type) {
            case INGOT -> METAL_ITEM_SOUND;
            case GEM -> GEM_ITEM_SOUND;
            case WOOD -> WOOD_ITEM_SOUND;
            case PAPER -> PAPER_ITEM_SOUND;
            case STRING -> CLOTH_ITEM_SOUND;
            case GRAINS -> ITEM_BAG_SOUND;
            case OIL -> BOTTLE_SOUND;
            case MEAT -> CLOTH_ITEM_SOUND;
        };
    }

    private void playSoundEvent(SoundEvent sound) {
        McUtils.playSoundUI(sound, soundVolume.get());
    }
}
