/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type.castedabilities;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3fc;

public class RitualCircleAbility extends CastedAbilityType {
    private static final ClassType CLASS_TYPE = ClassType.SHAMAN;
    private static final String NAME = "Ritual Circle";
    private static final String GROUP = "Ritual Circle";

    private static final float RADIUS_SCALE_STEP = 33.532f;
    private static final int MAX_RADIUS = 12;

    private static final float PULSE = 0.049f;
    private static final float PULSE_LOW = 1f - PULSE;
    private static final float PULSE_HIGH = 1f + PULSE;

    private int lastRadius = 0;

    public RitualCircleAbility() {
        super(CLASS_TYPE, null, null, NAME, GROUP, true);
    }

    @Override
    public int getCharge() {
        for (Integer entityId : entityIds) {
            Entity root = McUtils.mc().level.getEntity(entityId);
            if (!(root instanceof Display.ItemDisplay)) {
                lastRadius = 0;
                return 0;
            }

            for (Entity passenger : root.getPassengers()) {
                Vector3fc scale = passenger.getEntityData().get(Display.DATA_SCALE_ID);
                lastRadius = decodeRadius(scale.x());
                return lastRadius;
            }
        }

        lastRadius = 0;
        return 0;
    }

    private int decodeRadius(float scaleX) {
        if (lastRadius > 0 && inRadius(lastRadius, scaleX)) return lastRadius;

        int approx = Math.round(scaleX / RADIUS_SCALE_STEP);
        int best = -1;
        float bestDistance = Float.MAX_VALUE;
        for (int r = Math.max(1, approx - 1); r <= Math.min(MAX_RADIUS, approx + 1); r++) {
            if (!inRadius(r, scaleX)) continue;

            float distance = Math.abs(scaleX - r * RADIUS_SCALE_STEP);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = r;
            }
        }

        return best != -1 ? best : Math.clamp(approx, 1, MAX_RADIUS);
    }

    private static boolean inRadius(int radius, float scaleX) {
        float base = radius * RADIUS_SCALE_STEP;
        return scaleX >= base * PULSE_LOW && scaleX <= base * PULSE_HIGH;
    }

    @Override
    public boolean validSpell(SpellType spellType) {
        return true;
    }

    @Override
    public boolean allowsOutOfWindowSpawn(List<Float> modelIds) {
        return verifyCustomModelData(modelIds);
    }
}
