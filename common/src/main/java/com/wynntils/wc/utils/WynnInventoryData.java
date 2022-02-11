/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils;

import net.minecraft.client.Minecraft;

public class WynnInventoryData {
    /**
     * @return The maximum number of soul points the current player can have
     *     <p>Note: If veteran, this should always be 15, but currently might return the wrong value
     */

    // The wynncraft api has some info on if someone's a veteran, TODO either use that or find out
    // if veterans still exist
    public int getMaxSoulPoints() throws Exception {
        throw new Exception("TODO");
        /*
        int maxIfNotVeteran = 10 + MathHelper.clamp(get(CharacterData.class).getLevel() / 15, 0, 5);
        if (getSoulPoints() > maxIfNotVeteran) {
            return 15;
        }
        return maxIfNotVeteran;

         */
    }

    /**
     * @return Time in game ticks (1/20th of a second, 50ms) until next soul point
     *     <p>-1 if unable to determine
     *     <p>Also check that {@code {@link #getMaxSoulPoints()} >= {@link #getSoulPoints()}}, in
     *     which case soul points are already full
     */
    public static int getTicksTillNextSoulPoint() {
        if (Minecraft.getInstance().level == null) return -1;

        return 24000 - (int) (Minecraft.getInstance().level.getDayTime() % 24000);
    }
}
