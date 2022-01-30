/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.utils.CrashReportManager;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public abstract class CrashReportMixin {
    @Shadow @Final private CrashReportCategory systemDetails;

    @Inject(at = @At("RETURN"), method = "initDetails")
    private void addWynntilsDetails(CallbackInfo info) {
        String details = CrashReportManager.generateInfo();

        if (!details.isEmpty()) {
            systemDetails.setDetail("Wynntils", details);
        }
    }
}
