/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;

public final class WynnUtils {
    /**
     * Removes the characters 'À' ('\u00c0') and ֎ ('\u058e') that is sometimes added in Wynn APIs and
     * replaces '’' ('\u2019') (RIGHT SINGLE QUOTATION MARK) with '\'' (And trims)
     *
     * @param input string
     * @return the string without these two chars
     */
    public static String normalizeBadString(String input) {
        if (input == null) return "";
        return StringUtils.replaceEach(input, new String[] {"ÀÀÀ", "À", "֎", "’"}, new String[] {" ", "", "", "'"})
                .trim();
    }

    public static BlockPos newBlockPos(double x, double y, double z) {
        return new BlockPos((int) x, (int) y, (int) z);
    }

    public static BlockPos newBlockPos(Vec3 vec3) {
        return new BlockPos((int) vec3.x, (int) vec3.y, (int) vec3.z);
    }

    public static String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static ByteBuffer decodeBase64(String iconString) {
        return Base64.getDecoder().decode(ByteBuffer.wrap(iconString.getBytes(StandardCharsets.UTF_8)));
    }
}
