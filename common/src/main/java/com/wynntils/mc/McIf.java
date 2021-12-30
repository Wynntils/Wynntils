package com.wynntils.mc;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class McIf {
    public static String getUnformattedText(Component msg) {
        return msg.getString(1024);
    }

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }
}
