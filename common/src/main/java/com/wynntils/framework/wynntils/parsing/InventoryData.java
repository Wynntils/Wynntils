package com.wynntils.framework.wynntils.parsing;

import net.minecraft.client.Minecraft;

public class InventoryData {
    /**
     * @return The maximum number of soul points the current player can have
     *
     * Note: If veteran, this should always be 15, but currently might return the wrong value
     */
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
     *
     * -1 if unable to determine
     *
     * Also check that {@code {@link #getMaxSoulPoints()} >= {@link #getSoulPoints()}},
     * in which case soul points are already full
     */
    public static int getTicksTillNextSoulPoint() {
        if (Minecraft.getInstance().level == null) return -1;

        return 24000 - (int) (Minecraft.getInstance().level.getDayTime() % 24000);
    }
}
