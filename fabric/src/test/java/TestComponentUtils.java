/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
import com.wynntils.utils.mc.ComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestComponentUtils {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void getCoded_ShouldWork() {
        final Component component = Component.literal("Hello,")
                .withStyle(ChatFormatting.RED)
                .append(Component.literal(" World!")
                        .withStyle(ChatFormatting.BLUE)
                        .withStyle(ChatFormatting.BOLD));
        final String expected = "§cHello,§r§9§l World!";

        String actual = ComponentUtils.getCoded(component);

        Assertions.assertEquals(expected, actual, "ComponentUtils.getCoded() returned an unexpected value.");
    }
}
