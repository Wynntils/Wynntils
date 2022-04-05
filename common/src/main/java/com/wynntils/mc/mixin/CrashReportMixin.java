/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.utils.CrashReportManager;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.List;

@Mixin(CrashReport.class)
public abstract class CrashReportMixin {

    @Redirect(
            at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"),
            method = "getDetails(Ljava/lang/StringBuilder;)V")
    private Iterator<CrashReportCategory> addWynntilsDetails(List<CrashReportCategory> instance) {
        instance.add(CrashReportManager.generateDetails());

        return instance.iterator();
    }
}
