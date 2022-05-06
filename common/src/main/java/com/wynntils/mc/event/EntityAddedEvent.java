package com.wynntils.mc.event;

import net.minecraft.world.entity.EntityType;

import java.util.UUID;

public class EntityAddedEvent {
    private final int data;
    private final int id;
    static final double LIMIT;
    static final double MAGICAL_QUANTIZATION;
    private final EntityType<?> type;
    private final UUID uuid;
    private final double x;
    private final int xa;
    private final int xRot;
    private final double y;
    private final int ya;
    private final int yRot;
    private final double z;
    private final int za;

    public EntityAddedEvent(int data, int id, double LIMIT,
                            double MAGICAL_QUANTIZATION,
                            EntityType<?> type,
                            UUID uuid, double x, int xa,
                            int xRot, double y, int ya,
                            int yRot, double z, int za
                            ) {
        this.data = data;
        this.id = id;

        this.type = type;
        this.uuid = uuid;
        this.x = x;
        this.xa = xa;
        this.xRot = xRot;
        this.y = y;
        this.ya = ya;
        this.yRot = yRot;
        this.z = z;
        this.za = za;
    }
}
