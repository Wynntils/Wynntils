/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
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
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

public class ItemModel extends Model {
    @Override
    public void init() {
        Handlers.Item.registerAnnotator(new EmeraldPouchAnnotator());
        Handlers.Item.registerAnnotator(new GearAnnotator());
        Handlers.Item.registerAnnotator(new IngredientAnnotator());
        Handlers.Item.registerAnnotator(new IntelligenceSkillPointsAnnotator());
        Handlers.Item.registerAnnotator(new PowderAnnotator());
        Handlers.Item.registerAnnotator(new ServerAnnotator());
        Handlers.Item.registerAnnotator(new SoulPointAnnotator());
        Handlers.Item.registerAnnotator(new UnidentifiedAnnotator());
    }

    @Override
    public void disable() {
        // FIXME
    }

    public abstract static class Annotator implements ItemAnnotator {
        private final Predicate<ItemStack> pred;
        private final Function<ItemStack, WynnItemStack> cons;

        protected Annotator(Predicate<ItemStack> pred, Function<ItemStack, WynnItemStack> cons) {
            this.pred = pred;
            this.cons = cons;
        }

        public ItemAnnotation getAnnotation(ItemStack itemStack) {
            if (!pred.test(itemStack)) return null;
            WynnItemStack converted = cons.apply(itemStack);

            return converted;
        }
    }


    /** Model Declarations **/
    public static final class EmeraldPouchAnnotator extends Annotator {
        public EmeraldPouchAnnotator() {
            super(WynnItemMatchers::isEmeraldPouch, EmeraldPouchItemStack::new);
        }
    }

    public static final class GearAnnotator extends Annotator {
        public GearAnnotator() {
            super(WynnItemMatchers::isKnownGear, GearItemStack::new);
        }
    }

    public static final class IngredientAnnotator extends Annotator {
        public IngredientAnnotator() {
            super(WynnItemMatchers::isIngredient, IngredientItemStack::new);
        }
    }

    public static final class IntelligenceSkillPointsAnnotator extends Annotator {
        public IntelligenceSkillPointsAnnotator() {
            super(WynnItemMatchers::isIntelligenceSkillPoints, IntelligenceSkillPointsItemStack::new);
        }
    }

    public static final class PowderAnnotator extends Annotator {
        public PowderAnnotator() {
            super(WynnItemMatchers::isPowder, PowderItemStack::new);
        }
    }

    public static final class ServerAnnotator extends Annotator {
        public ServerAnnotator() {
            super(WynnItemMatchers::isServerItem, ServerItemStack::new);
        }
    }

    public static final class SoulPointAnnotator extends Annotator {
        public SoulPointAnnotator() {
            super(WynnItemMatchers::isSoulPoint, SoulPointItemStack::new);
        }
    }

    public static final class UnidentifiedAnnotator extends Annotator {
        public UnidentifiedAnnotator() {
            super(WynnItemMatchers::isUnidentified, UnidentifiedItemStack::new);
        }
    }
}
