package com.wynntils.mc.mixin;

import com.wynntils.core.components.Managers;
import com.wynntils.features.chat.ChatTimestampFeature;
import com.wynntils.mc.extension.GuiMessageLineExtension;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.type.Pair;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.util.Optional;

@Mixin(GuiMessage.Line.class)
public abstract class GuiMessageLineMixin implements GuiMessageLineExtension {
   @Unique
   private LocalDateTime createdAt;

   @Unique
   private Optional<Pair<Component, Integer>> timestamp;

   @Inject(method = "<init>", at = @At("TAIL"))
   private void init(int addedTime, FormattedCharSequence content, @Nullable GuiMessageTag tag, boolean endOfEntry, CallbackInfo ci) {
      timestamp = Optional.empty();
   }

   @Override
   public LocalDateTime getCreated() {
      return createdAt;
   }

   @Override
   public void setCreated(LocalDateTime date) {
      createdAt = date;
   }

   @Override
   public Optional<Pair<Component, Integer>> getTimestamp() {
      return timestamp;
   }

   @Override
   public void setTimestamp(Component component) {
      timestamp = Optional.of(
              Pair.of(
                      component,
                      FontRenderer.getInstance().getFont().width(component)
              )
      );
   }
}
