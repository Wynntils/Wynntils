package com.wynntils.mc.extension;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.type.Pair;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

public interface GuiMessageLineExtension {
   LocalDateTime getCreated();

   void setCreated(LocalDateTime date);

   Optional<Pair<Component, Integer>> getTimestamp();

   void setTimestamp(Component component);
}
