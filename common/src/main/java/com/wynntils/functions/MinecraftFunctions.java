/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.mc.mixin.accessors.MinecraftAccessor;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;

import net.minecraft.IdentifierException;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class MinecraftFunctions {

    @TemplateFunction(name = "my_location", aliases = "my_loc")
    public static Location myLocationFunction() {
        return new Location(McUtils.player().blockPosition());
    }

    @TemplateFunction(name = "dir")
    public static double dirFunction() {
        return McUtils.player().getYRot();

    }

    @TemplateFunction(name = "fps")
    public static int fpsFunction() {
        return MinecraftAccessor.getFps();
    }


    @TemplateFunction(name = "ticks")
    public static long ticksFunction() {
        return McUtils.mc().level.getGameTime();
    }


    @TemplateFunction(name = "key_pressed")
    public static boolean keyPressedFunction(int keyCode) {
        return KeyboardUtils.isKeyDown(keyCode);
    }

    @TemplateFunction(name = "minecraft_effect_duration")
    public static int minecraftEffectDurationFunction(String effectName) {
        Identifier effectLocation;
        try {
            effectLocation = Identifier.withDefaultNamespace(effectName);
        } catch (IdentifierException e) {
            return -1; // Effect name contains invalid characters
        }

        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.get(effectLocation).orElse(null);

        if (effectHolder == null) return -1; // Effect holder not found

        // Check if the player has the effect
        if (McUtils.player().hasEffect(effectHolder)) {
            MobEffectInstance effectInstance = McUtils.player().getEffect(effectHolder);

            if (effectInstance != null && effectInstance.getDuration() >= 0) {
                return effectInstance.getDuration();
            }
        }

        return -1; // Effect not active
    }

}
