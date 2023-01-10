package com.wynntils.features.user;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.features.UserFeature;
import com.wynntils.wynn.event.SpellCastedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class ShamanTotemTrackingFeature extends UserFeature {

    @SubscribeEvent
    public void onSpellCasted(SpellCastedEvent e) {
        System.out.println(e.getSpell().getName());
    }

    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.Spell);
    }
}
