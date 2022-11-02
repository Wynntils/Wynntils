package com.wynntils.mc.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class PlayerGhostArmorRenderEvent extends Event {

    private LivingEntity entity;
    private boolean renderGhostArmor;

    public PlayerGhostArmorRenderEvent(LivingEntity entity) {
        this.entity = entity;
        this.renderGhostArmor = true;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public boolean shouldRenderGhostArmor() {
        return renderGhostArmor;
    }

    public void setRenderGhostArmor(boolean renderGhostArmor) {
        this.renderGhostArmor = renderGhostArmor;
    }

}
