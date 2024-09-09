package com.wynntils.mc.event;

import net.minecraft.client.GuiMessage;
import net.neoforged.bus.api.Event;

public class AddGuiMessageLineEvent extends Event {
   private final GuiMessage.Line line;
   private final int index;

   public AddGuiMessageLineEvent(GuiMessage.Line line, int index) {
      this.line = line;
      this.index = index;
   }

   public GuiMessage.Line getLine() {
      return line;
   }

   public int getIndex() {
      return index;
   }
}
