/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.EmeraldPouchItemStack;
import com.wynntils.wynn.item.GearItemStack;
import com.wynntils.wynn.item.IngredientItemStack;
import com.wynntils.wynn.item.IntelligenceSkillPointsItemStack;
import com.wynntils.wynn.item.PowderItemStack;
import com.wynntils.wynn.item.ServerItemStack;
import com.wynntils.wynn.item.SoulPointItemStack;
import com.wynntils.wynn.item.UnidentifiedItemStack;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemStackTransformer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

public abstract class ItemStackModel extends Model {
    private final ItemStackTransformer transformer;

    protected ItemStackModel(Predicate<ItemStack> pred, Function<ItemStack, WynnItemStack> cons) {
        this.transformer = new ItemStackTransformer(pred, cons);
    }

    @Override
    public void init() {
        Managers.ItemStackTransform.registerTransformer(transformer);
    }

    @Override
    public void disable() {
        Managers.ItemStackTransform.unregisterTransformer(transformer);
    }

    /** Model Declarations **/
    public static final class EmeraldPouchItemStackModel extends ItemStackModel {
        public EmeraldPouchItemStackModel() {
            super(WynnItemMatchers::isEmeraldPouch, EmeraldPouchItemStack::new);
        }
    }

    public static final class GearItemStackModel extends ItemStackModel {
        public GearItemStackModel() {
            super(WynnItemMatchers::isKnownGear, GearItemStack::new);
        }
    }

    public static final class IngredientItemStackModel extends ItemStackModel {
        public IngredientItemStackModel() {
            super(WynnItemMatchers::isIngredient, IngredientItemStack::new);
        }
    }

    public static final class IntelligenceSkillPointsItemStackModel extends ItemStackModel {
        public IntelligenceSkillPointsItemStackModel() {
            super(WynnItemMatchers::isIntelligenceSkillPoints, IntelligenceSkillPointsItemStack::new);
        }
    }

    public static final class PowderItemStackModel extends ItemStackModel {
        public PowderItemStackModel() {
            super(WynnItemMatchers::isPowder, PowderItemStack::new);
        }
    }

    public static final class ServerItemStackModel extends ItemStackModel {
        public ServerItemStackModel() {
            super(WynnItemMatchers::isServerItem, ServerItemStack::new);
        }
    }

    public static final class SoulPointItemStackModel extends ItemStackModel {
        public SoulPointItemStackModel() {
            super(WynnItemMatchers::isSoulPoint, SoulPointItemStack::new);
        }
    }

    public static final class UnidentifiedItemStackModel extends ItemStackModel {
        public UnidentifiedItemStackModel() {
            super(WynnItemMatchers::isUnidentified, UnidentifiedItemStack::new);
        }
    }
}
