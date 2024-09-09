package com.wynntils.mc.mixin;

import com.wynntils.mc.extension.GuiMessageExtension;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;

@Mixin(GuiMessage.class)
public abstract class GuiMessageMixin implements GuiMessageExtension {
   @Unique
   private LocalDateTime createdAt;

   @Inject(method = "<init>", at = @At("TAIL"))
   private void init(
           int addedTime,
           Component content,
           MessageSignature signature,
           GuiMessageTag tag,
           CallbackInfo ci
   ) {
      createdAt = LocalDateTime.now();
   }

   @Override
   public LocalDateTime getCreated() {
      return createdAt;
   }
}
