package com.wynntils.core;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import me.fallenbreath.conditionalmixin.api.mixin.RestrictiveMixinConfigPlugin;

public class WynntilsMixinConfigPlugin extends RestrictiveMixinConfigPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    protected void onRestrictionCheckFailed(String mixinClassName, String reason) {
        LOGGER.warn("Mixin {} with restriction failed with reason: {}", mixinClassName, reason);
    }
}