package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.event.KeyInputEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.SMALL, performance = PerformanceImpact.SMALL)
public class DialogueOptionOverrideFeature extends Feature {
    @Override
    public MutableComponent getNameComponent() {
        return new TranslatableComponent("feature.wynntils.dialogueOptionOverride.name");
    }

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {}

    @Override
    protected boolean onEnable() {
        WynntilsMod.getEventBus().register(this);
        return true;
    }

    @Override
    protected void onDisable() {
        WynntilsMod.getEventBus().unregister(this);
    }

    @SubscribeEvent
    public void onDialogueKeyPress(KeyInputEvent e) {
        System.out.println("keyinputevent triggered");
        if (!WynnUtils.onWorld()) return;
        System.out.println(e.getKey());
        System.out.println(McUtils.player().getInventory().selected);
        if (e.getKey() - 2 == McUtils.player().getInventory().selected) {
            System.out.println("Sent key");
            McUtils.mc().getConnection().send(new ServerboundSetCarriedItemPacket(e.getKey()));
        }
    }
}
