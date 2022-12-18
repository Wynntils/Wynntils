/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.AmplifierTierProperty;
import com.wynntils.wynn.item.properties.ConsumableChargeProperty;
import com.wynntils.wynn.item.properties.CosmeticTierProperty;
import com.wynntils.wynn.item.properties.DailyRewardMultiplierProperty;
import com.wynntils.wynn.item.properties.DungeonKeyProperty;
import com.wynntils.wynn.item.properties.DurabilityProperty;
import com.wynntils.wynn.item.properties.EmeraldPouchTierProperty;
import com.wynntils.wynn.item.properties.GatheringToolProperty;
import com.wynntils.wynn.item.properties.HorseProperty;
import com.wynntils.wynn.item.properties.IngredientProperty;
import com.wynntils.wynn.item.properties.ItemTierProperty;
import com.wynntils.wynn.item.properties.MaterialProperty;
import com.wynntils.wynn.item.properties.PowderTierProperty;
import com.wynntils.wynn.item.properties.SearchOverlayProperty;
import com.wynntils.wynn.item.properties.ServerCountProperty;
import com.wynntils.wynn.item.properties.SkillIconProperty;
import com.wynntils.wynn.item.properties.SkillPointProperty;
import com.wynntils.wynn.item.properties.TeleportScrollProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

public abstract class PropertyModel extends Model {
    private final ItemPropertyWriter writer;

    protected PropertyModel(Predicate<ItemStack> pred, Consumer<WynnItemStack> cons) {
        this.writer = new ItemPropertyWriter(pred, cons);
    }

    @Override
    public void init() {
        Managers.ItemStackTransform.registerProperty(writer);
    }

    @Override
    public void disable() {
        Managers.ItemStackTransform.unregisterProperty(writer);
    }

    /** Model Declarations **/
    public static final class AmplifierTierPropertyModel extends PropertyModel {
        public AmplifierTierPropertyModel() {
            super(WynnItemMatchers::isAmplifier, AmplifierTierProperty::new);
        }
    }

    public static final class ConsumableChargePropertyModel extends PropertyModel {
        public ConsumableChargePropertyModel() {
            super(WynnItemMatchers::isConsumable, ConsumableChargeProperty::new);
        }
    }

    public static final class CosmeticTierPropertyModel extends PropertyModel {
        public CosmeticTierPropertyModel() {
            super(WynnItemMatchers::isCosmetic, CosmeticTierProperty::new);
        }
    }

    public static final class DailyRewardMultiplierPropertyModel extends PropertyModel {
        public DailyRewardMultiplierPropertyModel() {
            super(WynnItemMatchers::isDailyRewardsChest, DailyRewardMultiplierProperty::new);
        }
    }

    public static final class DungeonKeyPropertyModel extends PropertyModel {
        public DungeonKeyPropertyModel() {
            super(WynnItemMatchers::isDungeonKey, DungeonKeyProperty::new);
        }
    }

    public static final class DurabilityPropertyModel extends PropertyModel {
        public DurabilityPropertyModel() {
            super(WynnItemMatchers::isDurabilityItem, DurabilityProperty::new);
        }
    }

    public static final class EmeraldPouchTierPropertyModel extends PropertyModel {
        public EmeraldPouchTierPropertyModel() {
            super(WynnItemMatchers::isEmeraldPouch, EmeraldPouchTierProperty::new);
        }
    }

    public static final class GatheringToolPropertyModel extends PropertyModel {
        public GatheringToolPropertyModel() {
            super(WynnItemMatchers::isGatheringTool, GatheringToolProperty::new);
        }
    }

    public static final class HorsePropertyModel extends PropertyModel {
        public HorsePropertyModel() {
            super(WynnItemMatchers::isHorse, HorseProperty::new);
        }
    }

    public static final class IngredientPropertyModel extends PropertyModel {
        public IngredientPropertyModel() {
            super(WynnItemMatchers::isIngredient, IngredientProperty::new);
        }
    }

    public static final class ItemTierPropertyModel extends PropertyModel {
        public ItemTierPropertyModel() {
            super(WynnItemMatchers::isTieredItem, ItemTierProperty::new);
        }
    }

    public static final class MaterialPropertyModel extends PropertyModel {
        public MaterialPropertyModel() {
            super(WynnItemMatchers::isMaterial, MaterialProperty::new);
        }
    }

    public static final class PowderTierPropertyModel extends PropertyModel {
        public PowderTierPropertyModel() {
            super(WynnItemMatchers::isPowder, PowderTierProperty::new);
        }
    }

    public static final class SearchOverlayPropertyModel extends PropertyModel {
        public SearchOverlayPropertyModel() {
            super(itemstack -> true, SearchOverlayProperty::new);
        }
    }

    public static final class ServerCountPropertyModel extends PropertyModel {
        public ServerCountPropertyModel() {
            super(WynnItemMatchers::isServerItem, ServerCountProperty::new);
        }
    }

    public static final class SkillIconPropertyModel extends PropertyModel {
        public SkillIconPropertyModel() {
            super(WynnItemMatchers::isSkillTyped, SkillIconProperty::new);
        }
    }

    public static final class SkillPointPropertyModel extends PropertyModel {
        public SkillPointPropertyModel() {
            super(WynnItemMatchers::isSkillPoint, SkillPointProperty::new);
        }
    }

    public static final class TeleportScrollPropertyModel extends PropertyModel {
        public TeleportScrollPropertyModel() {
            super(WynnItemMatchers::isTeleportScroll, TeleportScrollProperty::new);
        }
    }
}
