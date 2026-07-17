/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.abilities.type.ArrowShieldAbility;
import com.wynntils.models.abilities.type.BrokenMantleAbility;
import com.wynntils.models.abilities.type.CastedAbilityType;
import com.wynntils.models.abilities.type.GuardianAngelsAbility;
import com.wynntils.models.abilities.type.JudrajimAbility;
import com.wynntils.models.abilities.type.MantleAbility;
import com.wynntils.models.abilities.type.ShamanTotem;
import com.wynntils.models.abilities.type.ShieldAbilityProperty;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.PosUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class SpellFunctions {

    @TemplateFunction(name = "arrow_shield_count", aliases = { "arrow_shield" })
    public int arrowShieldCountFunction() {
        if (Models.Character.getClassType() != ClassType.ARCHER)
            return 0;
        return Models.CastedAbility.getActiveAbility(ArrowShieldAbility.class).map(ArrowShieldAbility::getCharge).orElse(0);
    }

    @TemplateFunction(name = "guardian_angels_count", aliases = { "guardian_angels" })
    public int guardianAngelsCountFunction() {
        if (Models.Character.getClassType() != ClassType.ARCHER)
            return 0;
        return Models.CastedAbility.getActiveAbility(GuardianAngelsAbility.class).map(GuardianAngelsAbility::getCharge).orElse(0);
    }

    @TemplateFunction(name = "mantle_shield_count", aliases = { "mantle_shield" })
    public int mantleShieldCountFunction() {
        if (Models.Character.getClassType() != ClassType.WARRIOR)
            return 0;
        return Models.CastedAbility.getActiveAbility(MantleAbility.class).map(MantleAbility::getCharge).orElse(0);
    }

    @TemplateFunction(name = "broken_mantle_shield_count", aliases = { "broken_mantle_shield" })
    public int brokenMantleShieldCountFunction() {
        if (Models.Character.getClassType() != ClassType.WARRIOR)
            return 0;
        return Models.CastedAbility.getActiveAbility(BrokenMantleAbility.class).map(BrokenMantleAbility::getCharge).orElse(0);
    }

    @TemplateFunction(name = "shield_type_name", aliases = { "shield_type" })
    public String shieldTypeNameFunction() {
        return Models.CastedAbility.getActiveAbilities().stream().filter(a -> a instanceof ShieldAbilityProperty).findFirst().map(CastedAbilityType::getName).orElse("");
    }

    @TemplateFunction(name = "judrajim_active", aliases = { "is_judrajim_active" })
    public boolean judrajimActiveFunction() {
        return Models.CastedAbility.getActiveAbility(JudrajimAbility.class).isPresent();
    }

    @TemplateFunction(name = "shaman_mask")
    public String shamanMaskFunction() {
        return shamanMaskFunction(true, false);
    }

    @TemplateFunction(name = "shaman_mask")
    public String shamanMaskFunction(boolean useShortName, boolean isColored) {
        ChatFormatting color = isColored ? Models.ShamanMask.getCurrentMaskType().getColor() : ChatFormatting.WHITE;
        String name = useShortName ? Models.ShamanMask.getCurrentMaskType().getAlias() : Models.ShamanMask.getCurrentMaskType().getName();
        return color + name;
    }

    @TemplateFunction(name = "shaman_totem_state")
    public String shamanTotemStateFunction(int totemNumber) {
        ShamanTotem shamanTotem = Models.ShamanTotem.getTotem(totemNumber);
        if (shamanTotem == null) {
            return "";
        }
        return shamanTotem.getState().toString().toUpperCase(Locale.ROOT);
    }

    @TemplateFunction(name = "shaman_totem_location")
    public String shamanTotemLocationFunction(int totemNumber) {
        ShamanTotem shamanTotem = Models.ShamanTotem.getTotem(totemNumber);
        if (shamanTotem == null) {
            return "";
        }
        if (shamanTotem.getState() != ShamanTotem.TotemState.ACTIVE) {
            return "";
        }
        return Location.containing(shamanTotem.getPosition()).toString();
    }

    @TemplateFunction(name = "shaman_totem_time_left")
    public int shamanTotemTimeLeftFunction(int totemNumber) {
        ShamanTotem shamanTotem = Models.ShamanTotem.getTotem(totemNumber);
        if (shamanTotem == null) {
            return 0;
        }
        if (shamanTotem.getState() != ShamanTotem.TotemState.ACTIVE) {
            return 0;
        }
        return shamanTotem.getTime();
    }

    @TemplateFunction(name = "shaman_totem_distance")
    public double shamanTotemDistanceFunction(int totemNumber) {
        ShamanTotem shamanTotem = Models.ShamanTotem.getTotem(totemNumber);
        if (shamanTotem == null) {
            return 0d;
        }
        if (shamanTotem.getState() != ShamanTotem.TotemState.ACTIVE) {
            return 0d;
        }
        return McUtils.player().position().distanceTo(PosUtils.toVec3(shamanTotem.getPosition()));
    }

    @TemplateFunction(name = "shaman_totem_transfused_amount")
    public int shamanTotemTransfusedAmountFunction(int totemNumber) {
        ShamanTotem shamanTotem = Models.ShamanTotem.getTotem(totemNumber);
        if (shamanTotem == null)
            return 0;
        if (shamanTotem.getState() != ShamanTotem.TotemState.ACTIVE)
            return 0;
        return shamanTotem.getTransfusedAmount();
    }

    @TemplateFunction(name = "shaman_totem_poison_amount")
    public String shamanTotemPoisonAmountFunction(int totemNumber) {
        ShamanTotem shamanTotem = Models.ShamanTotem.getTotem(totemNumber);
        if (shamanTotem == null)
            return "";
        if (shamanTotem.getState() != ShamanTotem.TotemState.ACTIVE)
            return "";
        return shamanTotem.getPoisonAmount().toString();
    }
}
