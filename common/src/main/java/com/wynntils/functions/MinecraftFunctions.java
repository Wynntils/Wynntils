/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.mc.mixin.accessors.MinecraftAccessor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.wynn.RaycastUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.IdentifierException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class MinecraftFunctions {

    @TemplateFunction(name = "my_location", aliases = { "my_loc" })
    public Location myLocationFunction() {
        return new Location(McUtils.player().blockPosition());
    }

    @TemplateFunction(name = "dir", aliases = { "yaw" })
    public double dirFunction(boolean wrap) {
        double dir = McUtils.player().getYRot();
        return wrap ? Mth.wrapDegrees(dir) : dir;
    }

    @TemplateFunction(name = "fps")
    public int fpsFunction() {
        return MinecraftAccessor.getFps();
    }

    @TemplateFunction(name = "ticks")
    public long ticksFunction() {
        return McUtils.mc().level.getGameTime();
    }

    @TemplateFunction(name = "key_pressed")
    public boolean keyPressedFunction(int keyCode) {
        return KeyboardUtils.isKeyDown(keyCode);
    }

    @TemplateFunction(name = "minecraft_effect_duration")
    public int minecraftEffectDurationFunction(String effectName) {
        Identifier effectLocation;
        try {
            effectLocation = Identifier.withDefaultNamespace(effectName);
        } catch (IdentifierException e) {
            // Effect name contains invalid characters
            return -1;
        }
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.get(effectLocation).orElse(null);
        // Effect holder not found
        if (effectHolder == null)
            return -1;
        // Check if the player has the effect
        if (McUtils.player().hasEffect(effectHolder)) {
            MobEffectInstance effectInstance = McUtils.player().getEffect(effectHolder);
            if (effectInstance != null && effectInstance.getDuration() >= 0) {
                return effectInstance.getDuration();
            }
        }
        // Effect not active
        return -1;
    }

    @TemplateFunction(name = "location_at_crosshair", aliases = { "crosshair_loc" })
    public Location locationAtCrosshairFunction(boolean colliderOnly, double distance) {
        Optional<BlockPos> hitBlock = RaycastUtils.getTargetedBlockPosition(distance, colliderOnly);
        if (hitBlock.isEmpty())
            return Location.ZERO;
        return new Location(hitBlock.get());
    }

    @TemplateFunction(name = "pitch")
    public double pitchFunction() {
        return McUtils.player().getXRot();
    }
}
