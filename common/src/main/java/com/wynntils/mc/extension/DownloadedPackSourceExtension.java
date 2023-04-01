/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.extension;

import net.minecraft.server.packs.repository.Pack;

public interface DownloadedPackSourceExtension {
    Pack getServerPack();

    void setServerPack(Pack pack);
}
