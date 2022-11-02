package com.wynntils.features.user.players;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.PlayerGhostArmorRenderEvent;
import com.wynntils.wynn.utils.WynnPlayerUtils;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.PLAYERS)
public class PlayerGhostArmorTransparencyFeature extends UserFeature {

    @Config
    public boolean transparentGhostArmor = true;

    @SubscribeEvent
    public void onGhostArmorRender(PlayerGhostArmorRenderEvent e) {
        if (!WynnUtils.onWorld()) return;

        if (!(e.getEntity() instanceof Player player)) return;

        if (WynnPlayerUtils.isPlayerGhost(player)) {
            e.setRenderGhostArmor(transparentGhostArmor);
        }
    }

}
