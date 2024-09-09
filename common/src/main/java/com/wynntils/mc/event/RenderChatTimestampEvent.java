package com.wynntils.mc.event;

import net.neoforged.bus.api.Event;

public class RenderChatTimestampEvent extends Event {
   private boolean rendered = false;

   public boolean isRendered() {
      return rendered;
   }

   public void setRendered(boolean rendered) {
      this.rendered = rendered;
   }
}