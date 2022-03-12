package com.wynntils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.wynntils.core.config.ui.ConfigScreen;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.features.ItemGuessFeature;
import com.wynntils.mc.utils.commands.CommandBase;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ConfigCommand extends CommandBase {

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("config").executes(s -> {
            try {
                Thread.sleep(1);
                Minecraft.getInstance().setScreen(new ConfigScreen(FeatureRegistry.getFeature(ItemGuessFeature.class)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            return 1;
        }));
    }
}
